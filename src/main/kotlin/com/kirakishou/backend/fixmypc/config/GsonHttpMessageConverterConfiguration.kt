package com.kirakishou.backend.fixmypc.config

import com.google.gson.Gson
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.GsonHttpMessageConverter


@Configuration
@ConditionalOnClass(Gson::class)
@ConditionalOnMissingClass(value = "com.fasterxml.jackson.core.JsonGenerator")
@ConditionalOnBean(Gson::class)
class GsonHttpMessageConverterConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun gsonHttpMessageConverter(gson: Gson): GsonHttpMessageConverter {
        val converter = GsonHttpMessageConverter()
        converter.gson = gson

        return converter
    }

}