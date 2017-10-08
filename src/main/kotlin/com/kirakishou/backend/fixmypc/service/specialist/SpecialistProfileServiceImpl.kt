package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.extension.crop
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.exception.*
import com.kirakishou.backend.fixmypc.model.net.request.SpecialistProfileRequest
import com.kirakishou.backend.fixmypc.model.repository.SpecialistProfileRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
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
    private lateinit var mUserCache: UserCache

    @Autowired
    private lateinit var mSpecialistProfileRepository: SpecialistProfileRepository

    @Autowired
    private lateinit var fs: FileSystem

    @Autowired
    private lateinit var log: FileLog

    override fun getProfile(sessionId: String): Single<SpecialistProfileService.Get.Result> {
        val userFickle = mUserCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the cache")
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

    override fun updateProfile(sessionId: String, profilePhoto: MultipartFile, request: SpecialistProfileRequest):
            Single<SpecialistProfileService.Post.Result> {

        return Single.just(UpdatingProfileParams(sessionId, profilePhoto, request))
                .flatMap { params ->
                    val userFickle = mUserCache.findOne(sessionId)
                    if (!userFickle.isPresent()) {
                        log.d("SessionId $sessionId was not found in the cache")
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

                    val serverFilePath = "${fs.homeDirectory}/img/profile/${user.id}/"
                    val profile = profileFickle.get()

                    //TODO: delete old profile image before uploading a new one

                    val uploadingImageSingle = mImageService.uploadImage(serverFilePath, profilePhoto)
                            .first(ImageService.Post.Result.CouldNotUploadImage())

                    return@flatMap Singles.zip(uploadingImageSingle, Single.just(params), Single.just(user.id))
                }
                .map { (response, params, userId) ->
                    if (response !is ImageService.Post.Result.Ok) {
                        log.d("Error while trying to upload image")
                        throw CouldNotUploadImagesException()
                    }

                    val profileRequest = params.request
                    val name = profileRequest.profileName.crop(Constant.TextLength.MAX_PROFILE_NAME_LENGTH)
                    val phone = profileRequest.profilePhone.crop(Constant.TextLength.MAX_PHONE_LENGTH)
                    val photoName = response.imageName

                    if (!mSpecialistProfileRepository.update(userId, name, phone, photoName)) {
                        //TODO delete photo
                        log.d("Error while trying to update profile in the repository")
                        throw RepositoryErrorException()
                    }

                    return@map SpecialistProfileService.Post.Result.Ok() as SpecialistProfileService.Post.Result
                }
                .onErrorReturn { exception ->
                    return@onErrorReturn when (exception) {
                        is SessionIdExpiredException -> SpecialistProfileService.Post.Result.SessionIdExpired()
                        is BadAccountTypeException -> SpecialistProfileService.Post.Result.BadAccountType()
                        is NotFoundException -> SpecialistProfileService.Post.Result.NotFound()
                        is CouldNotUploadImagesException -> SpecialistProfileService.Post.Result.CouldNotUploadImage()
                        is RepositoryErrorException -> SpecialistProfileService.Post.Result.RepositoryError()
                        
                        else -> {
                            log.e(exception)
                            SpecialistProfileService.Post.Result.UnknownError()
                        }
                    }
                }
    }

    data class UpdatingProfileParams(val sessionId: String,
                                     val profilePhoto: MultipartFile,
                                     val request: SpecialistProfileRequest)
}





































