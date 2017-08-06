package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.ImageInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class ForwardImagesServiceImpl : ForwardImagesService {

    @Value("\${fixmypc.backend.fileservers}")
    var fileServers: Array<String> = arrayOf()

    @Autowired
    lateinit var restTemplate: RestTemplate

    override fun forwardImages(images: Map<Int, ArrayList<ImageInfo>>): Boolean {
        for ((key, imageInfos) in images) {
            System.out.println("key: $key")

            val map = LinkedMultiValueMap<String, Any>()
            val files = ArrayList<ByteArrayResource>()

            for (imageInfo in imageInfos) {
                files.add(ByteArrayResource(imageInfo.filePart.bytes))
            }

            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA

            map.put("images", files as List<ByteArrayResource>)
            val httpEntity = HttpEntity<MultiValueMap<String, Any>>(map, headers)

            try {
                val response = restTemplate.exchange("http://" + fileServers[key] + "/v1/api/upload_image", HttpMethod.POST, httpEntity, String::class.java)
                System.err.println(response)

            } catch(e: Exception) {
                e.printStackTrace()
            }
        }

        return true
    }
}