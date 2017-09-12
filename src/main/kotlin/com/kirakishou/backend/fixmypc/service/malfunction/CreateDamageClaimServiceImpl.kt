package com.kirakishou.backend.fixmypc.service.malfunction

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.FileServerErrorCode
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.manager.FileServersManagerImpl
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswerWrapper
import com.kirakishou.backend.fixmypc.model.entity.FileServerInfo
import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import com.kirakishou.backend.fixmypc.service.FileServerService
import com.kirakishou.backend.fixmypc.service.Generator
import com.kirakishou.backend.fixmypc.service.TempFilesService
import com.kirakishou.backend.fixmypc.util.ServerUtils
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class CreateDamageClaimServiceImpl : CreateDamageClaimService {

    @Value("\${spring.http.multipart.max-file-size}")
    private var maxFileSize: Long = 0

    @Value("\${spring.http.multipart.max-request-size}")
    private var maxRequestSize: Long = 0

    @Value("\${fixmypc.backend.fileservers}")
    private var fileServerHosts: Array<String> = arrayOf()

    @Value("\${fixmypc.backend.fileserver-ping-interval}")
    private val pingInterval: Long = 0

    @Autowired
    lateinit var generator: Generator

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    private lateinit var fileServerManager: FileServersManager

    @Autowired
    private lateinit var fileServerService: FileServerService

    @Autowired
    private lateinit var tempFileService: TempFilesService

    @Autowired
    private lateinit var damageClaimRepository: DamageClaimRepository

    @Autowired
    private lateinit var userCache: UserCache

    @PostConstruct
    fun init() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()

        for (host in fileServerHosts) {
            fileServerInfoList.add(FileServerInfo(host, true))
        }

        fileServerManager.init(fileServerInfoList, TimeUnit.MINUTES.toMillis(pingInterval))
    }

    override fun createDamageClaim(uploadingFiles: Array<MultipartFile>, imageType: Int,
                                   request: CreateDamageClaimRequest, sessionId: String): Single<CreateDamageClaimService.Post.Result> {

        //user must re login if sessionId was removed from the cache
        val userFickle = userCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("sessionId $sessionId was not found in the cache")
            return Single.just(CreateDamageClaimService.Post.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        val ownerId = user.id
        val malfunctionRequestId = generator.generateMalfunctionRequestId()

        //return error code if user somehow sent a request without any images
        if (uploadingFiles.isEmpty()) {
            log.e("No files to upload")
            return Single.just(CreateDamageClaimService.Post.Result.NoFilesToUpload())
        }

        //return error code if user somehow sent more than "maxImagesPerRequest" images
        if (uploadingFiles.size > Constant.DAMAGE_CLAIM_MAX_IMAGES_PER_REQUEST) {
            log.e("Too many files to upload (uploadingFiles.size > maxImagesPerRequest)")
            return Single.just(CreateDamageClaimService.Post.Result.ImagesCountExceeded())
        }

        val requestCheckResult = checkRequestCorrectness(request)
        if (requestCheckResult !is CreateDamageClaimService.Post.Result.Ok) {
            log.e("Bad malfunction request")
            return Single.just(requestCheckResult)
        }

        //return error code if either one of the images size is bigger than "maxFileSize" or sum of images sizes bigger than "maxRequestSize"
        val fileSizesCheckResult = checkFilesSizes(uploadingFiles)
        if (fileSizesCheckResult !is CreateDamageClaimService.Post.Result.Ok) {
            log.e("Bad size of photos")
            return Single.just(fileSizesCheckResult)
        }

        val fileNamesCheckResult = checkFileNames(uploadingFiles.map { it.originalFilename })
        if (fileNamesCheckResult !is CreateDamageClaimService.Post.Result.Ok) {
            log.e("Bad file name")
            return Single.just(requestCheckResult)
        }

        //return error code if there are no working file servers
        if (!fileServerManager.isAtLeastOneServerAlive()) {
            log.e("Could not get at least one file server")
            return Single.just(CreateDamageClaimService.Post.Result.AllFileServersAreNotWorking())
        }

        //for every image try to get a working file server
        val uploadingFilesList = uploadingFiles.toList()
        val servers = fileServerManager.getServers(uploadingFilesList.size)

        check(servers.size == uploadingFiles.size) {
            "Could not get enough file servers to store images"
        }

        val zippedFilesAndServers = uploadingFiles.zip(servers)

        val resultList = Flowable.just(zippedFilesAndServers)
                .subscribeOn(Schedulers.io())
                //send all images
                .flatMap { filesAndServers ->
                    return@flatMap sendFiles(imageType, ownerId, malfunctionRequestId, filesAndServers, uploadingFiles)
                }
                //collect all responses
                .toList()
                .map { results ->
                    return@map handleResponses(results, ownerId, request, malfunctionRequestId)
                }

        return resultList
    }

    private fun handleResponses(results: List<CreateDamageClaimService.Post.Result>, ownerId: Long,
                                request: CreateDamageClaimRequest, malfunctionRequestId: String): CreateDamageClaimService.Post.Result {
        //whatever the responses are - do not forget to delete temp files
        tempFileService.deleteAllTempFiles()

        //check results for errors
        for (result in results) {
            if (result !is CreateDamageClaimService.Post.Result.AllImagesUploaded) {
                //the only possible reason for this to happen is when all file servers are down
                return result
            }
        }

        val imageNamesList = (results as ArrayList<CreateDamageClaimService.Post.Result.AllImagesUploaded>).stream()
                .flatMap { it.names.stream() }
                .map { it }
                .collect(Collectors.toList())

        val malfunction = DamageClaim(
                ownerId = ownerId,
                category = request.category,
                description = request.description,
                lat = request.lat,
                lon = request.lon,
                isActive = true,
                folderName = malfunctionRequestId,
                createdOn = ServerUtils.getTimeFast(),
                imageNamesList = imageNamesList)

        if (!damageClaimRepository.saveOne(malfunction)) {
            log.d("Failed to create malfunction (Repository error)")

            //we failed to save malfunction request in the repository, so we have to notify file servers to delete images related to the request
            for (imageName in imageNamesList) {
                val extractedImageInfo = TextUtils.parseImageName(imageName)
                val host = fileServerManager.getHostById(extractedImageInfo.serverId)

                fileServerService.deleteDamageClaimImages(ownerId, host, malfunctionRequestId, imageName)
            }

            return CreateDamageClaimService.Post.Result.DatabaseError()
        }

        log.d("Malfunction successfully created")
        return CreateDamageClaimService.Post.Result.Ok()
    }

    private fun sendFiles(imageType: Int, ownerId: Long, malfunctionRequestId: String, filesAndServers: List<Pair<MultipartFile, FileServersManagerImpl.ServerWithId>>,
                          uploadingFiles: Array<MultipartFile>): Flowable<out CreateDamageClaimService.Post.Result> {

        val responses = arrayListOf<Flowable<FileServerAnswerWrapper>>()

        //send all images and retrieve responds
        for ((multipartFile, fileServerInfo) in filesAndServers) {
            val tempFile = tempFileService.fromMultipartFile(multipartFile)
            responses += storeImage(imageType, ownerId, malfunctionRequestId, fileServerInfo, tempFile, multipartFile)
        }

        //TODO: Would be nice to get rid of the blockingGet() but dunno how to do that atm
        //we need to collect only bad responses
        val mergedResponses = Flowable.merge(responses)
                .toList()
                .blockingGet()

        val newImagesNames = mergedResponses.stream()
                .filter({
                    val errorCode = FileServerErrorCode.from(it.answer.errorCode)
                    return@filter errorCode == FileServerErrorCode.OK
                })
                .map { it.newImageName }
                .collect(Collectors.toList())

        val badResponses = mergedResponses.stream()
                .filter({
                    val errorCode = FileServerErrorCode.from(it.answer.errorCode)
                    log.d("fileServerResponseErrorCode: $errorCode")

                    return@filter errorCode != FileServerErrorCode.OK
                })
                .collect(Collectors.toList())

        //return good error code if all images were successfully stored (no bad responses)
        if (badResponses.isEmpty()) {
            log.d("No bad responses. Every image was successfully stored")
            return Flowable.just(CreateDamageClaimService.Post.Result.AllImagesUploaded(newImagesNames))
        }

        val badFiles = arrayListOf<String>()

        //collect all bad files names (that couldn't have been uploaded)
        for (resp in badResponses) {
            check(resp.answer.badPhotoNames.isNotEmpty()) {
                "File server did send bad error code, but did not send bad photos with it. Possible bad mock"
            }

            badFiles.addAll(resp.answer.badPhotoNames)
        }

        var index = 0
        log.d("badFilesCount = ${badFiles.size}")

        //try to resend images
        while (index < badFiles.size) {
            //try to get working server
            val fileServerFickle = fileServerManager.getServer()

            //if there are none - return error status
            if (!fileServerFickle.isPresent()) {
                log.e("Could not find a working file server")
                return Flowable.just(CreateDamageClaimService.Post.Result.AllFileServersAreNotWorking())
            }

            //if there are any - try to store remaining images on to them
            val badFile = badFiles[index]
            val fileServer = fileServerFickle.get()
            val multipartFile = getFileByOriginalName(uploadingFiles, badFile)
            val tempFile = tempFileService.fromMultipartFile(multipartFile)
            log.d("Trying to resend a file. fileIndex = ${index}, serverId = ${fileServer.id}")

            val response = storeImage(imageType, ownerId, malfunctionRequestId, fileServer, tempFile, multipartFile)
                    .blockingFirst()

            val errorCode = FileServerErrorCode.from(response.answer.errorCode)
            if (errorCode == FileServerErrorCode.OK) {
                log.d("The file has been successfully stored")
                newImagesNames.add(response.newImageName)
                ++index
                continue
            }

            log.d("Could not store a file. errorCode = ${errorCode}")

            //if for some reason one more servers went down - mark them as not working and repeat this loop
            if (errorCode == FileServerErrorCode.REQUEST_TIMEOUT || errorCode == FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGES) {
                fileServerManager.notWorking(fileServer.id)
            }
        }

        return Flowable.just(CreateDamageClaimService.Post.Result.AllImagesUploaded(newImagesNames))
    }

    private fun storeImage(imageType: Int, ownerId: Long, malfunctionRequestId: String, server: FileServersManagerImpl.ServerWithId,
                           tempFile: String, uploadingFile: MultipartFile): Flowable<FileServerAnswerWrapper> {

        val responseFlowable = fileServerService.saveDamageClaimImage(server.id, server.fileServerInfo.host, tempFile,
                uploadingFile.originalFilename, imageType, ownerId, malfunctionRequestId)
                //max request waiting time
                .timeout(Constant.FILE_SERVER_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .onErrorResumeNext({ error: Throwable ->
                    if (error is TimeoutException || error.cause is ConnectException) {
                        log.d("Operation was cancelled due to timeout")
                        fileServerManager.notWorking(server.id)

                        val answer = FileServerAnswerWrapper(FileServerAnswer(
                                FileServerErrorCode.REQUEST_TIMEOUT.value,
                                arrayListOf(uploadingFile.originalFilename)), "")

                        return@onErrorResumeNext Flowable.just(answer)
                    } else {
                        log.e(error)

                        val answer = FileServerAnswerWrapper((FileServerAnswer(
                                FileServerErrorCode.UNKNOWN_ERROR.value, emptyList())), "")

                        return@onErrorResumeNext Flowable.just(answer)
                    }
                })
                .doOnNext({ response ->
                    val errCode = FileServerErrorCode.from(response.answer.errorCode)

                    if (errCode == FileServerErrorCode.COULD_NOT_STORE_ONE_OR_MORE_IMAGES) {
                        fileServerManager.notWorking(server.id)
                    }
                })

        return responseFlowable
    }

    private fun getFileByOriginalName(uploadingFiles: Array<MultipartFile>, name: String): MultipartFile {
        for (file in uploadingFiles) {
            if (file.originalFilename == name) {
                return file
            }
        }

        throw IllegalStateException("Could not file $name in uploadingFiles")
    }

    private fun checkFileNames(originalNames: List<String>): CreateDamageClaimService.Post.Result {
        for (name in originalNames) {
            if (!name.endsWith(".jpg") && !name.endsWith(".png") && !name.endsWith(".jpeg")) {
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
}




































