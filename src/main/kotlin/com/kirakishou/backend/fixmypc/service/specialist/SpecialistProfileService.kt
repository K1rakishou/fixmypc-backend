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
        interface Result {
            class Ok(val newPhotoName: String) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class NotFound : Result
            class CouldNotUploadImage : Result
            class CouldNotDeleteOldImage : Result
            class RepositoryError: Result
            class UnknownError : Result
        }
    }

    fun getProfile(sessionId: String): Single<Get.Result>
    fun updateProfile(sessionId: String, profilePhoto: MultipartFile, request: SpecialistProfileRequest): Single<Post.Result>
}