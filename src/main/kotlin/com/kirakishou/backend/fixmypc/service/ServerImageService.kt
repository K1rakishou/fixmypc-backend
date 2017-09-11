package com.kirakishou.backend.fixmypc.service

import io.reactivex.Single
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity

interface ServerImageService {

    interface Get {
        interface Result {
            class Ok(val image: ResponseEntity<Resource>) : Result
            class NotFound : Result
            class ServerIsDead : Result
        }
    }

    fun serveImage(imageName: String, size: String): Single<Get.Result>
}