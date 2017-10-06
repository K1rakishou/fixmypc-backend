package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.PickSpecialistRequest
import com.kirakishou.backend.fixmypc.model.net.response.SpecialistsListResponse
import com.kirakishou.backend.fixmypc.model.net.response.StatusResponse
import com.kirakishou.backend.fixmypc.service.specialist.ClientAssignSpecialistService
import com.kirakishou.backend.fixmypc.service.specialist.GetRespondedSpecialistsService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping
class SpecialistController {

    @Autowired
    lateinit var mGetRespondedSpecialistsService: GetRespondedSpecialistsService

    @Autowired
    lateinit var mClientAssignSpecialistService: ClientAssignSpecialistService

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/profile/{damage_claim_id}/{skip}/{count}"),
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
                                    HttpStatus.FORBIDDEN)
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

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/assign"),
            method = arrayOf(RequestMethod.POST))
    fun clientAssignSpecialist(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                               @RequestBody request: PickSpecialistRequest): Single<ResponseEntity<StatusResponse>> {

        return mClientAssignSpecialistService.assignSpecialist(sessionId, request.userId, request.damageClaimId)
                .map { result ->
                    when (result) {
                        is ClientAssignSpecialistService.Get.Result.Ok -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is ClientAssignSpecialistService.Get.Result.CouldNotRemoveRespondedSpecialists -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_COULD_NOT_REMOVE_RESPONDED_SPECIALISTS.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is ClientAssignSpecialistService.Get.Result.BadAccountType -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.FORBIDDEN)
                        }

                        is ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotBelongToUser -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_DAMAGE_CLAIM_DOES_NOT_BELONG_TO_USER.value),
                                    HttpStatus.FORBIDDEN)
                        }

                        is ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotExist -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_DAMAGE_CLAIM_DOES_NOT_EXIST.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is ClientAssignSpecialistService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
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
}


































