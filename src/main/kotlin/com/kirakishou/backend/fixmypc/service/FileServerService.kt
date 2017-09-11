package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.core.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswerWrapper
import io.reactivex.Flowable
import io.reactivex.Single
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity

interface FileServerService {

    fun saveDamageClaimImage(serverId: Int, host: String, tempFile: String, originalImageName: String, imageType: Int,
                             ownerId: Long, folderName: String): Flowable<FileServerAnswerWrapper>

    fun deleteDamageClaimImages(ownerId: Long, host: String, folderName: String, imageName: String): Single<FileServerErrorCode>

    fun serveDamageClaimImage(ownerId: Long, host: String, folderName: String, imageName: String,
                              imageType: Int, size: String): Single<ResponseEntity<Resource>>
}