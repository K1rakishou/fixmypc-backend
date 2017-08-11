package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.model.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.util.Util
import io.reactivex.Flowable
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.util.*
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

        val result = checkFilesSizes(uploadingFiles)
        if (result !is MalfunctionRequestService.Result.Ok) {
            return Single.just(result)
        }

        if (!fileServerManager.isAtLeastOneServerAlive()) {
            return Single.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
        }

        val tempFiles = ArrayList<String>()
        val responseList = arrayListOf<Flowable<FileServerAnswer>>()

        for (uploadingFile in uploadingFiles) {
            val tempFile = "${tempImgsPath}${uploadingFile.originalFilename}"
            tempFiles.add(tempFile)

            val fo = FileOutputStream(tempFile)

            fo.use {
                it.write(uploadingFile.bytes)
            }

            val server = fileServerManager.getWorkingServerOrNothing()
            if (!server.isPresent()) {
                return Single.just(MalfunctionRequestService.Result.AllFileServersAreNotWorking())
            }

            responseList += sendRequestService.sendImageRequest(server.get().serverId, server.get().host, tempFile,
                    uploadingFile.originalFilename, 0, 0L, FileServerAnswer::class.java)
                    //max request waiting time 5 seconds
                    .timeout(5, TimeUnit.SECONDS)
                    .doOnError({ error ->
                        log.e(error)
                        deleteTempFiles(tempFiles)

                        //no response after five seconds means that server is probably dead
                        server.ifPresent {
                            it.isWorking = false
                            it.timeOfDeath = Util.getTimeFast()
                        }
                    })
        }

        return Flowable.merge(responseList)
                .toList()
                .map { fileServerResponses ->
                    for ((errorCode, badPhotoNames) in fileServerResponses) {
                        if (FileServerErrorCode.from(errorCode) == FileServerErrorCode.OK) {
                            return@map MalfunctionRequestService.Result.Ok()
                        }

                        when (FileServerErrorCode.from(errorCode)) {
                            FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGE -> TODO()
                            FileServerErrorCode.UNKNOWN_ERROR -> MalfunctionRequestService.Result.UnknownError()
                            FileServerErrorCode.NOT_ENOUGH_DISK_SPACE -> TODO()
                        }
                    }

                    return@map MalfunctionRequestService.Result.Ok()
                }
    }

    private fun deleteTempFiles(tempFileNames: ArrayList<String>) {
        for (fileName in tempFileNames) {
            val f = File(fileName)
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
}




































