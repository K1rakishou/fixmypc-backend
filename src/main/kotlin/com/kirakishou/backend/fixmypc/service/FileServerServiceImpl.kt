package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.DistributedImage
import com.kirakishou.backend.fixmypc.model.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.FileServerAnswerWrapper
import io.reactivex.Flowable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.AsyncRestTemplate
import javax.annotation.PostConstruct

@Component
class FileServerServiceImpl : FileServerService {

    @Autowired
    lateinit var generator: Generator

    @Autowired
    lateinit var restTemplate: AsyncRestTemplate

    @PostConstruct
    fun init() {
        restTemplate.messageConverters.add(FormHttpMessageConverter())
    }

    override fun storeImage(serverId: Int, host: String, tempFile: String, originalImageName: String, imageType: Int,
                                ownerId: Long, malfunctionRequestId: String): Flowable<FileServerAnswerWrapper> {

        val mvmap = LinkedMultiValueMap<String, Any>()
        mvmap.add("images", FileSystemResource(tempFile))

        val generatedImageName = generator.generateImageName()
        val newImageName = "n${serverId}_i$generatedImageName"

        val distImage = DistributedImage(originalImageName, imageType, newImageName, ownerId, malfunctionRequestId)
        mvmap.add("images_info", distImage)

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val httpEntity = HttpEntity<MultiValueMap<String, Any>>(mvmap, headers)
        val url = "http://$host/v1/api/upload_image"

        return Flowable.fromFuture(restTemplate.postForEntity(url, httpEntity, FileServerAnswer::class.java))
                .map {
                    return@map FileServerAnswerWrapper(it.body as FileServerAnswer, newImageName)
                }
    }

    override fun deleteImage(owner_id: Long, malfunctionRequestId: String, imageName: String) {

    }
}