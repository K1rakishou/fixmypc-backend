package com.kirakishou.backend.fixmypc.service.specialist

/*
interface SpecialistProfileService {

    interface Get {
        interface ResultProfile {
            class Ok(val profile: SpecialistProfile) : ResultProfile
            class SessionIdExpired : ResultProfile
            class BadAccountType : ResultProfile
            class NotFound : ResultProfile
        }

        interface ResultIsFilledIn {
            class Ok(val isFilledIn: Boolean) : ResultIsFilledIn
            class SessionIdExpired : ResultIsFilledIn
            class BadAccountType : ResultIsFilledIn
            class CouldNotFindClientProfile : ResultIsFilledIn
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
            class StoreError : ResultPhoto
            class UnknownError : ResultPhoto
        }
    }

    fun isSpecialistProfileFilledIn(sessionId: String): Single<Get.ResultIsFilledIn>
    fun getSpecialistProfile(sessionId: String): Single<Get.ResultProfile>
    fun getSpecialistProfileById(sessionId: String, specialistUserId: Long): Single<Get.ResultProfile>
    fun updateSpecialistProfile(sessionIdParam: String, requestParam: SpecialistProfileRequest): Single<Post.ResultInfo>
    fun updateProfilePhoto(sessionIdParam: String, profilePhotoParam: MultipartFile): Single<Post.ResultPhoto>
}*/