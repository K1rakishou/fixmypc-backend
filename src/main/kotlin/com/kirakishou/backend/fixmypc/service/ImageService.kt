package com.kirakishou.backend.fixmypc.service

import kotlinx.coroutines.experimental.Deferred
import java.io.File
import java.io.InputStream


interface ImageService {
    suspend fun uploadImage(serverHomeDirectory: String, imageFile: File, originalImageName: String, newImageName: String): Deferred<Boolean>
    suspend fun serveImage(userId: Long, imageType: Int, imageName: String, imageSize: String): InputStream
    suspend fun deleteImage(serverHomeDirectory: String, imageName: String): Deferred<Boolean>
}