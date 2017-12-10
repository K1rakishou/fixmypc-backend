package com.kirakishou.backend.fixmypc.handlers

import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.dao.UserDao
import com.kirakishou.backend.fixmypc.model.net.request.LoginRequest
import com.kirakishou.backend.fixmypc.service.Generator
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.reactive.awaitSingle
import kotlinx.coroutines.experimental.reactor.asMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class LoginHandler(
        val sessionCache: SessionCache,
        val userDao: UserDao,
        val generator: Generator
) : WebHandler {

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val result = async {
            val loginRequest = request.bodyToMono(LoginRequest::class.java).awaitSingle()

            return@async ServerResponse.ok().build()

        }

        return result.asMono(CommonPool)
                .flatMap { it }
    }
}