package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.manager.FileServersManagerImpl
import com.kirakishou.backend.fixmypc.model.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.annotation.PostConstruct

@Component
class MalfunctionRequestServiceImpl : MalfunctionRequestService {

    @Value("\${spring.http.multipart.max-file-size}")
    private var maxFileSize: Long = 0

    @Value("\${spring.http.multipart.max-request-size}")
    private var maxRequestSize: Long = 0

    @Value("\${fixmypc.backend.fileservers}")
    private var fileServerHosts: Array<String> = arrayOf()

    @Value("\${fixmypc.backend.fileserver-ping-interval}")
    private val pingInterval: Long = 0

    @Value("\${fixmypc.backend.max-images-per-request}")
    private val maxImagesPerRequest: Int = 0

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    lateinit var generator: Generator

    @Autowired
    private lateinit var fileServerManager: FileServersManager

    @Autowired
    private lateinit var distributedImageServerService: DistributedImageServerService

    @Autowired
    private lateinit var tempFileService: TempFilesService

    private val FILE_SERVER_REQUEST_TIMEOUT: Long = 7L

    @PostConstruct
    fun init() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()

        for (host in fileServerHosts) {
            fileServerInfoList.add(FileServerInfo(host, true))
        }

        fileServerManager.init(fileServerInfoList, TimeUnit.MINUTES.toMillis(pingInterval))
    }

    override fun handleNewMalfunctionRequest(uploadingFiles: Array<MultipartFile>, imageType: Int,
                                             request: MalfunctionRequest): Single<MalfunctionRequestService.Result> {

        val newMalfunctionRequest = Malfunction()
        val ownerId = 0L //getOwnerId

        //return error code if user somehow sent a request without any images
        if (uploadingFiles.isEmpty()) {
            log.e("No files to upload")
            return Single.just(MalfunctionRequestService.Result.NoFilesToUpload())
        }

        //return error code if user somehow sent more than "maxImagesPerRequest" images
        if (uploadingFiles.size > maxImagesPerRequest) {
            log.e("Too many files to upload (uploadingFiles.size > maxImagesPerRequest)")
            return Single.just(MalfunctionRequestService.Result.ImagesCountExceeded())
        }

        val requestCheckResult = checkRequestCorrectness(request)
        if (requestCheckResult !is MalfunctionRequestService.Result.Ok) {
            log.e("Bad malfunction request")
            return Single.just(requestCheckResult)
        }

        //return error code if either one of the images size is bigger than "maxFileSize" or sum of images sizes bigger than "maxRequestSize"
        val fileSizesCheckResult = checkFilesSizes(uploadingFiles)
        if (fileSizesCheckResult !is MalfunctionRequestService.Result.Ok) {
            log.e("Bad size of photos")
            return Single.just(fileSizesCheckResult)
        }

        //return error code if there are no working file servers
        if (!fileServerManager.isAtLeastOneServerAlive()) {
            log.e("Could not get at least one file server")
            return Single.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
        }

        //for every image try to get a working file server
        val uploadingFilesList = uploadingFiles.toList()
        val servers = fileServerManager.getServers(uploadingFilesList.size)

        check(servers.size == uploadingFiles.size) {
            "Could not get enough file servers to store images"
        }

        val zippedFilesAndServers = uploadingFiles.zip(servers)

        return Flowable.just(zippedFilesAndServers)
                .subscribeOn(Schedulers.io())
                //send all images
                .flatMap { filesAndServers ->
                    return@flatMap handleFiles(imageType, ownerId, filesAndServers, uploadingFiles)
                }
                //collect all responses
                .toList()
                .map { results ->
                    //whatever the responses - do not forget to delete temp files
                    tempFileService.deleteAllTempFiles()

                    for (result in results) {
                        if (result !is MalfunctionRequestService.Result.Ok) {
                            //the only possible reason for this to happen is when all file servers are down
                            log.e("Error. Something went wrong")
                            return@map result
                        }
                    }

                    log.d("Everything is OK")
                    return@map MalfunctionRequestService.Result.Ok()
                }
    }

    private fun handleFiles(imageType: Int, ownerId: Long, filesAndServers: List<Pair<MultipartFile, FileServersManagerImpl.ServerWithId>>,
                            uploadingFiles: Array<MultipartFile>): Flowable<out MalfunctionRequestService.Result> {

        val responses = arrayListOf<Flowable<FileServerAnswer>>()

        //send all images and retrieve responds
        for ((multipartFile, fileServerInfo) in filesAndServers) {
            val newImageName = makeNewImageName(fileServerInfo.id)
            val tempFile = tempFileService.fromMultipartFile(multipartFile)

            responses += storeImage(newImageName, imageType, ownerId, fileServerInfo, tempFile, multipartFile)
        }

        //TODO: Would be nice to get rid of the blockingGet() but dunno how to do that atm
        //we need to collect only bad responses
        val badResponses = Flowable.merge(responses)
                .filter({
                    val errorCode = FileServerErrorCode.from(it.errorCode)
                    log.d("fileServerResponseErrorCode: $errorCode")

                    return@filter errorCode != FileServerErrorCode.OK
                })
                .toList()
                .blockingGet()

        //return good error code if all images were successfully stored (no bad responses)
        if (badResponses.isEmpty()) {
            log.d("No bad responses. Every image was successfully stored")
            return Flowable.just(MalfunctionRequestService.Result.Ok())
        }

        val badFiles = arrayListOf<String>()

        //collect all bad files (that couldn't have been uploaded)
        for (resp in badResponses) {
            check(resp.badPhotoNames.isNotEmpty()) {
                "File server did send bad error code, but did not send bad photos with it. Possible bad mock"
            }

            badFiles.addAll(resp.badPhotoNames)
        }

        var index = 0
        log.d("badFilesCount = ${badFiles.size}")

        //try to resend images
        while (index < badFiles.size) {
            //try to get working server
            val fileServerFickle = fileServerManager.getServer()

            //if there are none - return error status
            if (!fileServerFickle.isPresent()) {
                log.e("Could not find a working file server")
                return Flowable.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
            }

            //if there are any - try to store remaining images on to them
            val badFile = badFiles[index]
            val fileServer = fileServerFickle.get()
            val multipartFile = getFileByOriginalName(uploadingFiles, badFile)
            val tempFile = tempFileService.fromMultipartFile(multipartFile)
            val newImageName = makeNewImageName(fileServer.id)
            log.d("Trying to resend a file. fileIndex = ${index}, serverId = ${fileServer.id}")

            val response = storeImage(newImageName, imageType, ownerId, fileServer, tempFile, multipartFile)
                    .blockingFirst()

            val errorCode = FileServerErrorCode.from(response.errorCode)
            if (errorCode == FileServerErrorCode.OK) {
                log.d("The file has been successfully stored")
                ++index
                continue
            }

            log.d("Could not store a file. errorCode = ${errorCode}")

            //if for some reason one more servers went down - mark them as not working and repeat this loop
            if (errorCode == FileServerErrorCode.REQUEST_TIMEOUT ||
                    errorCode == FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGES) {
                fileServerManager.notWorking(fileServer.id)
            }
        }

        return Flowable.just(MalfunctionRequestService.Result.Ok())
    }

    private fun makeNewImageName(serverId: Int): String {
        val generatedImageName = generator.generateImageName()
        return "n${serverId}_i$generatedImageName"
    }

    private fun storeImage(newImageName: String, imageType: Int, ownerId: Long, server: FileServersManagerImpl.ServerWithId,
                           tempFile: String, uploadingFile: MultipartFile): Flowable<FileServerAnswer> {

        return distributedImageServerService.storeImage(server.id, server.fileServerInfo.host, tempFile,
                uploadingFile.originalFilename, newImageName, imageType, ownerId, FileServerAnswer::class.java)
                //max request waiting time
                .timeout(FILE_SERVER_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .onErrorResumeNext({ error: Throwable ->
                    if (error is TimeoutException) {
                        log.d("Operation was cancelled due to timeout")
                        fileServerManager.notWorking(server.id)

                        return@onErrorResumeNext Flowable.just(FileServerAnswer(
                                FileServerErrorCode.REQUEST_TIMEOUT.value,
                                arrayListOf(uploadingFile.originalFilename)))
                    } else {
                        log.e(error)
                        return@onErrorResumeNext Flowable.just(FileServerAnswer(
                                FileServerErrorCode.UNKNOWN_ERROR.value,
                                emptyList()))
                    }
                })
                .doOnNext({ response ->
                    val errCode = FileServerErrorCode.from(response.errorCode)

                    if (errCode == FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGES) {
                        fileServerManager.notWorking(server.id)
                    }
                })
    }

    private fun getFileByOriginalName(uploadingFiles: Array<MultipartFile>, name: String): MultipartFile {
        for (file in uploadingFiles) {
            if (file.originalFilename == name) {
                return file
            }
        }

        throw IllegalStateException("Could not file $name in uploadingFiles")
    }

    private fun checkFilesSizes(uploadingFiles: Array<MultipartFile>): MalfunctionRequestService.Result {
        var totalSize = 0L

        for (uploadingFile in uploadingFiles) {
            if (uploadingFile.size > maxFileSize) {
                return MalfunctionRequestService.Result.FileSizeExceeded()
            }

            totalSize += uploadingFile.size
        }

        if (totalSize > maxRequestSize) {
            return MalfunctionRequestService.Result.RequestSizeExceeded()
        }

        return MalfunctionRequestService.Result.Ok()
    }

    private fun checkRequestCorrectness(request: MalfunctionRequest): MalfunctionRequestService.Result {
        return MalfunctionRequestService.Result.Ok()
    }
}




































