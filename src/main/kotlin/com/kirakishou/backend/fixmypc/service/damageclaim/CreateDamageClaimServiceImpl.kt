package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.exception.*
import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.store.DamageClaimStore
import com.kirakishou.backend.fixmypc.model.store.LocationStore
import com.kirakishou.backend.fixmypc.service.ImageService
import com.kirakishou.backend.fixmypc.util.ServerUtils
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class CreateDamageClaimServiceImpl : CreateDamageClaimService {

    @Value("\${spring.http.multipart.max-file-size}")
    private var maxFileSize: Long = 0

    @Value("\${spring.http.multipart.max-request-size}")
    private var maxRequestSize: Long = 0

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    private lateinit var fs: FileSystem

    @Autowired
    private lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var locationStore: LocationStore

    @Autowired
    private lateinit var sessionCache: SessionCache

    private val allowedExtensions = listOf("png", "jpg", "jpeg", "PNG", "JPG", "JPEG")

    override fun createDamageClaim(uploadingFiles: Array<MultipartFile>, imageType: Int,
                                   request: CreateDamageClaimRequest, sessionId: String): Single<CreateDamageClaimService.Post.Result> {

        return Single.just(Params(uploadingFiles, imageType, request, sessionId))
                .map { params ->
                    //user must re login if sessionId was removed from the specialistProfileStore
                    val userFickle = sessionCache.findOne(params.sessionId)
                    if (!userFickle.isPresent()) {
                        log.d("sessionId ${params.sessionId} was not found in the sessionRepository")
                        throw SessionIdExpiredException()
                    }

                    val user = userFickle.get()
                    val userId = user.id
                    check(userId != -1L) { "userId should not be -1" }

                    if (user.accountType != AccountType.Client) {
                        log.d("User with accountType ${user.accountType} no supposed to do this operation")
                        throw BadAccountTypeException()
                    }

                    //return error code if user somehow sent a request without any images
                    if (params.uploadingFiles.isEmpty()) {
                        log.e("No files to upload")
                        throw NoFilesToUploadException()
                    }

                    //return error code if user somehow sent more than "maxImagesPerRequest" images
                    if (params.uploadingFiles.size > Constant.DAMAGE_CLAIM_MAX_IMAGES_PER_REQUEST) {
                        log.e("Too many files to upload (uploadingFiles.size > maxImagesPerRequest)")
                        throw ImagesCountExceededException()
                    }

                    checkRequestCorrectness(params.request)
                    checkFilesSizes(params.uploadingFiles)
                    checkFileNames(params.uploadingFiles.map { it.originalFilename })

                    val damageClaim = DamageClaim(
                            userId = userId,
                            category = params.request.category,
                            description = params.request.description,
                            lat = params.request.lat,
                            lon = params.request.lon,
                            isActive = true,
                            createdOn = ServerUtils.getTimeFast(),
                            imageNamesList = mutableListOf())

                    return@map DamageClaimAndParams(damageClaim, params)
                }
                .flatMap { (damageClaim, _) ->
                    val serverFilePath = "${fs.homeDirectory}/img/damage_claim/${damageClaim.userId}/"
                    val responseList = mutableListOf<Flowable<ImageService.Post.Result>>()

                    for (uploadingFile in uploadingFiles) {
                        responseList += imageService.uploadImage(serverFilePath, uploadingFile)
                    }

                    return@flatMap Singles.zip(Flowable.merge(responseList)
                            .toList(), Single.just(damageClaim))
                }
                .map { (responseList, damageClaim) ->
                    var isAllFilesUploaded = true
                    val imagesNames = mutableListOf<String>()

                    for (response in responseList) {
                        if (response is ImageService.Post.Result.CouldNotUploadImage) {
                            isAllFilesUploaded = false
                            break
                        } else {
                            imagesNames += (response as ImageService.Post.Result.Ok).imageName
                        }
                    }

                    if (!isAllFilesUploaded) {
                        throw CouldNotUploadImagesException()
                    }

                    damageClaim.imageNamesList = imagesNames

                    if (!damageClaimStore.saveOne(damageClaim)) {
                        val serverFilePath = "${fs.homeDirectory}/img/damage_claim/${damageClaim.userId}/"
                        fs.delete(Path(serverFilePath), true)
                        throw StoreErrorException()
                    }

                    val location = LatLon(damageClaim.lat, damageClaim.lon)
                    locationStore.saveOne(location, damageClaim.id)

                    return@map CreateDamageClaimService.Post.Result.Ok() as CreateDamageClaimService.Post.Result
                }
                .onErrorReturn { exception ->
                    return@onErrorReturn when (exception) {
                        is SessionIdExpiredException -> CreateDamageClaimService.Post.Result.SessionIdExpired()
                        is NoFilesToUploadException -> CreateDamageClaimService.Post.Result.NoFilesToUpload()
                        is ImagesCountExceededException -> CreateDamageClaimService.Post.Result.ImagesCountExceeded()
                        is BadAccountTypeException -> CreateDamageClaimService.Post.Result.BadAccountType()
                        is BadFileOriginalNameException -> CreateDamageClaimService.Post.Result.BadFileOriginalName()
                        is FileSizeExceededException -> CreateDamageClaimService.Post.Result.FileSizeExceeded()
                        is RequestSizeExceededException -> CreateDamageClaimService.Post.Result.RequestSizeExceeded()
                        is CouldNotUploadImagesException -> CreateDamageClaimService.Post.Result.CouldNotUploadImages()
                        is StoreErrorException -> CreateDamageClaimService.Post.Result.StoreError()
                        else -> CreateDamageClaimService.Post.Result.UnknownError()
                    }
                }
    }

    private fun checkFileNames(originalNames: List<String>) {
        for (name in originalNames) {
            val extension = TextUtils.extractExtension(name)

            if (extension !in allowedExtensions) {
                throw BadFileOriginalNameException()
            }
        }
    }

    private fun checkFilesSizes(uploadingFiles: Array<MultipartFile>) {
        var totalSize = 0L

        for (uploadingFile in uploadingFiles) {
            if (uploadingFile.size > maxFileSize) {
                throw FileSizeExceededException()
            }

            totalSize += uploadingFile.size
        }

        if (totalSize > maxRequestSize) {
            throw RequestSizeExceededException()
        }
    }

    private fun checkRequestCorrectness(request: CreateDamageClaimRequest) {
        //do nothing for now
    }

    class Params(val uploadingFiles: Array<MultipartFile>,
                 val imageType: Int,
                 val request: CreateDamageClaimRequest,
                 val sessionId: String)

    class DamageClaimAndParams(val damageClaim: DamageClaim,
                               val params: Params) {

        operator fun component1() = damageClaim
        operator fun component2() = params
    }
}




































