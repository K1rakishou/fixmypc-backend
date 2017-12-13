package com.kirakishou.backend.fixmypc.handlers

import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.dao.UserDao
import com.kirakishou.backend.fixmypc.model.exception.DatabaseUnknownException
import com.kirakishou.backend.fixmypc.model.net.request.LoginRequest
import com.kirakishou.backend.fixmypc.model.net.response.LoginResponse
import com.kirakishou.backend.fixmypc.service.Generator
import com.kirakishou.backend.fixmypc.service.JsonConverterService
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

class LoginHandler(
        private val hikariCP: DataSource,
        private val sessionCache: SessionCache,
        private val userDao: UserDao,
        private val jsonConverter: JsonConverterService,
        private val generator: Generator,
        private val fileLog: FileLog
) : WebHandler {

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> {
        val result = async {
            try {
                val requestBuffers = serverRequest.body(BodyExtractors.toDataBuffers())
                        .buffer()
                        .single()
                        .awaitFirst()

                val loginRequest = jsonConverter.fromJson<LoginRequest>(requestBuffers)
                val userFickle = userDao.databaseRequest(hikariCP.connection) { connection ->
                   userDao.findOne(loginRequest.login, connection)
                }!!

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
        return if (error is DatabaseUnknownException) {
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