package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.response.ClientProfileResponse
import com.kirakishou.backend.fixmypc.service.client.ClientProfileService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
@RequestMapping
class ClientController {

    @Autowired
    private lateinit var clientProfileService: ClientProfileService

    @RequestMapping(path = arrayOf("${Constant.Paths.CLIENT_CONTROLLER_PATH}/profile/{user_id}"),
            method = arrayOf(RequestMethod.GET))
    fun getClientProfile(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                         @PathVariable("user_id") userId: Long): Single<ResponseEntity<ClientProfileResponse>> {

        return clientProfileService.getClientProfile(sessionId, userId)
                .map { result ->
                    when (result) {
                        is ClientProfileService.Get.Result.Ok -> {
                            return@map ResponseEntity(
                                    ClientProfileResponse(result.clientProfile, result.clientProfile.isProfileInfoFilledIn(),
                                            ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is ClientProfileService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(
                                    ClientProfileResponse(null, false,
                                            ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        is ClientProfileService.Get.Result.CouldNotFindProfile -> {
                            return@map ResponseEntity(
                                    ClientProfileResponse(null, false,
                                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(ClientProfileResponse(null, false,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}