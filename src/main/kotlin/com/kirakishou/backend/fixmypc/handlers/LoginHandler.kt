package com.kirakishou.backend.fixmypc.handlers

import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.dao.UserDao
import com.kirakishou.backend.fixmypc.model.exception.DatabaseException
import com.kirakishou.backend.fixmypc.model.net.request.LoginRequest
import com.kirakishou.backend.fixmypc.model.net.response.LoginResponse
import com.kirakishou.backend.fixmypc.service.Generator
import com.kirakishou.backend.fixmypc.service.JsonConverterService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.reactive.awaitSingle
import kotlinx.coroutines.experimental.reactor.asMono
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

class LoginHandler(
        private val sessionCache: SessionCache,
        private val userDao: UserDao,
        private val jsonConverter: JsonConverterService,
        private val generator: Generator,
        private val fileLog: FileLog
) : WebHandler {

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val result = async {
            try {
                val loginRequest = request.bodyToMono(LoginRequest::class.java).awaitSingle()
                val userFickle = userDao.findOne(loginRequest.login)
                if (!userFickle.isPresent()) {
                    fileLog.d("LoginHandler: Couldn't find anything with login: ${loginRequest.login}")
                    return@async formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, LoginResponse.fail(ServerErrorCode.SEC_WRONG_LOGIN_OR_PASSWORD))
                }

                val user = userFickle.get()
                if (user.password != loginRequest.password) {
                    fileLog.d("LoginHandler: saved password is not equals entered password: ${user.password}, ${loginRequest.password}")
                    return@async formatResponse(HttpStatus.UNPROCESSABLE_ENTITY, LoginResponse.fail(ServerErrorCode.SEC_WRONG_LOGIN_OR_PASSWORD))
                }

                val sessionId = generator.generateSessionId()

                user.sessionId = sessionId
                sessionCache.saveOne(sessionId, user)

                return@async formatResponse(HttpStatus.OK, LoginResponse.success(sessionId, user.accountType))
            } catch (error: Throwable) {
                return@async handleErrors(error)
            }
        }

        return result.asMono(CommonPool)
                .flatMap { it }
    }

    private fun handleErrors(error: Throwable): Mono<ServerResponse> {
        return if (error is DatabaseException) {
            formatResponse(HttpStatus.INTERNAL_SERVER_ERROR, LoginResponse.fail(ServerErrorCode.SEC_DATABASE_ERROR))
        } else {
            fileLog.e(error)
            formatResponse(HttpStatus.INTERNAL_SERVER_ERROR, LoginResponse.fail(ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR))
        }
    }

    private fun formatResponse(httpStatus: HttpStatus, response: LoginResponse): Mono<ServerResponse> {
        val photoAnswerJson = jsonConverter.toJson(response)
        return ServerResponse.status(httpStatus).body(Mono.just(photoAnswerJson))
    }
}