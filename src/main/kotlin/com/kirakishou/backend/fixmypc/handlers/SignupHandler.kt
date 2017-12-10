package com.kirakishou.backend.fixmypc.handlers

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.dao.ClientProfileDao
import com.kirakishou.backend.fixmypc.model.dao.SpecialistProfileDao
import com.kirakishou.backend.fixmypc.model.dao.UserDao
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.exception.DatabaseException
import com.kirakishou.backend.fixmypc.model.net.request.SignupRequest
import com.kirakishou.backend.fixmypc.model.net.response.SignupResponse
import com.kirakishou.backend.fixmypc.service.JsonConverterService
import com.kirakishou.backend.fixmypc.util.ServerUtils
import com.kirakishou.backend.fixmypc.util.TextUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.reactive.awaitSingle
import kotlinx.coroutines.experimental.reactor.asMono
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

class SignupHandler(
        private val userDao: UserDao,
        private val clientProfileDao: ClientProfileDao,
        private val specialistProfileDao: SpecialistProfileDao,
        private val jsonConverter: JsonConverterService,
        private val fileLog: FileLog
) : WebHandler {

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val result = async {
            try {
                val signupRequest = request.bodyToMono(SignupRequest::class.java).awaitSingle()
                val checkRequestResult = checkRequest(signupRequest)
                if (checkRequestResult != null) {
                    return@async checkRequestResult
                }

                val user = userDao.findOne(signupRequest.login)
                if (user.isPresent()) {
                    return@async formatResponse(HttpStatus.CONFLICT,
                            SignupResponse.fail(ServerErrorCode.SEC_LOGIN_ALREADY_EXISTS))
                }

                val currentTime =  ServerUtils.getTimeFast()
                val newUser = User(0L, signupRequest.login, signupRequest.password,
                        AccountType.from(signupRequest.accountType))

                val saveUserResult = userDao.saveOne(newUser)
                if (!saveUserResult.first) {
                    return@async formatResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            SignupResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
                }

                if (signupRequest.accountType == AccountType.Client.value) {
                    if (!clientProfileDao.saveOne(ClientProfile(userId = saveUserResult.second,
                            registeredOn = currentTime))) {
                        return@async formatResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                SignupResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
                    }
                } else if (signupRequest.accountType == AccountType.Specialist.value) {
                    if (!specialistProfileDao.saveOne(SpecialistProfile(userId = saveUserResult.second,
                            registeredOn =currentTime))) {
                        return@async formatResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                SignupResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
                    }
                }

                return@async formatResponse(HttpStatus.OK, SignupResponse.success())
            } catch (error: Throwable) {
                return@async handleErrors(error)
            }
        }

        return result
                .asMono(CommonPool)
                .flatMap { it }
    }

    private fun checkRequest(signupRequest: SignupRequest): Mono<ServerResponse>? {
        if (!TextUtils.checkLoginCorrect(signupRequest.login)) {
            return formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, SignupResponse.fail(ServerErrorCode.SEC_LOGIN_IS_INCORRECT))
        }

        if (!TextUtils.checkLoginLenCorrect(signupRequest.login)) {
            return formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, SignupResponse.fail(ServerErrorCode.SEC_LOGIN_IS_TOO_LONG))
        }

        if (!TextUtils.checkPasswordLenCorrect(signupRequest.password)) {
            return formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, SignupResponse.fail(ServerErrorCode.SEC_PASSWORD_IS_INCORRECT))
        }

        if (!AccountType.contains(signupRequest.accountType)) {
            return formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, SignupResponse.fail(ServerErrorCode.SEC_ACCOUNT_TYPE_IS_INCORRECT))
        }

        return null
    }

    private fun handleErrors(error: Throwable): Mono<ServerResponse> {
        return if (error is DatabaseException) {
            formatResponse(HttpStatus.INTERNAL_SERVER_ERROR, SignupResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
        } else {
            fileLog.e(error)
            formatResponse(HttpStatus.INTERNAL_SERVER_ERROR, SignupResponse.fail(ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR))
        }
    }

    private fun formatResponse(httpStatus: HttpStatus, response: SignupResponse): Mono<ServerResponse> {
        val photoAnswerJson = jsonConverter.toJson(response)
        return ServerResponse.status(httpStatus).body(Mono.just(photoAnswerJson))
    }
}