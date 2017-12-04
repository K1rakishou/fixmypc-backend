package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import io.reactivex.Single
import org.springframework.web.multipart.MultipartFile

interface CreateDamageClaimService {

    interface Post {
        interface Result {
            class Ok : Result
            class BadFileOriginalName : Result
            class SessionIdExpired : Result
            class NoFilesToUpload : Result
            class ImagesCountExceeded : Result
            class BadAccountType : Result
            class FileSizeExceeded : Result
            class RequestSizeExceeded : Result
            class AllFileServersAreNotWorking : Result
            class CouldNotFindClientProfile : Result
            class ProfileIsNotFilledIn : Result
            class StoreError : Result
            class UnknownError : Result
            class CouldNotUploadImages : Result
        }
    }

    fun createDamageClaim(uploadingFiles: Array<MultipartFile>, imageType: Int, request: CreateDamageClaimRequest, sessionId: String): Single<Post.Result>
}