package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.net.request.RespondToDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.net.response.*
import com.kirakishou.backend.fixmypc.service.damageclaim.CreateDamageClaimService
import com.kirakishou.backend.fixmypc.service.damageclaim.DamageClaimResponseService
import com.kirakishou.backend.fixmypc.service.damageclaim.GetRespondedSpecialistsService
import com.kirakishou.backend.fixmypc.service.damageclaim.GetUserDamageClaimListService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping
class DamageClaimController {

    @Autowired
    lateinit var mCreateDamageClaimService: CreateDamageClaimService

    @Autowired
    lateinit var mGetUserDamageClaimListService: GetUserDamageClaimListService

    @Autowired
    lateinit var mDamageClaimResponseService: DamageClaimResponseService

    @Autowired
    lateinit var mGetRespondedSpecialistsService: GetRespondedSpecialistsService

    @Autowired
    lateinit var log: FileLog

    @RequestMapping(path = arrayOf("${Constant.Paths.DAMAGE_CLAIM_CONTROLLER_PATH}/create"),
            method = arrayOf(RequestMethod.POST))
    fun createDamageClaim(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                          @RequestPart("photos") uploadingFiles: Array<MultipartFile>,
                          @RequestPart("request") request: CreateDamageClaimRequest,
                          @RequestPart("images_type") imagesType: Int): Single<ResponseEntity<CreateDamageClaimResponse>> {

        return mCreateDamageClaimService.createDamageClaim(uploadingFiles, imagesType, request, sessionId)
                .map { result ->
                    when (result) {
                        is CreateDamageClaimService.Post.Result.Ok -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is CreateDamageClaimService.Post.Result.SessionIdExpired -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        is CreateDamageClaimService.Post.Result.BadFileOriginalName -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_BAD_ORIGINAL_FILE_NAME.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is CreateDamageClaimService.Post.Result.NoFilesToUpload -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_NO_FILES_WERE_SELECTED_TO_UPLOAD.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is CreateDamageClaimService.Post.Result.ImagesCountExceeded -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_IMAGES_COUNT_EXCEEDED.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is CreateDamageClaimService.Post.Result.FileSizeExceeded -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_FILE_SIZE_EXCEEDED.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is CreateDamageClaimService.Post.Result.RequestSizeExceeded -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_REQUEST_SIZE_EXCEEDED.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is CreateDamageClaimService.Post.Result.AllFileServersAreNotWorking -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_ALL_FILE_SERVERS_ARE_NOT_WORKING.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is CreateDamageClaimService.Post.Result.DatabaseError -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_DATABASE_ERROR.ordinal),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is CreateDamageClaimService.Post.Result.UnknownError -> {
                            return@map ResponseEntity(CreateDamageClaimResponse(
                                    ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(CreateDamageClaimResponse(
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.DAMAGE_CLAIM_CONTROLLER_PATH}/get_within/{lat}/{lon}/{radius}/{skip}/{count}"),
            method = arrayOf(RequestMethod.GET))
    fun getDamageClaimsWithinRadiusPaged(@PathVariable("lat") lat: Double,
                                         @PathVariable("lon") lon: Double,
                                         @PathVariable("radius") radius: Double,
                                         @PathVariable("skip") skip: Long,
                                         @PathVariable("count") count: Long): Single<ResponseEntity<DamageClaimsResponse>> {

        return mGetUserDamageClaimListService.getDamageClaimsWithinRadiusPaged(lat, lon, radius, skip, count)
                .map { result ->
                    when (result) {
                        is GetUserDamageClaimListService.Get.Result.Ok -> {
                            return@map ResponseEntity(DamageClaimsResponse(result.damageClaimList,
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is GetUserDamageClaimListService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(DamageClaimsResponse(emptyList(),
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(DamageClaimsResponse(emptyList(),
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.DAMAGE_CLAIM_CONTROLLER_PATH}/respond/{damage_claim_id}"),
            method = arrayOf(RequestMethod.GET))
    fun hasAlreadyResponded(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                            @PathVariable("damage_claim_id") damageClaimId: Long): Single<ResponseEntity<HasAlreadyRespondedResponse>> {

        return mDamageClaimResponseService.hasAlreadyResponded(sessionId, damageClaimId)
                .map { result ->
                    when (result) {
                        is DamageClaimResponseService.Get.Result.Ok -> {
                            return@map ResponseEntity(HasAlreadyRespondedResponse(result.responded, ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is DamageClaimResponseService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(HasAlreadyRespondedResponse(null, ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        is DamageClaimResponseService.Get.Result.BadAccountType -> {
                            return@map ResponseEntity(HasAlreadyRespondedResponse(null, ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(HasAlreadyRespondedResponse(null,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.DAMAGE_CLAIM_CONTROLLER_PATH}/respond"),
            method = arrayOf(RequestMethod.POST))
    fun respondToDamageClaim(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                             @RequestBody request: RespondToDamageClaimRequest): Single<ResponseEntity<StatusResponse>> {

        return mDamageClaimResponseService.respondToDamageClaim(sessionId, request.damageClaimId)
                .map { result ->
                    when (result) {
                        is DamageClaimResponseService.Post.Result.Ok -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is DamageClaimResponseService.Post.Result.CouldNotRespondToDamageClaim -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_COULD_NOT_RESPOND_TO_DAMAGE_CLAIM.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is DamageClaimResponseService.Post.Result.SessionIdExpired -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        is DamageClaimResponseService.Post.Result.DamageClaimDoesNotExist -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_DAMAGE_CLAIM_DOES_NOT_EXIST.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is DamageClaimResponseService.Post.Result.DamageClaimIsNotActive -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_DAMAGE_CLAIM_IS_NOT_ACTIVE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is DamageClaimResponseService.Post.Result.BadAccountType -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(StatusResponse(
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.DAMAGE_CLAIM_CONTROLLER_PATH}/get/{damage_claim_id}/{skip}/{count}"),
            method = arrayOf(RequestMethod.GET))
    fun getAllRespondedSpecialistsPaged(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                                        @PathVariable("damage_claim_id") damageClaimId: Long,
                                        @PathVariable("skip") skip: Long,
                                        @PathVariable("count") count: Long): Single<ResponseEntity<SpecialistsListResponse>> {

        return mGetRespondedSpecialistsService.getRespondedSpecialistsPaged(sessionId, damageClaimId, skip, count)
                .map { result ->
                    when (result) {
                        is GetRespondedSpecialistsService.Get.Result.Ok -> {
                            return@map ResponseEntity(SpecialistsListResponse(result.responded,
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is GetRespondedSpecialistsService.Get.Result.BadAccountType -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is GetRespondedSpecialistsService.Get.Result.DamageClaimDoesNotExist -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_DAMAGE_CLAIM_DOES_NOT_EXIST.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is GetRespondedSpecialistsService.Get.Result.DamageClaimIsNotActive -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_DAMAGE_CLAIM_IS_NOT_ACTIVE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is GetRespondedSpecialistsService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(SpecialistsListResponse(emptyList(),
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}








































