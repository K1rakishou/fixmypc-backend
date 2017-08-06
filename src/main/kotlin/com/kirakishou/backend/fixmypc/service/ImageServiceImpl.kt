package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.util.Util
import org.springframework.stereotype.Component

@Component
class ImageServiceImpl : ImageService {

    override fun getImageMd5(rawImage: ByteArray): ByteArray {
        return Util.md5(rawImage)
    }
}