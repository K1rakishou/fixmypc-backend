package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import com.kirakishou.backend.fixmypc.service.Generator
import com.kirakishou.backend.fixmypc.service.ImageService
import com.kirakishou.backend.fixmypc.service.TempFilesService
import com.kirakishou.backend.fixmypc.util.ServerUtils
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.LocalFileSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import javax.annotation.PostConstruct

@Component
class CreateDamageClaimServiceImpl : CreateDamageClaimService {

    @Value("\${spring.http.multipart.max-file-size}")
    private var maxFileSize: Long = 0

    @Value("\${spring.http.multipart.max-request-size}")
    private var maxRequestSize: Long = 0

    @Value("\${fixmypc.backend.images.temp-dir}")
    private lateinit var tempImagesDir: String

    private val tempFilesDir = "D:/img/tmp"
    private val imagesDir = "D:/img/images"

    @Autowired
    lateinit var generator: Generator

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    private lateinit var fs: FileSystem

    @Autowired
    private lateinit var localFs: FileSystem

    @Autowired
    private lateinit var tempFileService: TempFilesService

    @Autowired
    private lateinit var damageClaimRepository: DamageClaimRepository

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var userCache: UserCache

    private val allowedExtensions = listOf("png", "jpg", "jpeg", "PNG", "JPG", "JPEG")

    @PostConstruct
    fun init() {
        localFs = LocalFileSystem.newInstance(Configuration())
        localFs.setVerifyChecksum(false)
        localFs.setWriteChecksum(false)
    }

    override fun createDamageClaim(uploadingFiles: Array<MultipartFile>, imageType: Int,
                                   request: CreateDamageClaimRequest, sessionId: String): Single<CreateDamageClaimService.Post.Result> {

        return Single.just(Params(uploadingFiles, imageType, request, sessionId))
                .map { params ->
                    //user must re login if sessionId was removed from the cache
                    val userFickle = userCache.findOne(params.sessionId)
                    if (!userFickle.isPresent()) {
                        log.d("sessionId ${params.sessionId} was not found in the cache")
                        //return@map CreateDamageClaimService.Post.Result.SessionIdExpired()
                        throw SessionIdExpiredException()
                    }

                    val user = userFickle.get()
                    val ownerId = user.id

                    if (user.accountType != AccountType.Client) {
                        log.d("User with accountType ${user.accountType} no supposed to do this operation")
                        throw BadAccountTypeException()
                    }

                    //return error code if user somehow sent a request without any images
                    if (params.uploadingFiles.isEmpty()) {
                        log.e("No files to upload")
                        //return@map CreateDamageClaimService.Post.Result.NoFilesToUpload()
                        throw NoFilesToUploadException()
                    }

                    //return error code if user somehow sent more than "maxImagesPerRequest" images
                    if (params.uploadingFiles.size > Constant.DAMAGE_CLAIM_MAX_IMAGES_PER_REQUEST) {
                        log.e("Too many files to upload (uploadingFiles.size > maxImagesPerRequest)")
                        //return@map CreateDamageClaimService.Post.Result.ImagesCountExceeded()
                        throw ImagesCountExceededException()
                    }

                    checkRequestCorrectness(params.request)
                    checkFilesSizes(params.uploadingFiles)
                    checkFileNames(params.uploadingFiles.map { it.originalFilename })

                    val damageClaim = DamageClaim(
                            ownerId = ownerId,
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
                    val serverFilePath = "${fs.homeDirectory}/img/${damageClaim.ownerId}/"
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

                    if (!damageClaimRepository.saveOne(damageClaim)) {
                        throw RepositoryErrorException()
                    }

                    return@map CreateDamageClaimService.Post.Result.Ok()
                }
    }

    private fun checkFileNames(originalNames: List<String>) {
        for (name in originalNames) {
            val extension = TextUtils.extractExtension(name)

            if (extension !in allowedExtensions) {
                //return CreateDamageClaimService.Post.Result.BadFileOriginalName()
                throw BadFileOriginalNameException()
            }
        }
    }

    private fun checkFilesSizes(uploadingFiles: Array<MultipartFile>) {
        var totalSize = 0L

        for (uploadingFile in uploadingFiles) {
            if (uploadingFile.size > maxFileSize) {
                //return CreateDamageClaimService.Post.Result.FileSizeExceeded()
                throw FileSizeExceededException()
            }

            totalSize += uploadingFile.size
        }

        if (totalSize > maxRequestSize) {
            //return CreateDamageClaimService.Post.Result.RequestSizeExceeded()
            throw RequestSizeExceededException()
        }
    }

    private fun checkRequestCorrectness(request: CreateDamageClaimRequest) {
        //do nothing for now
    }

    class SessionIdExpiredException : Exception()
    class NoFilesToUploadException : Exception()
    class ImagesCountExceededException : Exception()
    class BadAccountTypeException : Exception()
    class BadFileOriginalNameException: Exception()
    class FileSizeExceededException : Exception()
    class RequestSizeExceededException : Exception()
    class CouldNotUploadImagesException : Exception()
    class RepositoryErrorException : Exception()

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




































