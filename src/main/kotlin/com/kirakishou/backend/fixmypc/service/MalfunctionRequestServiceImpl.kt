package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.ImageInfo
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.atomic.AtomicInteger

@Component
class MalfunctionRequestServiceImpl : MalfunctionRequestService {

    @Value("\${spring.http.multipart.max-file-size}")
    var maxFileSize: Long = 0

    @Value("\${spring.http.multipart.max-request-size}")
    var maxRequestSize: Long = 0

    @Value("\${fixmypc.backend.fileservers}")
    var fileServers: Array<String> = arrayOf()

    val serverId = AtomicInteger(0)

    @Autowired
    lateinit var forwardImagesServer: ForwardImagesService

    override fun handleNewMalfunctionRequest(uploadingFiles: Array<MultipartFile>, imagesType: Int,
                                             request: MalfunctionRequest): MalfunctionRequestService.Result {
        var totalSize = 0L
        val imageInfoList = hashMapOf<Int, ArrayList<ImageInfo>>()

        for (filePart in uploadingFiles) {
            if (filePart.size > maxFileSize) {
                return MalfunctionRequestService.Result.FileSizeExceeded()
            }

            totalSize += filePart.size
            System.out.println("fileSize: ${filePart.size}")

            val id = serverId.getAndIncrement() % fileServers.size
            System.out.println("id: $id, serverId: ${serverId.get()}")

            imageInfoList.putIfAbsent(id, ArrayList())
            imageInfoList[id]!!.add(ImageInfo(id, filePart))
        }

        System.out.println("totalSize: $totalSize")

        if (totalSize > maxRequestSize) {
            return MalfunctionRequestService.Result.RequestSizeExceeded()
        }

        forwardImagesServer.forwardImages(imageInfoList, imagesType)
        return MalfunctionRequestService.Result.Ok()
    }
}