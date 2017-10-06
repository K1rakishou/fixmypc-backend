package com.kirakishou.backend.fixmypc.service

import io.reactivex.Flowable
import org.springframework.web.multipart.MultipartFile

interface ImageService {

    interface Post {
        interface Result {
            class Ok(val imageName: String) : Result
            class CouldNotUploadImage : Result
        }
    }

    fun uploadImage(serverHomeDirectory: String, multipartFile: MultipartFile): Flowable<Post.Result>
}