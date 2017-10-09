package com.kirakishou.backend.fixmypc.service

import io.reactivex.Flowable
import io.reactivex.Single
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface ImageService {

    interface Post {
        interface Result {
            class Ok(val imageName: String) : Result
            class CouldNotUploadImage : Result
        }
    }

    interface Get {
        interface Result {
            class Ok(val inputStream: InputStream) : Result
            class BadFileName : Result
            class BadImageType : Result
            class NotFound : Result
        }
    }

    fun uploadImage(serverHomeDirectory: String, multipartFile: MultipartFile): Flowable<Post.Result>
    fun serveImage(userId: Long, imageType: Int, imageNameParam: String, imageSizeParam: String): Single<Get.Result>
}