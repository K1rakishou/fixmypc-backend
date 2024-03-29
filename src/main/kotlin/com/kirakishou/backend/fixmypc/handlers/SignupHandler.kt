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
import com.kirakishou.backend.fixmypc.model.exception.DatabaseUnknownException
import com.kirakishou.backend.fixmypc.model.net.request.SignupRequest
import com.kirakishou.backend.fixmypc.model.net.response.SignupResponse
import com.kirakishou.backend.fixmypc.service.JsonConverterService
import com.kirakishou.backend.fixmypc.util.ServerUtils
import com.kirakishou.backend.fixmypc.util.TextUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.reactive.awaitFirst
import kotlinx.coroutines.experimental.reactor.asMono
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import javax.sql.DataSource

class SignupHandler(
        private val hikariCP: DataSource,
        private val userDao: UserDao,
        private val clientProfileDao: ClientProfileDao,
        private val specialistProfileDao: SpecialistProfileDao,
        private val jsonConverter: JsonConverterService,
        private val fileLog: FileLog
) : WebHandler {

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> {
        val result = async {
            try {
                val requestBuffers = serverRequest.body(BodyExtractors.toDataBuffers())
                        .buffer()
                        .single()
                        .awaitFirst()

                val signupRequest = jsonConverter.fromJson<SignupRequest>(requestBuffers)
                val checkRequestResult = checkRequest(signupRequest)
                if (checkRequestResult != null) {
                    return@async checkRequestResult
                }

                val currentTime = ServerUtils.getTimeFast()
                var saveUserResult: Pair<Boolean, Long>? = null

                val errorResponse = userDao.databaseRequest<Mono<ServerResponse>>(hikariCP.connection) { connection ->
                    val user = userDao.findOne(signupRequest.login, connection)
                    if (user.isPresent()) {
                        return@databaseRequest formatResponse(HttpStatus.CONFLICT,
                                SignupResponse.fail(ServerErrorCode.SEC_LOGIN_ALREADY_EXISTS))
                    }

                    val newUser = User(0L, signupRequest.login, signupRequest.password,
                            AccountType.from(signupRequest.accountType))

                    saveUserResult = userDao.saveOne(newUser, connection)
                    if (!saveUserResult!!.first) {
                        return@databaseRequest formatResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                SignupResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
                    }

                    return@databaseRequest null
                }

                if (errorResponse != null) {
                    return@async errorResponse
                }

                if (signupRequest.accountType == AccountType.Client.value) {
                    if (!clientProfileDao.saveOne(ClientProfile(userId = saveUserResult!!.second,
                            registeredOn = currentTime))) {
                        return@async formatResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                SignupResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
                    }
                } else if (signupRequest.accountType == AccountType.Specialist.value) {
                    if (!specialistProfileDao.saveOne(SpecialistProfile(userId = saveUserResult!!.second,
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
        return if (error is DatabaseUnknownException) {
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