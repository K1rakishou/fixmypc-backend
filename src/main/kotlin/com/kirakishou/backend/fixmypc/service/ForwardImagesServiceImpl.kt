package com.kirakishou.backend.fixmypc.service

import com.fasterxml.jackson.annotation.JsonProperty
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
import java.util.*
import javax.annotation.PostConstruct


@Component
class ForwardImagesServiceImpl : ForwardImagesService {

    @Value("\${fixmypc.backend.fileservers}")
    var fileServers: Array<String> = arrayOf()

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var generator: Generator

    @PostConstruct
    fun init() {
        restTemplate.messageConverters.add(FormHttpMessageConverter())
    }

    override fun forwardImages(image: Map<Int, ArrayList<com.kirakishou.backend.fixmypc.model.ImageInfo>>, imagesType: Int): Boolean {
        val tempFileNames = ArrayList<String>()

        try {
            for ((serverId, imageInfos) in image) {
                System.out.println("key: $serverId")

                val mvmap = LinkedMultiValueMap<String, Any>()
                var tempFileName: String
                val fimageInfo = ForwardedImageInfo()

                for (imageInfo in imageInfos) {
                    tempFileName = "D:\\img\\tmp\\" + imageInfo.filePart.originalFilename
                    tempFileNames.add(tempFileName)

                    val fo = FileOutputStream(tempFileName)

                    fo.use {
                        it.write(imageInfo.filePart.bytes)
                    }

                    mvmap.add("images", FileSystemResource(tempFileName))

                    val imageName = generator.generateImageName()
                    val newImageName = "n${serverId}_i${imageName}.jpg"

                    //mvmap.add("images_info", ImageInfo(imageInfo.filePart.originalFilename, imagesType, fullImageName))
                    fimageInfo.imageOrigName.add(imageInfo.filePart.originalFilename)
                    fimageInfo.imageType.add(imagesType)
                    fimageInfo.imageNewName.add(newImageName)
                }

                mvmap.add("images_info", fimageInfo)

                val headers = HttpHeaders()
                headers.contentType = MediaType.MULTIPART_FORM_DATA

                val httpEntity = HttpEntity<MultiValueMap<String, Any>>(mvmap, headers)
                val url = "http://" + fileServers[serverId] + "/v1/api/upload_image"

                try {
                    val response = restTemplate.postForEntity(url, httpEntity, String::class.java)
                    System.err.println(response)

                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        } finally {
            for (fileName in tempFileNames) {
                val f = File(fileName)
                f.delete()
            }
        }

        return true
    }

    class ForwardedImageInfo {
        @JsonProperty("image_orig_name") val imageOrigName = arrayListOf<String>()
        @JsonProperty("image_type") val imageType = arrayListOf<Int>()
        @JsonProperty("image_name") val imageNewName = arrayListOf<String>()
    }
}