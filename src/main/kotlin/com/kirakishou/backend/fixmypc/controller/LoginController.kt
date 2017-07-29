package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.net.StatusCode
import com.kirakishou.backend.fixmypc.model.net.request.LoginRequest
import com.kirakishou.backend.fixmypc.model.net.response.LoginResponse
import com.kirakishou.backend.fixmypc.service.LoginService
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
                            return@map ResponseEntity(LoginResponse(result.sessionId, AccountType.Client.ordinal,
                                    StatusCode.STATUS_OK.ordinal), HttpStatus.OK)
                        }

                        is LoginService.Result.WrongLoginOrPassword -> {
                            return@map ResponseEntity(LoginResponse("", AccountType.Client.ordinal,
                                    StatusCode.STATUS_WRONG_LOGIN_OR_PASSWORD.ordinal), HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        else -> {
                            return@map ResponseEntity(LoginResponse("", AccountType.Client.ordinal,
                                    StatusCode.STATUS_UNKNOWN_SERVER_ERROR.ordinal), HttpStatus.INTERNAL_SERVER_ERROR)
                        }
                    }
                }
    }
}