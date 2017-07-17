package com.kirakishou.backend.fixmypc.controller

import com.fasterxml.jackson.annotation.JsonProperty
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

    @RequestMapping(path = arrayOf("/login"), method = arrayOf(RequestMethod.POST))
    fun login(@RequestBody request: LoginRequest): Single<ResponseEntity<LoginResponse>> {

        return Single.just(request).map { (login, password) ->
            val result = measureTime {
                return@measureTime loginService.doLogin(login, password)
            }

            when (result) {
                is LoginService.Result.Ok -> {
                    return@map ResponseEntity(LoginResponse(result.sessionId), HttpStatus.OK)
                }

                is LoginService.Result.WrongLoginOrPassword -> {
                    return@map ResponseEntity(LoginResponse(""), HttpStatus.UNPROCESSABLE_ENTITY)
                }

                else -> {
                    return@map ResponseEntity(LoginResponse(""), HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
        }
    }

    fun <T> measureTime(block: () -> T): T {
        val start = System.nanoTime()
        val retVal = block()
        val diff = (System.nanoTime() - start) / 1000
        System.err.println("Block execution took: ${diff}us")

        return retVal
    }

    data class LoginRequest(@JsonProperty("login") val login: String,
                            @JsonProperty("password") val password: String)

    data class LoginResponse(val sessionId: String)
}