package com.kirakishou.backend.fixmypc.handlers

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

class GetSpecialistProfileHandler : WebHandler {

    override fun handle(serverRequest: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(Mono.just("not implemented yet"))
    }
}