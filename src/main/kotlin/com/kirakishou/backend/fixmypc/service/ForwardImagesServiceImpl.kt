package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.ForwardedImageInfo
import com.kirakishou.backend.fixmypc.model.ImageInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.FileOutputStream
import javax.annotation.PostConstruct


@Component
class ForwardImagesServiceImpl : ForwardImagesService {

    @Value("\${fixmypc.backend.fileservers}")
    var fileServers: Array<String> = arrayOf()

    @Value("\${fixmypc.backend.images.temp-path}")
    lateinit var tempImgsPath: String

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var generator: Generator

    @Autowired
    lateinit var log: FileLog

    @Autowired
    lateinit var sendRequestService: SendRequestService<String>

    @PostConstruct
    fun init() {
        restTemplate.messageConverters.add(FormHttpMessageConverter())
    }

    override fun forwardImages(image: Map<Int, ArrayList<ImageInfo>>, imagesType: Int): MalfunctionRequestService.Result {
        val tempFileNames = ArrayList<String>()
        val badFiles = hashMapOf<Int, ArrayList<String>>()

        try {
            for ((serverId, imageInfos) in image) {
                val mvmap = LinkedMultiValueMap<String, Any>()
                var tempFileName: String
                val fimageInfo = ForwardedImageInfo()

                for (imageInfo in imageInfos) {
                    tempFileName = "${tempImgsPath}${imageInfo.filePart.originalFilename}"
                    tempFileNames.add(tempFileName)

                    val fo = FileOutputStream(tempFileName)

                    fo.use {
                        it.write(imageInfo.filePart.bytes)
                    }

                    mvmap.add("images", FileSystemResource(tempFileName))

                    val imageName = generator.generateImageName()
                    val newImageName = "n${serverId}_i${imageName}"

                    fimageInfo.imageOrigName.add(imageInfo.filePart.originalFilename)
                    fimageInfo.imageType.add(imagesType)
                    fimageInfo.imageNewName.add(newImageName)
                    fimageInfo.ownerId.add(0L)
                }

                mvmap.add("images_info", fimageInfo)

                val headers = HttpHeaders()
                headers.contentType = MediaType.MULTIPART_FORM_DATA

                val httpEntity = HttpEntity<MultiValueMap<String, Any>>(mvmap, headers)
                val url = "http://${fileServers[serverId]}/v1/api/upload_image"

                try {
                    val response = restTemplate.postForObject(url, httpEntity, FileServerAnswer::class.java)
                    val fileServerErrCode = FileServerErrorCode.from(response.errorCode)

                    if (fileServerErrCode != FileServerErrorCode.OK) {
                        when (fileServerErrCode) {
                            FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGE -> return MalfunctionRequestService.Result.CouldNotStoreOneOreMoreImages()
                            FileServerErrorCode.UNKNOWN_ERROR -> MalfunctionRequestService.Result.UnknownError()
                            else -> throw RuntimeException("Unknown fileServerErrCode: $fileServerErrCode")
                        }
                    }

                } catch(e: Exception) {
                    log.e(e)
                    return MalfunctionRequestService.Result.UnknownError()
                }
            }
        } finally {
            for (fileName in tempFileNames) {
                val f = File(fileName)
                f.delete()
            }
        }

        return MalfunctionRequestService.Result.Ok()
    }
}