package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.manager.FileServersManagerImpl
import com.kirakishou.backend.fixmypc.model.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.annotation.PostConstruct

@Component
class MalfunctionRequestServiceImpl : MalfunctionRequestService {

    @Value("\${spring.http.multipart.max-file-size}")
    var maxFileSize: Long = 0

    @Value("\${spring.http.multipart.max-request-size}")
    var maxRequestSize: Long = 0

    @Value("\${fixmypc.backend.fileservers}")
    var fileServerHosts: Array<String> = arrayOf()

    @Value("\${fixmypc.backend.fileserver-ping-interval}")
    val pingInterval: Long = 5

    @Autowired
    lateinit var log: FileLog

    @Autowired
    lateinit var fileServerManager: FileServersManager

    @Autowired
    lateinit var distributedImageServerService: DistributedImageServerService

    @Autowired
    lateinit var tempFileService: TempFilesService

    @Autowired
    @Qualifier("processors_count")
    var processorsCount: Int = 0

    lateinit var fileServerRequestsExecutorService: ExecutorService
    private val FILE_SERVER_REQUEST_TIMEOUT: Long = 7L

    @PostConstruct
    fun init() {
        fileServerRequestsExecutorService = Executors.newFixedThreadPool(processorsCount)
        val fileServerInfoList = arrayListOf<FileServerInfo>()

        for (host in fileServerHosts) {
            fileServerInfoList.add(FileServerInfo(host, true, true))
        }

        fileServerManager.init(fileServerInfoList, TimeUnit.MINUTES.toMillis(pingInterval))
    }

    override fun handleNewMalfunctionRequest(uploadingFiles: Array<MultipartFile>, imagesType: Int,
                                             request: MalfunctionRequest): Single<MalfunctionRequestService.Result> {

        val requestCheckResult = checkRequestCorrectness(request)
        if (requestCheckResult !is MalfunctionRequestService.Result.Ok) {
            return Single.just(requestCheckResult)
        }

        val fileSizesCheckResult = checkFilesSizes(uploadingFiles)
        if (fileSizesCheckResult !is MalfunctionRequestService.Result.Ok) {
            return Single.just(fileSizesCheckResult)
        }

        //return error status is there are no working file servers
        if (!fileServerManager.isAtLeastOneServerAlive()) {
            return Single.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
        }

        val uploadingFilesList = uploadingFiles.toList()
        val zippedFilesAndServers = uploadingFiles.zip(fileServerManager.getServers(uploadingFilesList.size))
        log.d("filesCount: ${zippedFilesAndServers.size}")

        return Flowable.just(zippedFilesAndServers)
                .subscribeOn(Schedulers.from(fileServerRequestsExecutorService))
                .flatMap { filesAndServers ->
                    val responses = arrayListOf<Flowable<FileServerAnswer>>()

                    //send all images and get responds
                    for ((multipartFile, fileServerInfo) in filesAndServers) {
                        val tempFile = tempFileService.fromMultipartFile(multipartFile)
                        responses += storeImage(fileServerInfo, tempFile, multipartFile)
                    }

                    //TODO: Would be nice to get rid of the blockingGet() but dunno how to do that atm
                    //we need only bad responses, so we need to filter them
                    val badResponses = Flowable.merge(responses)
                            .filter({
                                val errorCode = FileServerErrorCode.from(it.errorCode)
                                log.d("fileServerResponseErrorCode: $errorCode")

                                return@filter errorCode != FileServerErrorCode.OK
                            })
                            .toList()
                            .blockingGet()

                    //return good status if all images were successfully stored
                    if (badResponses.isEmpty()) {
                        log.d("No bad responses. Every image was successfully stored")
                        return@flatMap Flowable.just(MalfunctionRequestService.Result.Ok())
                    }

                    val badFiles = arrayListOf<String>()

                    for (resp in badResponses) {
                        badFiles.addAll(resp.badPhotoNames)
                    }

                    var index = 0
                    log.d("badFilesCount = ${badFiles.size}")

                    //trying to resend images
                    while (index < badFiles.size) {
                        //try to get working server
                        val fileServerFickle = fileServerManager.getServer()

                        //if there are none - return error status
                        if (!fileServerFickle.isPresent()) {
                            log.e("Could not find a working file server")
                            return@flatMap Flowable.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
                        }

                        //if there are try to store remaining images on them
                        val badFile = badFiles[index]
                        val fileServer = fileServerFickle.get()
                        val multipartFile = getFileByOriginalName(uploadingFiles, badFile)
                        val tempFile = tempFileService.fromMultipartFile(multipartFile)
                        log.d("Trying to resend a file. fileIndex = ${index}, serverId = ${fileServer.id}")

                        val response = storeImage(fileServer, tempFile, multipartFile)
                                .blockingFirst()

                        val errorCode = FileServerErrorCode.from(response.errorCode)
                        if (errorCode == FileServerErrorCode.OK) {
                            log.d("The file has been successfully stored")
                            ++index
                            continue
                        }

                        log.d("Could not store a file. errorCode = ${errorCode}")

                        //of for some reason one more server went down - mark it as not working and repeat this loop
                        if (errorCode == FileServerErrorCode.REQUEST_TIMEOUT ||
                                errorCode == FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGES) {
                            fileServerManager.notWorking(fileServer.id)
                        } else if (errorCode == FileServerErrorCode.NOT_ENOUGH_DISK_SPACE) {
                            fileServerManager.noDiskSpace(fileServer.id)
                        }
                    }

                    return@flatMap Flowable.just(MalfunctionRequestService.Result.Ok())
                }
                .toList()
                .map { results ->
                    //whatever the results - do not forget to delete temp files
                    tempFileService.deleteAllTempFiles()

                    for (result in results) {
                        if (result !is MalfunctionRequestService.Result.Ok) {
                            log.e("Error. Something went wrong")
                            return@map result
                        }
                    }

                    log.d("Everything is OK")
                    return@map MalfunctionRequestService.Result.Ok()
                }
    }

    private fun storeImage(server: FileServersManagerImpl.ServerWithId, tempFile: String, uploadingFile: MultipartFile): Flowable<FileServerAnswer> {
        return distributedImageServerService.storeImage(server.id, server.fileServerInfo.host, tempFile,
                uploadingFile.originalFilename, 0, 0L, FileServerAnswer::class.java)
                //max request waiting time
                .timeout(FILE_SERVER_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .onErrorResumeNext({ error: Throwable ->
                    if (error is TimeoutException) {
                        log.d("Operation was cancelled because of timeout")
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

                    if (errCode == FileServerErrorCode.NOT_ENOUGH_DISK_SPACE || errCode == FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGES) {
                        fileServerManager.noDiskSpace(server.id)
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




































