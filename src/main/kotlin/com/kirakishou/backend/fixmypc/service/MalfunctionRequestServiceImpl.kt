package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.manager.FileServersManagerImpl
import com.kirakishou.backend.fixmypc.model.*
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.model.repository.postgresql.MalfunctionRepository
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.sql.SQLException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
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

    @Autowired
    lateinit var generator: Generator

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    private lateinit var fileServerManager: FileServersManager

    @Autowired
    private lateinit var fileServerService: FileServerService

    @Autowired
    private lateinit var tempFileService: TempFilesService

    @Autowired
    private lateinit var malfunctionRepository: MalfunctionRepository

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

        val ownerId = 0L //getOwnerId
        val malfunctionRequestId = generator.generateMalfunctionRequestId()

        //return error code if user somehow sent a request without any images
        if (uploadingFiles.isEmpty()) {
            log.e("No files to upload")
            return Single.just(MalfunctionRequestService.Result.NoFilesToUpload())
        }

        //return error code if user somehow sent more than "maxImagesPerRequest" images
        if (uploadingFiles.size > Constant.MALFUNCTION_MAX_IMAGES_PER_REQUEST) {
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

        val resultList = Flowable.just(zippedFilesAndServers)
                .subscribeOn(Schedulers.io())
                //send all images
                .flatMap { filesAndServers ->
                    return@flatMap handleFiles(imageType, ownerId, malfunctionRequestId, filesAndServers, uploadingFiles)
                }
                //collect all responses
                .toList()

        return resultList.map { results ->
            //whatever the responses are - do not forget to delete temp files
            tempFileService.deleteAllTempFiles()

            //check results for errors
            for (result in results) {
                if (result !is MalfunctionRequestService.Result.AllImagesUploaded) {
                    //the only possible reason for this to happen is when all file servers are down
                    log.e("Error. Something went wrong")
                    return@map result
                }
            }

            val imageNamesList = (results as ArrayList<MalfunctionRequestService.Result.AllImagesUploaded>).stream()
                    .flatMap { it.names.stream() }
                    .map { it }
                    .collect(Collectors.toList())

            val malfunction = Malfunction(
                    category = request.category,
                    description = request.description,
                    malfunctionRequestId = malfunctionRequestId,
                    imageNamesList = imageNamesList)

            try {
                malfunctionRepository.createNewMalfunctionRequest(malfunction)
            } catch (e: SQLException) {
                log.e(e)

                //we failed to save malfunction request in the DB, so we have to notify file servers to delete images related to the request
                for (imageName in imageNamesList) {
                    val extractedImageInfo = TextUtils.parseImageName(imageName)
                    val host = fileServerManager.getHostById(extractedImageInfo.serverId)

                    fileServerService.deleteMalfunctionRequestImages(ownerId, host, malfunctionRequestId, imageName)
                }

                return@map MalfunctionRequestService.Result.DatabaseError()
            }

            log.d("Everything is OK")
            return@map MalfunctionRequestService.Result.Ok()
        }
    }

    private fun handleFiles(imageType: Int, ownerId: Long, malfunctionRequestId: String, filesAndServers: List<Pair<MultipartFile, FileServersManagerImpl.ServerWithId>>,
                            uploadingFiles: Array<MultipartFile>): Flowable<out MalfunctionRequestService.Result> {

        val responses = arrayListOf<Flowable<FileServerAnswerWrapper>>()

        //send all images and retrieve responds
        for ((multipartFile, fileServerInfo) in filesAndServers) {
            val tempFile = tempFileService.fromMultipartFile(multipartFile)
            responses += storeImage(imageType, ownerId, malfunctionRequestId, fileServerInfo, tempFile, multipartFile)
        }

        //TODO: Would be nice to get rid of the blockingGet() but dunno how to do that atm
        //we need to collect only bad responses
        val mergedResponses = Flowable.merge(responses)
                .toList()
                .blockingGet()

        val newImagesNames = mergedResponses.stream()
                .filter({
                    val errorCode = FileServerErrorCode.from(it.answer.errorCode)
                    return@filter errorCode == FileServerErrorCode.OK
                })
                .map { it.newImageName }
                .collect(Collectors.toList())

        val badResponses = mergedResponses.stream()
                .filter({
                    val errorCode = FileServerErrorCode.from(it.answer.errorCode)
                    log.d("fileServerResponseErrorCode: $errorCode")

                    return@filter errorCode != FileServerErrorCode.OK
                })
                .collect(Collectors.toList())

        //return good error code if all images were successfully stored (no bad responses)
        if (badResponses.isEmpty()) {
            log.d("No bad responses. Every image was successfully stored")
            return Flowable.just(MalfunctionRequestService.Result.AllImagesUploaded(newImagesNames))
        }

        val badFiles = arrayListOf<String>()

        //collect all bad files names (that couldn't have been uploaded)
        for (resp in badResponses) {
            check(resp.answer.badPhotoNames.isNotEmpty()) {
                "File server did send bad error code, but did not send bad photos with it. Possible bad mock"
            }

            badFiles.addAll(resp.answer.badPhotoNames)
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
            log.d("Trying to resend a file. fileIndex = ${index}, serverId = ${fileServer.id}")

            val response = storeImage(imageType, ownerId, malfunctionRequestId, fileServer, tempFile, multipartFile)
                    .blockingFirst()

            val errorCode = FileServerErrorCode.from(response.answer.errorCode)
            if (errorCode == FileServerErrorCode.OK) {
                log.d("The file has been successfully stored")
                newImagesNames.add(response.newImageName)
                ++index
                continue
            }

            log.d("Could not store a file. errorCode = ${errorCode}")

            //if for some reason one more servers went down - mark them as not working and repeat this loop
            if (errorCode == FileServerErrorCode.REQUEST_TIMEOUT || errorCode == FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGES) {
                fileServerManager.notWorking(fileServer.id)
            }
        }

        return Flowable.just(MalfunctionRequestService.Result.AllImagesUploaded(newImagesNames))
    }

    private fun storeImage(imageType: Int, ownerId: Long, malfunctionRequestId: String, server: FileServersManagerImpl.ServerWithId,
                           tempFile: String, uploadingFile: MultipartFile): Flowable<FileServerAnswerWrapper> {

        return fileServerService.saveMalfunctionRequestImage(server.id, server.fileServerInfo.host, tempFile,
                uploadingFile.originalFilename, imageType, ownerId, malfunctionRequestId)
                //max request waiting time
                .timeout(FILE_SERVER_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .onErrorResumeNext({ error: Throwable ->
                    if (error is TimeoutException) {
                        log.d("Operation was cancelled due to timeout")
                        fileServerManager.notWorking(server.id)

                        val answer = FileServerAnswerWrapper(FileServerAnswer(
                                FileServerErrorCode.REQUEST_TIMEOUT.value,
                                arrayListOf(uploadingFile.originalFilename)), "")

                        return@onErrorResumeNext Flowable.just(answer)
                    } else {
                        log.e(error)

                        val answer = FileServerAnswerWrapper((FileServerAnswer(
                                FileServerErrorCode.UNKNOWN_ERROR.value, emptyList())), "")

                        return@onErrorResumeNext Flowable.just(answer)
                    }
                })
                .doOnNext({ response ->
                    val errCode = FileServerErrorCode.from(response.answer.errorCode)

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




































