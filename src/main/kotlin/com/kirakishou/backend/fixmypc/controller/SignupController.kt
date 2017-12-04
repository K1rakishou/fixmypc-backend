package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.SignupRequest
import com.kirakishou.backend.fixmypc.model.net.response.SignupResponse
import com.kirakishou.backend.fixmypc.service.user.SignupService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Created by kirakishou on 7/15/2017.
 */

@Controller
@RequestMapping
class SignupController {

    @Autowired
    lateinit var signupService: SignupService

    @RequestMapping(path = arrayOf(Constant.Paths.SIGNUP_CONTROLLER_PATH),
            method = arrayOf(RequestMethod.POST),
            consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun signup(@RequestBody request: SignupRequest): Single<ResponseEntity<SignupResponse>> {

        return Single.just(request)
                .map {
                    val result = signupService.doSignup(request.login, request.password, AccountType.from(request.accountType))

                    when (result) {
                        is SignupService.Result.Ok -> {
                            return@map ResponseEntity(SignupResponse(ServerErrorCode.SEC_OK.value),
                                    HttpStatus.CREATED)
                        }

                        is SignupService.Result.LoginAlreadyExists -> {
                            return@map ResponseEntity(SignupResponse(ServerErrorCode.SEC_LOGIN_ALREADY_EXISTS.value),
                                    HttpStatus.CONFLICT)
                        }

                        is SignupService.Result.LoginIsTooLong -> {
                            return@map ResponseEntity(SignupResponse(ServerErrorCode.SEC_LOGIN_IS_TOO_LONG.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is SignupService.Result.LoginIsIncorrect -> {
                            return@map ResponseEntity(SignupResponse(ServerErrorCode.SEC_LOGIN_IS_INCORRECT.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is SignupService.Result.PasswordIsIncorrect -> {
                            return@map ResponseEntity(SignupResponse(ServerErrorCode.SEC_PASSWORD_IS_INCORRECT.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is SignupService.Result.AccountTypeIsIncorrect -> {
                            return@map ResponseEntity(SignupResponse(ServerErrorCode.SEC_ACCOUNT_TYPE_IS_INCORRECT.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is SignupService.Result.StoreError -> {
                            return@map ResponseEntity(SignupResponse(ServerErrorCode.SEC_STORE_ERROR.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(SignupResponse(
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}
