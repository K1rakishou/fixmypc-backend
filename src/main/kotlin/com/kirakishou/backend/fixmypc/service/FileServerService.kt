package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.FileServerAnswerWrapper
import io.reactivex.Flowable

interface FileServerService {
    fun storeImage(serverId: Int, host: String, tempFile: String, originalImageName: String, imageType: Int,
                       ownerId: Long, malfunctionRequestId: String): Flowable<FileServerAnswerWrapper>

    fun deleteImage(owner_id: Long, malfunctionRequestId: String, imageName: String)
}