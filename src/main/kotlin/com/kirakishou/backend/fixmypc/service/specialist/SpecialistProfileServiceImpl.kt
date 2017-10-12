package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.extension.limit
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.exception.*
import com.kirakishou.backend.fixmypc.model.net.request.SpecialistProfileRequest
import com.kirakishou.backend.fixmypc.model.repository.SpecialistProfileRepository
import com.kirakishou.backend.fixmypc.model.repository.store.UserStore
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
    private lateinit var mUserStore: UserStore

    @Autowired
    private lateinit var mSpecialistProfileRepository: SpecialistProfileRepository

    @Autowired
    private lateinit var fs: FileSystem

    @Autowired
    private lateinit var log: FileLog

    override fun getProfile(sessionId: String): Single<SpecialistProfileService.Get.Result> {
        val userFickle = mUserStore.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the specialistProfileStore")
            return Single.just(SpecialistProfileService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Specialist) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(SpecialistProfileService.Get.Result.BadAccountType())
        }

        val profileFickle = mSpecialistProfileRepository.findOne(user.id)
        if (!profileFickle.isPresent()) {
            log.d("Could not find specialist profile with id ${user.id}")
            return Single.just(SpecialistProfileService.Get.Result.NotFound())
        }

        val profile = profileFickle.get()
        profile.isFilledIn = (profile.name.isNotEmpty() && profile.phone.isNotEmpty() && profile.photoName.isNotEmpty())

        return Single.just(SpecialistProfileService.Get.Result.Ok(profile))
    }

    override fun updateProfileInfo(sessionIdParam: String, requestParam: SpecialistProfileRequest):
            Single<SpecialistProfileService.Post.ResultInfo> {

        return Single.just(UpdatingProfileParams(sessionIdParam, requestParam))
                .map { (sessionId, request) ->
                    val userFickle = mUserStore.findOne(sessionId)
                    if (!userFickle.isPresent()) {
                        log.d("SessionId $sessionId was not found in the specialistProfileStore")
                        throw SessionIdExpiredException()
                    }

                    val user = userFickle.get()
                    if (user.accountType != AccountType.Specialist) {
                        log.d("Bad accountType ${user.accountType}")
                        throw BadAccountTypeException()
                    }

                    val name = request.profileName.limit(Constant.TextLength.MAX_PROFILE_NAME_LENGTH)
                    val phone = request.profilePhone.limit(Constant.TextLength.MAX_PHONE_LENGTH)
                    val userId = user.id

                    //do not update the photo
                    if (!mSpecialistProfileRepository.updateInfo(userId, name, phone)) {
                        log.d("Error while trying to update profile info in the repository")
                        throw RepositoryErrorException()
                    }

                    return@map SpecialistProfileService.Post.ResultInfo.Ok() as SpecialistProfileService.Post.ResultInfo
                }
                .onErrorReturn { exception ->
                    return@onErrorReturn when (exception) {
                        is SessionIdExpiredException -> SpecialistProfileService.Post.ResultInfo.SessionIdExpired()
                        is BadAccountTypeException -> SpecialistProfileService.Post.ResultInfo.BadAccountType()
                        is NotFoundException -> SpecialistProfileService.Post.ResultInfo.NotFound()
                        is RepositoryErrorException -> SpecialistProfileService.Post.ResultInfo.RepositoryError()

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
                    val userFickle = mUserStore.findOne(sessionId)
                    if (!userFickle.isPresent()) {
                        log.d("SessionId $sessionId was not found in the specialistProfileStore")
                        throw SessionIdExpiredException()
                    }

                    val user = userFickle.get()
                    if (user.accountType != AccountType.Specialist) {
                        log.d("Bad accountType ${user.accountType}")
                        throw BadAccountTypeException()
                    }

                    val profileFickle = mSpecialistProfileRepository.findOne(user.id)
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

                    if (!mSpecialistProfileRepository.updatePhoto(userId, photoName)) {
                        log.d("Error while trying to update profile photo in the repository")
                        throw RepositoryErrorException()
                    }

                    return@map SpecialistProfileService.Post.ResultPhoto.Ok(photoName) as SpecialistProfileService.Post.ResultPhoto
                }
                .onErrorReturn { exception ->
                    return@onErrorReturn when (exception) {
                        is SessionIdExpiredException -> SpecialistProfileService.Post.ResultPhoto.SessionIdExpired()
                        is BadAccountTypeException -> SpecialistProfileService.Post.ResultPhoto.BadAccountType()
                        is NotFoundException -> SpecialistProfileService.Post.ResultPhoto.NotFound()
                        is CouldNotUploadImagesException -> SpecialistProfileService.Post.ResultPhoto.CouldNotUploadImage()
                        is RepositoryErrorException -> SpecialistProfileService.Post.ResultPhoto.RepositoryError()
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





































