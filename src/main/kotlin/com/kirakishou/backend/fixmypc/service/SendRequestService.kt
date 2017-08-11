package com.kirakishou.backend.fixmypc.service

import io.reactivex.Flowable

interface SendRequestService {
    fun <T> sendImageRequest(serverId: Int, host: String, tempFile: String, originalFileName: String,
                             imageType: Int, ownerId: Long, responseType: Class<T>): Flowable<T>
}