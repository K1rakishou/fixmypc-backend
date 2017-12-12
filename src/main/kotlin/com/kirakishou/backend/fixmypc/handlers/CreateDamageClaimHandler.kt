package com.kirakishou.backend.fixmypc.handlers

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.dao.ClientProfileDao
import com.kirakishou.backend.fixmypc.model.dao.DamageClaimDao
import com.kirakishou.backend.fixmypc.model.dao.UserDao
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.exception.DatabaseUnknownException
import com.kirakishou.backend.fixmypc.model.exception.EmptyPacketException
import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.net.response.CreateDamageClaimResponse
import com.kirakishou.backend.fixmypc.model.store.LocationStore
import com.kirakishou.backend.fixmypc.service.Generator
import com.kirakishou.backend.fixmypc.service.ImageService
import com.kirakishou.backend.fixmypc.service.JsonConverterService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.reactive.awaitFirst
import kotlinx.coroutines.experimental.reactor.asMono
import org.apache.hadoop.fs.FileSystem
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.io.File

class CreateDamageClaimHandler(
        private val sessionCache: SessionCache,
        private val userDao: UserDao,
        private val damageClaimDao: DamageClaimDao,
        private val clientProfileDao: ClientProfileDao,
        private val locationStore: LocationStore,
        private val jsonConverter: JsonConverterService,
        private val fileLog: FileLog,
        private val fs: FileSystem,
        private val imageService: ImageService,
        private val generator: Generator
        ) : WebHandler {

    private val SESSION_ID_HEADER_NAME = "session_id"
    private val PACKET_PART_KEY = "packet"
    private val PHOTOS_PART_KEY = "photos"
    private val maxFilesCount = 4
    private val maxRequestSize: Long = (1024 * 1024) * 20
    private val maxFileSize: Long = maxRequestSize / maxFilesCount

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> {
        val result = async {
            try {
                val sessionIdHeader = serverRequest.headers().header(SESSION_ID_HEADER_NAME)
                if (sessionIdHeader.isEmpty()) {
                    return@async formatResponse(HttpStatus.BAD_REQUEST, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_NO_SESSION_HEADER))
                }

                val multiValueMap = serverRequest.body(BodyExtractors.toMultipartData()).awaitFirst()
                if (!checkMultiValueMapPart(multiValueMap, PACKET_PART_KEY)) {
                    return@async formatResponse(HttpStatus.BAD_REQUEST, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_MULTIPART_REQUEST_NO_PART))
                }

                if (!checkMultiValueMapPart(multiValueMap, PHOTOS_PART_KEY)) {
                    return@async formatResponse(HttpStatus.BAD_REQUEST, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_MULTIPART_REQUEST_NO_PART))
                }

                val packetParts = collectPacketParts(multiValueMap).awaitFirst()
                val uploadingFilesMap = savePhotosOnDiskTemporarily(multiValueMap)

                try {
                    val sessionId = sessionIdHeader[0]
                    val requestInfo = getRequestInfo(sessionId, packetParts)
                    if (requestInfo is Either.Error) {
                        return@async requestInfo.error
                    }

                    val user = (requestInfo as Either.Value).value.user
                    val packet = (requestInfo as Either.Value).value.packet
                    val fileNames = uploadingFilesMap.map { it.value.newFileName }
                    val damageClaim = DamageClaim.create(user.id, packet.category, packet.description, packet.lat, packet.lon, fileNames)

                    if (!damageClaimDao.saveOne(damageClaim)) {
                        return@async formatResponse(HttpStatus.INTERNAL_SERVER_ERROR, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
                    }

                    if (!uploadImages(user.id, uploadingFilesMap)) {
                        damageClaimDao.deleteOne(damageClaim.id)
                    }

                    //TODO: save coordinates to locationStore
                    return@async formatResponse(HttpStatus.OK, CreateDamageClaimResponse.success())

                } finally {
                    deleteTempFiles(uploadingFilesMap.map { it.value.file })
                }

            } catch (error: Throwable) {
                return@async handleErrors(error)
            }
        }

        return result
                .asMono(CommonPool)
                .flatMap { it }
    }

    private suspend fun uploadImages(userId: Long, uploadingFilesMap: Map<String, UploadingFile>): Boolean {
        val deferredUploadResponses = mutableListOf<Deferred<Boolean>>()
        val newImageNames = mutableListOf<String>()
        var isAllResponsesOk = false
        val serverImageDirPath = "${fs.homeDirectory}/img/damage_claim/$userId/"

        try {
            for ((originalImageName, uploadingFile) in uploadingFilesMap) {
                deferredUploadResponses += imageService.uploadImage(serverImageDirPath, uploadingFile.file, originalImageName, uploadingFile.newFileName)
            }

            val responses = deferredUploadResponses.map { it.await() }
            isAllResponsesOk = responses.any { !it }

            return isAllResponsesOk
        } finally {
            if (isAllResponsesOk) {
                return false
            }

            removeUploaded(newImageNames, serverImageDirPath)
        }
    }

    private suspend fun removeUploaded(newImageNames: MutableList<String>, serverImageDirPath: String) {
        val deferredDeleteResponses = mutableListOf<Deferred<Boolean>>()

        for (imageName in newImageNames) {
            deferredDeleteResponses += imageService.deleteImage(serverImageDirPath, imageName)
        }

        deferredDeleteResponses.forEach { it.await() }
    }

    private suspend fun getRequestInfo(sessionId: String, packetParts: List<DataBuffer>): Either<Mono<ServerResponse>, RequestInfo> {
        val packet = jsonConverter.fromJson<CreateDamageClaimRequest>(packetParts)
        if (!packet.isOk()) {
            return Either.Error(formatResponse(HttpStatus.BAD_REQUEST, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_BAD_REQUEST)))
        }

        val userFickle = sessionCache.findOne(sessionId)

        if (!userFickle.isPresent()) {
            fileLog.d("sessionId $sessionId was not found in the sessionRepository")
            return Either.Error(formatResponse(HttpStatus.UNAUTHORIZED, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_SESSION_ID_EXPIRED)))
        }

        val user = userFickle.get()
        val userId = user.id
        check(userId != -1L) { "userId should not be -1" }

        if (user.accountType != AccountType.Client) {
            fileLog.d("User with accountType ${user.accountType} no supposed to do this operation")
            return Either.Error(formatResponse(HttpStatus.FORBIDDEN, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_BAD_ACCOUNT_TYPE)))
        }

        val clientProfileFickle = clientProfileDao.findOne(userId)
        if (!clientProfileFickle.isPresent()) {
            //wut?
            fileLog.d("Could not find client profile with id ${user.id}")
            return Either.Error(formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE)))
        }

        val clientProfile = clientProfileFickle.get()
        if (!clientProfile.isProfileInfoFilledIn()) {
            fileLog.d("User with id ${user.id} tried to respond to damage claim with not filled in profile")
            return Either.Error(formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_PROFILE_IS_NOT_FILLED_IN)))
        }

        return Either.Value(RequestInfo(packet, user))
    }

    private fun deleteTempFiles(files: List<File>) {
        files.forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private suspend fun savePhotosOnDiskTemporarily(mvm: MultiValueMap<String, Part>): Map<String, UploadingFile> {
        if (mvm[PHOTOS_PART_KEY]!!.size > maxFilesCount) {
            fileLog.e("Too many files to upload (uploadingFiles.size > maxImagesPerRequest)")
            throw ImagesCountExceededException()
        }

        val collectedPhotosParts = mvm[PHOTOS_PART_KEY]!!
                .map(this::collectBuffersAndOriginalNames)
                .map { it.awaitFirst() }

        val totalPhotosSize = collectedPhotosParts.sumBy {
            val photoSize = it.t2.sumBy { it.readableByteCount() }
            if (photoSize > maxFileSize) {
                throw FileSizeExceededException()
            }

            return@sumBy photoSize
        }

        if (totalPhotosSize > maxRequestSize) {
            throw RequestSizeExceededException()
        }

        val uploadingFilesMap = hashMapOf<String, UploadingFile>()

        for (photoBuffersListWithName in collectedPhotosParts) {
            val outputFile = File.createTempFile("file", "tmp")
            val photoBuffersList = photoBuffersListWithName.t2
            val newImageName = generator.generateImageName()

            writeBuffersToFile(outputFile, photoBuffersList)
            uploadingFilesMap.put(photoBuffersListWithName.t1, UploadingFile(newImageName, outputFile))
        }

        return uploadingFilesMap
    }

    private fun writeBuffersToFile(tempFile: File, photoBuffersList: MutableList<DataBuffer>) {
        tempFile.outputStream().use { outputStream ->
            for (buffer in photoBuffersList) {
                buffer.asInputStream().use { inputStream ->
                    val chunkSize = inputStream.available()
                    val array = ByteArray(chunkSize)

                    //copy chunks from one stream to another
                    inputStream.read(array, 0, chunkSize)
                    outputStream.write(array, 0, chunkSize)
                }
            }
        }
    }

    private fun collectBuffersAndOriginalNames(it: Part): Mono<Tuple2<String, MutableList<DataBuffer>>> {
        val originalFileName = if (it is FilePart) {
            it.filename()
        } else {
            "123.jpg"
        }

        val collectedBuffers = it.content()
                .doOnNext { dataBuffer ->
                    if (dataBuffer.readableByteCount() == 0) {
                        throw EmptyPacketException()
                    }
                }
                .buffer()
                .single()

        return Mono.zip(Mono.just(originalFileName), collectedBuffers)
    }

    private fun collectPacketParts(mvm: MultiValueMap<String, Part>): Mono<MutableList<DataBuffer>> {
        return mvm.getFirst(PACKET_PART_KEY)!!
                .content()
                .doOnNext { dataBuffer ->
                    if (dataBuffer.readableByteCount() == 0) {
                        throw EmptyPacketException()
                    }
                }
                .buffer()
                .single()
    }

    private fun checkMultiValueMapPart(mvm: MultiValueMap<String, Part>, key: String): Boolean {
        if (!mvm.contains(key)) {
            return false
        }

        if (mvm.getFirst(key) == null) {
            return false
        }

        return true
    }

    private fun handleErrors(error: Throwable): Mono<ServerResponse> {
        return if (error is DatabaseUnknownException) {
            formatResponse(HttpStatus.INTERNAL_SERVER_ERROR, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
        } else {
            fileLog.e(error)
            formatResponse(HttpStatus.INTERNAL_SERVER_ERROR, CreateDamageClaimResponse.fail(ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR))
        }
    }

    private fun formatResponse(httpStatus: HttpStatus, response: CreateDamageClaimResponse): Mono<ServerResponse> {
        val photoAnswerJson = jsonConverter.toJson(response)
        return ServerResponse.status(httpStatus).body(Mono.just(photoAnswerJson))
    }

    data class RequestInfo(val packet: CreateDamageClaimRequest,
                           val user: User)

    data class UploadingFile(val newFileName: String,
                             val file: File)

    class ImagesCountExceededException : Exception()
    class RequestSizeExceededException : Exception()
    class FileSizeExceededException : Exception()
    class CouldNotUploadImageException : Exception()
}