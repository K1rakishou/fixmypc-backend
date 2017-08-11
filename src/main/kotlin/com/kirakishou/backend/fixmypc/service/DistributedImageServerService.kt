package com.kirakishou.backend.fixmypc.service

import io.reactivex.Flowable

interface DistributedImageServerService {
    fun <T> storeImage(serverId: Int, host: String, tempFile: String, originalFileName: String,
                       imageType: Int, ownerId: Long, responseType: Class<T>): Flowable<T>
}