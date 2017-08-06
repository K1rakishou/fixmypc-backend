package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.ImageInfo
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.math.BigInteger

@Component
class MalfunctionRequestServiceImpl : MalfunctionRequestService {

    @Value("\${spring.http.multipart.max-file-size}")
    var maxFileSize: Long = 0

    @Value("\${spring.http.multipart.max-request-size}")
    var maxRequestSize: Long = 0

    @Value("\${fixmypc.backend.fileservers}")
    var fileServers: Array<String> = arrayOf()

    @Autowired
    lateinit var imageService: ImageService

    @Autowired
    lateinit var forwardImagesServer: ForwardImagesService

    override fun handleNewMalfunctionRequest(uploadingFiles: Array<MultipartFile>, request: MalfunctionRequest): MalfunctionRequestService.Result {
        var totalSize = 0L
        val imageInfoList = hashMapOf<Int, ArrayList<ImageInfo>>()

        for (filePart in uploadingFiles) {
            if (filePart.size > maxFileSize) {
                return MalfunctionRequestService.Result.FileSizeExceeded()
            }

            totalSize += filePart.size
            System.out.println("fileSize: ${filePart.size}")

            val imageMd5 = imageService.getImageMd5(filePart.bytes)
            val bigInteger = BigInteger(imageMd5)
            val serverId = Math.abs((bigInteger % BigInteger.valueOf(fileServers.size.toLong())).toInt())
            System.out.println("serverId: $serverId")

            imageInfoList.putIfAbsent(serverId, ArrayList())
            imageInfoList[serverId]!!.add(ImageInfo(Util.toHexString(imageMd5), serverId, filePart))
        }

        System.out.println("totalSize: $totalSize")

        if (totalSize > maxRequestSize) {
            return MalfunctionRequestService.Result.RequestSizeExceeded()
        }

        forwardImagesServer.forwardImages(imageInfoList)
        return MalfunctionRequestService.Result.Ok()
    }
}