package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.FileServerAnswerWrapper
import com.kirakishou.backend.fixmypc.model.FileServerErrorCode
import io.reactivex.Flowable
import io.reactivex.Single

interface FileServerService {
    fun saveMalfunctionRequestImage(serverId: Int, host: String, tempFile: String, originalImageName: String, imageType: Int,
                                    ownerId: Long, malfunctionRequestId: String): Flowable<FileServerAnswerWrapper>

    fun deleteMalfunctionRequestImages(ownerId: Long, host: String, malfunctionRequestId: String, imageName: String): Single<FileServerErrorCode>
}