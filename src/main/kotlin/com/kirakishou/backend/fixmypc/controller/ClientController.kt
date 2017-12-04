package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.ClientProfileRequest
import com.kirakishou.backend.fixmypc.model.net.response.ClientProfileResponse
import com.kirakishou.backend.fixmypc.model.net.response.IsProfileFilledInResponse
import com.kirakishou.backend.fixmypc.model.net.response.UpdateClientProfileResponse
import com.kirakishou.backend.fixmypc.service.client.ClientProfileService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
@RequestMapping
class ClientController {

    @Autowired
    private lateinit var clientProfileService: ClientProfileService

    @RequestMapping(path = arrayOf("${Constant.Paths.CLIENT_CONTROLLER_PATH}/profile"),
            method = arrayOf(RequestMethod.GET))
    fun getClientProfile(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String): Single<ResponseEntity<ClientProfileResponse>> {

        return clientProfileService.getClientProfile(sessionId)
                .map { result ->
                    when (result) {
                        is ClientProfileService.Get.ResultProfile.Ok -> {
                            return@map ResponseEntity(
                                    ClientProfileResponse(result.clientProfile, result.clientProfile.isProfileInfoFilledIn(),
                                            ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is ClientProfileService.Get.ResultProfile.BadAccountType -> {
                            return@map ResponseEntity(
                                    ClientProfileResponse(null, false,
                                            ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value), HttpStatus.FORBIDDEN)
                        }

                        is ClientProfileService.Get.ResultProfile.SessionIdExpired -> {
                            return@map ResponseEntity(
                                    ClientProfileResponse(null, false,
                                            ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        is ClientProfileService.Get.ResultProfile.CouldNotFindProfile -> {
                            return@map ResponseEntity(
                                    ClientProfileResponse(null, false,
                                            ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE.value), HttpStatus.NOT_FOUND)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(ClientProfileResponse(
                            null, false,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.CLIENT_CONTROLLER_PATH}/profile"),
            method = arrayOf(RequestMethod.POST))
    fun updateClientProfile(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                            @RequestBody request: ClientProfileRequest): Single<ResponseEntity<UpdateClientProfileResponse>> {

        return clientProfileService.updateClientProfile(sessionId, request)
                .map { result ->
                    when (result) {
                        is ClientProfileService.Post.Result.Ok -> {
                            return@map ResponseEntity(UpdateClientProfileResponse(
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is ClientProfileService.Post.Result.StoreError -> {
                            return@map ResponseEntity(UpdateClientProfileResponse(
                                    ServerErrorCode.SEC_STORE_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is ClientProfileService.Post.Result.SessionIdExpired -> {
                            return@map ResponseEntity(UpdateClientProfileResponse(
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        is ClientProfileService.Post.Result.BadAccountType -> {
                            return@map ResponseEntity(UpdateClientProfileResponse(
                                    ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value), HttpStatus.FORBIDDEN)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(UpdateClientProfileResponse(
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.CLIENT_CONTROLLER_PATH}/profile/is_filled_in"),
            method = arrayOf(RequestMethod.GET))
    fun isClientProfileFilledIn(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String):
            Single<ResponseEntity<IsProfileFilledInResponse>> {

        return clientProfileService.isClientProfileFilledIn(sessionId)
                .map { result ->
                    when (result) {
                        is ClientProfileService.Get.ResultFilledIn.Ok -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(result.isFilledIn, ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is ClientProfileService.Get.ResultFilledIn.SessionIdExpired -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(false, ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        is ClientProfileService.Get.ResultFilledIn.BadAccountType -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(false, ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.FORBIDDEN)
                        }

                        is ClientProfileService.Get.ResultFilledIn.CouldNotFindProfile -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(false, ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE.value),
                                    HttpStatus.NOT_FOUND)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(IsProfileFilledInResponse(false,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}




























