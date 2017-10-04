package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import com.kirakishou.backend.fixmypc.service.Generator
import com.kirakishou.backend.fixmypc.service.TempFilesService
import com.kirakishou.backend.fixmypc.util.ServerUtils
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Single
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.LocalFileSystem
import org.apache.hadoop.fs.Path
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
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
                        return@map CreateDamageClaimService.Post.Result.SessionIdExpired()
                    }

                    val user = userFickle.get()
                    val ownerId = user.id
                    //val folderName = generator.generateMalfunctionRequestId()

                    //return error code if user somehow sent a request without any images
                    if (params.uploadingFiles.isEmpty()) {
                        log.e("No files to upload")
                        return@map CreateDamageClaimService.Post.Result.NoFilesToUpload()
                    }

                    //return error code if user somehow sent more than "maxImagesPerRequest" images
                    if (params.uploadingFiles.size > Constant.DAMAGE_CLAIM_MAX_IMAGES_PER_REQUEST) {
                        log.e("Too many files to upload (uploadingFiles.size > maxImagesPerRequest)")
                        return@map CreateDamageClaimService.Post.Result.ImagesCountExceeded()
                    }

                    val requestCheckResult = checkRequestCorrectness(params.request)
                    if (requestCheckResult !is CreateDamageClaimService.Post.Result.Ok) {
                        log.e("Bad damageclaim request")
                        return@map requestCheckResult
                    }

                    //return error code if either one of the images size is bigger than "maxFileSize" or sum of images sizes bigger than "maxRequestSize"
                    val fileSizesCheckResult = checkFilesSizes(params.uploadingFiles)
                    if (fileSizesCheckResult !is CreateDamageClaimService.Post.Result.Ok) {
                        log.e("Bad size of photos")
                        return@map fileSizesCheckResult
                    }

                    val fileNamesCheckResult = checkFileNames(params.uploadingFiles.map { it.originalFilename })
                    if (fileNamesCheckResult !is CreateDamageClaimService.Post.Result.Ok) {
                        log.e("Bad file name")
                        return@map fileNamesCheckResult
                    }

                    val uploadedFiles = uploadFiles(ownerId, params.uploadingFiles)

                    if (uploadedFiles.size != params.uploadingFiles.size) {
                        return@map CreateDamageClaimService.Post.Result.CouldNotUploadImages()
                    }

                    val malfunction = DamageClaim(
                            ownerId = ownerId,
                            category = params.request.category,
                            description = params.request.description,
                            lat = params.request.lat,
                            lon = params.request.lon,
                            isActive = true,
                            photoFolder = "",
                            createdOn = ServerUtils.getTimeFast(),
                            imageNamesList = uploadedFiles)

                    if (!damageClaimRepository.saveOne(malfunction)) {
                        return@map CreateDamageClaimService.Post.Result.DatabaseError()
                    }

                    return@map CreateDamageClaimService.Post.Result.Ok()
                }
    }

    private fun uploadFiles(ownerId: Long, uploadingFiles: Array<MultipartFile>): MutableList<String> {
        val uploadedFiles = mutableListOf<String>()
        val serverFilePath = "${fs.homeDirectory}/img/$ownerId/"

        for (multipartFile in uploadingFiles) {
            try {
                val fileName = generator.generateTempFileName()
                val extension = TextUtils.extractExtension(multipartFile.originalFilename)
                val fileNameWithExtension = "$fileName.$extension"
                fs.mkdirs(Path(serverFilePath))

                val fullPath = "$serverFilePath$fileNameWithExtension"
                val outputStream = localFs.create(Path(fullPath))

                val tempFile = File.createTempFile("img", ".tmp", File("D:\\img\\tmp"))
                multipartFile.transferTo(tempFile)
                val inputStream = tempFile.inputStream()

                try {
                    IOUtils.copyLarge(inputStream, outputStream)
                } finally {
                    inputStream.close()
                    outputStream.close()
                }

                uploadedFiles.add(fileNameWithExtension)
            } catch (e: Throwable) {
                log.e(e)

                for (uploadedFile in uploadedFiles) {
                    fs.delete(Path("$serverFilePath$uploadedFile"), false)
                }

                return mutableListOf()
            }
        }

        return uploadedFiles
    }

    private fun checkFileNames(originalNames: List<String>): CreateDamageClaimService.Post.Result {
        for (name in originalNames) {
            val extension = TextUtils.extractExtension(name)

            if (extension !in allowedExtensions) {
                return CreateDamageClaimService.Post.Result.BadFileOriginalName()
            }
        }

        return CreateDamageClaimService.Post.Result.Ok()
    }

    private fun checkFilesSizes(uploadingFiles: Array<MultipartFile>): CreateDamageClaimService.Post.Result {
        var totalSize = 0L

        for (uploadingFile in uploadingFiles) {
            if (uploadingFile.size > maxFileSize) {
                return CreateDamageClaimService.Post.Result.FileSizeExceeded()
            }

            totalSize += uploadingFile.size
        }

        if (totalSize > maxRequestSize) {
            return CreateDamageClaimService.Post.Result.RequestSizeExceeded()
        }

        return CreateDamageClaimService.Post.Result.Ok()
    }

    private fun checkRequestCorrectness(request: CreateDamageClaimRequest): CreateDamageClaimService.Post.Result {
        return CreateDamageClaimService.Post.Result.Ok()
    }

    class Params(val uploadingFiles: Array<MultipartFile>,
                 val imageType: Int,
                 val request: CreateDamageClaimRequest,
                 val sessionId: String)
}




































