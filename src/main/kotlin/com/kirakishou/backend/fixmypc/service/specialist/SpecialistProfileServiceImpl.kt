package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.extension.limit
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.exception.*
import com.kirakishou.backend.fixmypc.model.net.request.SpecialistProfileRequest
import com.kirakishou.backend.fixmypc.model.store.SpecialistProfileStore
import com.kirakishou.backend.fixmypc.service.ImageService
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.apache.hadoop.fs.FileSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class SpecialistProfileServiceImpl : SpecialistProfileService {

    @Autowired
    private lateinit var mImageService: ImageService

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var mSpecialistProfileStore: SpecialistProfileStore

    @Autowired
    private lateinit var fs: FileSystem

    @Autowired
    private lateinit var log: FileLog

    override fun isSpecialistProfileFilledIn(sessionId: String): Single<SpecialistProfileService.Get.ResultIsFilledIn> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(SpecialistProfileService.Get.ResultIsFilledIn.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Specialist) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(SpecialistProfileService.Get.ResultIsFilledIn.BadAccountType())
        }

        val specialistProfileFickle = mSpecialistProfileStore.findOne(user.id)
        if (!specialistProfileFickle.isPresent()) {
            return Single.just(SpecialistProfileService.Get.ResultIsFilledIn.CouldNotFindClientProfile())
        }

        val profile = specialistProfileFickle.get()
        return Single.just(SpecialistProfileService.Get.ResultIsFilledIn.Ok(profile.isProfileInfoFilledIn()))
    }

    override fun getSpecialistProfile(sessionId: String): Single<SpecialistProfileService.Get.ResultProfile> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(SpecialistProfileService.Get.ResultProfile.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Specialist) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(SpecialistProfileService.Get.ResultProfile.BadAccountType())
        }

        check(user.id != -1L) { "userId should not be -1" }

        val profileFickle = mSpecialistProfileStore.findOne(user.id)
        if (!profileFickle.isPresent()) {
            log.d("Could not find specialist profile with id ${user.id}")
            return Single.just(SpecialistProfileService.Get.ResultProfile.NotFound())
        }

        val profile = profileFickle.get()
        return Single.just(SpecialistProfileService.Get.ResultProfile.Ok(profile))
    }

    override fun getSpecialistProfile(sessionId: String, specialistUserId: Long): Single<SpecialistProfileService.Get.ResultProfile> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(SpecialistProfileService.Get.ResultProfile.SessionIdExpired())
        }

        val profileFickle = mSpecialistProfileStore.findOne(specialistUserId)
        if (!profileFickle.isPresent()) {
            log.d("Could not find specialist profile with id $specialistUserId")
            return Single.just(SpecialistProfileService.Get.ResultProfile.NotFound())
        }

        return Single.just(SpecialistProfileService.Get.ResultProfile.Ok(profileFickle.get()))
    }

    override fun updateSpecialistProfile(sessionIdParam: String, requestParam: SpecialistProfileRequest):
            Single<SpecialistProfileService.Post.ResultInfo> {

        return Single.just(UpdatingProfileParams(sessionIdParam, requestParam))
                .map { (sessionId, request) ->
                    val userFickle = sessionCache.findOne(sessionId)
                    if (!userFickle.isPresent()) {
                        log.d("SessionId $sessionId was not found in the sessionCache")
                        throw SessionIdExpiredException()
                    }

                    val user = userFickle.get()
                    if (user.accountType != AccountType.Specialist) {
                        log.d("Bad accountType ${user.accountType}")
                        throw BadAccountTypeException()
                    }

                    val name = request.profileName.limit(Constant.TextLength.MAX_PROFILE_NAME_LENGTH)
                    val phone = request.profilePhone.limit(Constant.TextLength.MAX_PHONE_LENGTH)

                    check(user.id != -1L) { "userId should not be -1" }
                    val userId = user.id

                    //do not update the photo
                    if (!mSpecialistProfileStore.updateInfo(userId, name, phone)) {
                        log.d("Error while trying to update profile info in the store")
                        throw StoreErrorException()
                    }

                    return@map SpecialistProfileService.Post.ResultInfo.Ok() as SpecialistProfileService.Post.ResultInfo
                }
                .onErrorReturn { exception ->
                    return@onErrorReturn when (exception) {
                        is SessionIdExpiredException -> SpecialistProfileService.Post.ResultInfo.SessionIdExpired()
                        is BadAccountTypeException -> SpecialistProfileService.Post.ResultInfo.BadAccountType()
                        is NotFoundException -> SpecialistProfileService.Post.ResultInfo.NotFound()
                        is StoreErrorException -> SpecialistProfileService.Post.ResultInfo.RepositoryError()

                        else -> {
                            log.e(exception)
                            SpecialistProfileService.Post.ResultInfo.UnknownError()
                        }
                    }
                }
    }

    override fun updateProfilePhoto(sessionIdParam: String, profilePhotoParam: MultipartFile): Single<SpecialistProfileService.Post.ResultPhoto> {
        return Single.just(UpdateProfilePhotoParams(sessionIdParam, profilePhotoParam))
                .flatMap { (sessionId, newProfilePhoto) ->
                    val userFickle = sessionCache.findOne(sessionId)
                    if (!userFickle.isPresent()) {
                        log.d("SessionId $sessionId was not found in the sessionCache")
                        throw SessionIdExpiredException()
                    }

                    val user = userFickle.get()
                    if (user.accountType != AccountType.Specialist) {
                        log.d("Bad accountType ${user.accountType}")
                        throw BadAccountTypeException()
                    }

                    check(user.id != -1L) { "userId should not be -1" }

                    val profileFickle = mSpecialistProfileStore.findOne(user.id)
                    if (!profileFickle.isPresent()) {
                        log.d("Could not find specialist profile with id ${user.id}")
                        throw NotFoundException()
                    }

                    val profile = profileFickle.get()
                    val serverFilePath = "${fs.homeDirectory}/img/profile/${user.id}/"

                    val deleteImageSingle = if (profile.photoName.isNotEmpty()) {
                        mImageService.deleteImage(serverFilePath, profile.photoName)
                    } else {
                        Single.just(ImageService.Delete.Result.Ok())
                    }

                    return@flatMap Singles.zip(deleteImageSingle, Single.just(newProfilePhoto), Single.just(user.id))
                }
                .flatMap { (deleteResponse, newProfilePhoto, userId) ->
                    if (deleteResponse !is ImageService.Delete.Result.Ok) {
                        log.d("Error while trying to delete image")
                        throw CouldNotDeleteImageException()
                    }

                    val serverFilePath = "${fs.homeDirectory}/img/profile/$userId/"
                    val uploadingImageSingle = mImageService.uploadImage(serverFilePath, newProfilePhoto)
                            .first(ImageService.Post.Result.CouldNotUploadImage())

                    return@flatMap Singles.zip(uploadingImageSingle, Single.just(userId))
                }
                .map { (response, userId) ->
                    if (response !is ImageService.Post.Result.Ok) {
                        log.d("Error while trying to upload image")
                        throw CouldNotUploadImagesException()
                    }

                    val photoName = response.imageName

                    if (!mSpecialistProfileStore.updatePhoto(userId, photoName)) {
                        log.d("Error while trying to update profile photo in the store")
                        throw StoreErrorException()
                    }

                    return@map SpecialistProfileService.Post.ResultPhoto.Ok(photoName) as SpecialistProfileService.Post.ResultPhoto
                }
                .onErrorReturn { exception ->
                    return@onErrorReturn when (exception) {
                        is SessionIdExpiredException -> SpecialistProfileService.Post.ResultPhoto.SessionIdExpired()
                        is BadAccountTypeException -> SpecialistProfileService.Post.ResultPhoto.BadAccountType()
                        is NotFoundException -> SpecialistProfileService.Post.ResultPhoto.NotFound()
                        is CouldNotUploadImagesException -> SpecialistProfileService.Post.ResultPhoto.CouldNotUploadImage()
                        is StoreErrorException -> SpecialistProfileService.Post.ResultPhoto.StoreError()
                        is CouldNotDeleteImageException -> SpecialistProfileService.Post.ResultPhoto.CouldNotDeleteOldImage()

                        else -> {
                            log.e(exception)
                            SpecialistProfileService.Post.ResultPhoto.UnknownError()
                        }
                    }
                }
    }

    data class UpdatingProfileParams(val sessionId: String,
                                     val request: SpecialistProfileRequest)

    data class UpdateProfilePhotoParams(val sessionId: String,
                                        val profilePhoto: MultipartFile)
}





































