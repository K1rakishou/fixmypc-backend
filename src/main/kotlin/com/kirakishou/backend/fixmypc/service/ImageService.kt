package com.kirakishou.backend.fixmypc.service

import io.reactivex.Flowable
import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun uploadImage(serverHomeDirectory: String, multipartFile: MultipartFile): Flowable<MutableList<String>>
}