package com.kirakishou.backend.fixmypc.handlers

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

interface WebHandler {
    fun handle(serverRequest: ServerRequest): Mono<ServerResponse>
}