package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.model.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.util.ServerUtil
import io.reactivex.Flowable
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
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

    @Value("\${fixmypc.backend.images.temp-path}")
    lateinit var tempImgsPath: String

    @Autowired
    lateinit var log: FileLog

    @Autowired
    lateinit var fileServerManager: FileServersManager

    @Autowired
    lateinit var sendRequestService: SendRequestService

    private val FILE_SERVER_REQUEST_TIMEOUT: Long = 7L

    @PostConstruct
    fun init() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()

        for (host in fileServerHosts) {
            fileServerInfoList.add(FileServerInfo(host, true, true))
        }

        fileServerManager.init(fileServerInfoList, pingInterval)
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

        //if we have no alive file servers we can't store photos
        if (!fileServerManager.isAtLeastOneServerAlive()) {
            return Single.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
        }

        val tempFiles = ArrayList<String>()
        val responseList = arrayListOf<Flowable<FileServerAnswer>>()

        //for every multipartfile
        for (uploadingFile in uploadingFiles) {

            //copy it into the temp folder and remember it, so we can delete all of them later
            val tempFile = "${tempImgsPath}${uploadingFile.originalFilename}"
            tempFiles.add(tempFile)

            val fo = FileOutputStream(tempFile)

            fo.use {
                it.write(uploadingFile.bytes)
            }

            //get file server (round robin)
            val serverFickle = fileServerManager.getWorkingServerOrNothing()
            if (!serverFickle.isPresent()) {
                return Single.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
            }

            val server = serverFickle.get()

            //forward photo to the server
            responseList += sendRequestService.sendImageRequest(server.serverId, server.host, tempFile,
                    uploadingFile.originalFilename, 0, 0L, FileServerAnswer::class.java)
                    //max request waiting time
                    .timeout(FILE_SERVER_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    //if timeout has happened that means server is dead.
                    //delete temp file and mark the server as dead
                    .onErrorResumeNext({ error: Throwable ->
                        log.e(error)

                        deleteTempFile(tempFile)
                        fileServerManager.at(server.serverId).isWorking = false
                        fileServerManager.at(server.serverId).timeOfDeath = ServerUtil.getTimeFast()

                        return@onErrorResumeNext Flowable.just(FileServerAnswer(FileServerErrorCode.REQUEST_TIMEOUT.value, emptyList()))
                    })
                    //check errCode to see if server returned NOT_ENOUGH_DISK_SPACE
                    //if so - mark server as run out of disk space
                    .doOnNext({ next ->
                        val errCode = FileServerErrorCode.from(next.errorCode)
                        if (errCode != FileServerErrorCode.OK) {
                            if (errCode == FileServerErrorCode.NOT_ENOUGH_DISK_SPACE) {
                                fileServerManager.at(server.serverId).isDiskSpaceOk = false
                            }
                        }
                    })

        }

        //merge all of the responses into a list
        return Flowable.merge(responseList)
                .toList()
                .map { fileServerResponses ->
                    for ((errorCode, badPhotoNames) in fileServerResponses) {
                        if (FileServerErrorCode.from(errorCode) == FileServerErrorCode.OK) {
                            return@map MalfunctionRequestService.Result.Ok()
                        }

                        when (FileServerErrorCode.from(errorCode)) {
                            FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGE -> TODO()
                            FileServerErrorCode.REQUEST_TIMEOUT -> TODO()
                            FileServerErrorCode.UNKNOWN_ERROR -> MalfunctionRequestService.Result.UnknownError()
                            else -> log.e("Bad errorCode: $errorCode")
                        }
                    }

                    return@map MalfunctionRequestService.Result.Ok()
                }
    }

    private fun deleteTempFiles(tempFileNames: ArrayList<String>) {
        for (fileName in tempFileNames) {
            val f = File(fileName)

            if (f.exists()) {
                f.delete()
            }
        }
    }

    private fun deleteTempFile(tempFile: String) {
        val f = File(tempFile)

        if (f.exists()) {
            f.delete()
        }
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




































