package com.kirakishou.backend.fixmypc.service

interface ImageService {
    fun getImageMd5(rawImage: ByteArray): ByteArray
}