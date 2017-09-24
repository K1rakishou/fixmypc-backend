package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.LoginRequest
import com.kirakishou.backend.fixmypc.model.net.response.LoginResponse
import com.kirakishou.backend.fixmypc.service.user.LoginService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Created by kirakishou on 7/9/2017.
 */

@Controller
@RequestMapping
class LoginController {

    @Autowired
    lateinit var loginService: LoginService

    @RequestMapping(path = arrayOf(Constant.Paths.LOGIN_CONTROLLER_PATH), method = arrayOf(RequestMethod.POST))
    fun login(@RequestBody request: LoginRequest): Single<ResponseEntity<LoginResponse>> {

        return Single.just(request)
                .map { (login, password) ->
                    val result = loginService.doLogin(login, password)

                    when (result) {
                        is LoginService.Result.Ok -> {
                            return@map ResponseEntity(LoginResponse(result.sessionId, result.accountType.value,
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is LoginService.Result.WrongLoginOrPassword -> {
                            return@map ResponseEntity(LoginResponse("", AccountType.Guest.value,
                                    ServerErrorCode.SEC_WRONG_LOGIN_OR_PASSWORD.value), HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(LoginResponse("", AccountType.Guest.value,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}