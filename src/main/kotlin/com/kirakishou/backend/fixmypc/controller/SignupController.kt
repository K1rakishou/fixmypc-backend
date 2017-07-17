package com.kirakishou.backend.fixmypc.controller

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.service.SignupService
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

    @RequestMapping(path = arrayOf("/signup"), method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun signup(@RequestBody request: SignupRequest): Single<ResponseEntity<SignupResponse>> {

        return Single.just(request).map {
            val result = signupService.doSignup(request.login, request.password, request.accountType)

            when (result) {
                is SignupService.Result.Ok -> {
                    return@map ResponseEntity(SignupResponse(), HttpStatus.CREATED)
                }

                is SignupService.Result.LoginAlreadyExists -> {
                    return@map ResponseEntity(SignupResponse(), HttpStatus.CONFLICT)
                }

                is SignupService.Result.LoginIsIncorrect -> {
                    return@map ResponseEntity(SignupResponse(), HttpStatus.UNPROCESSABLE_ENTITY)
                }

                is SignupService.Result.PasswordIsIncorrect -> {
                    return@map ResponseEntity(SignupResponse(), HttpStatus.UNPROCESSABLE_ENTITY)
                }

                is SignupService.Result.AccountTypeIsIncorrect -> {
                    return@map ResponseEntity(SignupResponse(), HttpStatus.UNPROCESSABLE_ENTITY)
                }

                else -> {
                    return@map ResponseEntity(SignupResponse(), HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
        }
    }

    data class SignupRequest(@JsonProperty("login") val login: String,
                             @JsonProperty("password") val password: String,
                             @JsonProperty("account_type") val accountType: AccountType)

    class SignupResponse
}
