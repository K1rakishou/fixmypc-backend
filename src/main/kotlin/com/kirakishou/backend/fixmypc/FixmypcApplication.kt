package com.kirakishou.backend.fixmypc

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration

@SpringBootApplication
@EnableAutoConfiguration(exclude = arrayOf(JacksonAutoConfiguration::class))
class FixmypcApplication

fun main(args: Array<String>) {
    SpringApplication.run(FixmypcApplication::class.java, *args)
}
