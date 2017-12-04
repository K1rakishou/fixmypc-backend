package com.kirakishou.backend.fixmypc

import com.kirakishou.backend.fixmypc.config.myBeans
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.context.support.GenericApplicationContext
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.ipc.netty.http.server.HttpServer
import reactor.ipc.netty.tcp.BlockingNettyContext


class FixmypcApplication(port: Int = 8080) {
    private val logger = LoggerFactory.getLogger(FixmypcApplication::class.java)
    private val httpHandler: HttpHandler
    private val server: HttpServer
    private lateinit var nettyContext: BlockingNettyContext

    init {
        val context = GenericApplicationContext().apply {
            myBeans().initialize(this)
            refresh()
        }

        server = HttpServer.create(port)
        httpHandler = WebHttpHandlerBuilder.applicationContext(context).build()
    }

    fun start() {
        nettyContext = server.start(ReactorHttpHandlerAdapter(httpHandler))
    }

    fun startAndAwait() {
        server.startAndAwait(ReactorHttpHandlerAdapter(httpHandler), { nettyContext = it })
    }

    fun stop() {
        nettyContext.shutdown()
    }
}


fun main(args: Array<String>) {
    SpringApplication.run(FixmypcApplication::class.java, *args)
}
