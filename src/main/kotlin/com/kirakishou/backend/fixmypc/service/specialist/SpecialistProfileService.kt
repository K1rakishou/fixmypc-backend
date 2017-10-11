package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import com.kirakishou.backend.fixmypc.model.net.request.SpecialistProfileRequest
import io.reactivex.Single
import org.springframework.web.multipart.MultipartFile

interface SpecialistProfileService {

    interface Get {
        interface Result {
            class Ok(val profile: SpecialistProfile) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class NotFound : Result
        }
    }

    interface Post {
        interface ResultInfo {
            class Ok : ResultInfo
            class SessionIdExpired : ResultInfo
            class BadAccountType : ResultInfo
            class NotFound : ResultInfo
            class RepositoryError: ResultInfo
            class UnknownError : ResultInfo
        }

        interface ResultPhoto {
            class Ok(val newPhotoName: String) : ResultPhoto
            class SessionIdExpired : ResultPhoto
            class BadAccountType : ResultPhoto
            class CouldNotUploadImage : ResultPhoto
            class CouldNotDeleteOldImage : ResultPhoto
            class NotFound : ResultPhoto
            class RepositoryError: ResultPhoto
            class UnknownError : ResultPhoto
        }
    }

    fun getProfile(sessionId: String): Single<Get.Result>
    fun updateProfileInfo(sessionIdParam: String, requestParam: SpecialistProfileRequest): Single<Post.ResultInfo>
    fun updateProfilePhoto(sessionIdParam: String, profilePhotoParam: MultipartFile): Single<Post.ResultPhoto>
}