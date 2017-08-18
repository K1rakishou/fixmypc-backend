package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.DistributedImage
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
class DistributedImageServerServiceImpl : DistributedImageServerService {

    @Autowired
    lateinit var restTemplate: AsyncRestTemplate

    @PostConstruct
    fun init() {
        restTemplate.messageConverters.add(FormHttpMessageConverter())
    }

    override fun <T> storeImage(serverId: Int, host: String, tempFile: String, originalImageName: String, newImageName: String, imageType: Int,
                                ownerId: Long, responseType: Class<T>): Flowable<T> {

        val mvmap = LinkedMultiValueMap<String, Any>()
        mvmap.add("images", FileSystemResource(tempFile))

        val distImage = DistributedImage(originalImageName, imageType, newImageName, ownerId)
        mvmap.add("images_info", distImage)

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val httpEntity = HttpEntity<MultiValueMap<String, Any>>(mvmap, headers)
        val url = "http://$host/v1/api/upload_image"

        return Flowable.fromFuture(restTemplate.postForEntity(url, httpEntity, responseType))
                .map {
                    return@map it.body
                }
    }
}