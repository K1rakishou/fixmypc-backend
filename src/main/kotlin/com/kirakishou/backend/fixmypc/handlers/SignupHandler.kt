package com.kirakishou.backend.fixmypc.handlers

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

class SignupHandler : WebHandler {

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(Mono.just("not implemented yet"))
    }
}