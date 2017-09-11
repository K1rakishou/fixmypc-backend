package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.entity.DistributedImage
import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswerWrapper
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.AsyncRestTemplate
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class FileServerServiceImpl : FileServerService {

    @Autowired
    private lateinit var generator: Generator

    @Autowired
    private lateinit var restTemplate: AsyncRestTemplate

    @PostConstruct
    fun init() {
        restTemplate.messageConverters.add(FormHttpMessageConverter())
        restTemplate.messageConverters.add(BufferedImageHttpMessageConverter())
    }

    override fun saveDamageClaimImage(serverId: Int, host: String, tempFile: String, originalImageName: String, imageType: Int,
                                      ownerId: Long, folderName: String): Flowable<FileServerAnswerWrapper> {

        val mvmap = LinkedMultiValueMap<String, Any>()
        mvmap.add("image", FileSystemResource(tempFile))

        val generatedImageName = generator.generateImageName()
        val newImageName = "n${serverId}_i$generatedImageName"
        val extension = TextUtils.extractExtension(originalImageName)
        val newImageNameWithExtension = "$newImageName.$extension"

        val distImage = DistributedImage(originalImageName, imageType, newImageName, ownerId, folderName)
        mvmap.add("image_info", distImage)

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val httpEntity = HttpEntity<MultiValueMap<String, Any>>(mvmap, headers)
        val url = String.format(Constant.Url.SAVE_DAMAGE_CLAIM_IMAGE_URL, host)

        return Flowable.fromFuture(restTemplate.postForEntity(url, httpEntity, FileServerAnswer::class.java))
                .map {
                    return@map FileServerAnswerWrapper(it.body as FileServerAnswer, newImageNameWithExtension)
                }
    }

    override fun deleteDamageClaimImages(ownerId: Long, host: String, folderName: String, imageName: String): Single<FileServerErrorCode> {
        val url = String.format(Constant.Url.DELETE_DAMAGE_CLAIM_IMAGES_URL, host, ownerId, folderName)

        return Single.fromFuture(restTemplate.delete(url))
                .map { errorCode ->
                    val errCode = errorCode as Int
                    return@map FileServerErrorCode.from(errCode)
                }
    }

    override fun serveDamageClaimImage(ownerId: Long, host: String, folderName: String, imageName: String,
                                       imageType: Int, size: String): Single<ResponseEntity<Resource>> {
        val url = String.format(Constant.Url.GET_DAMAGE_CLAIM_IMAGE_URL, host, imageType, ownerId, folderName, imageName, size)

        return Single.fromFuture(restTemplate.getForEntity(url, Resource::class.java))
                .timeout(Constant.FILE_SERVER_REQUEST_TIMEOUT, TimeUnit.SECONDS)
    }
}