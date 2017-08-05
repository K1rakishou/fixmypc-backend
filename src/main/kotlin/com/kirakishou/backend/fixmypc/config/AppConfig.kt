package com.kirakishou.backend.fixmypc.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.User
import com.zaxxer.hikari.HikariDataSource
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import javax.sql.DataSource


/**
 * Created by kirakishou on 7/9/2017.
 */

@Configuration
open class AppConfig {

    @Bean
    open fun kotlinModule() = KotlinModule()

    @Bean
    open fun redisTemplate(cf: RedisConnectionFactory): RedisTemplate<String, User> {
        val redisTemplate = RedisTemplate<String, User>()
        redisTemplate.connectionFactory = cf
        return redisTemplate
    }

    @Bean
    open fun cacheManager(redisTemplate: RedisTemplate<*, *>): CacheManager {
        val cacheManager = RedisCacheManager(redisTemplate)
        cacheManager.setDefaultExpiration(Constant.USER_CACHE_LIFE_TIME_SECONDS)
        cacheManager.setUsePrefix(true)
        return cacheManager
    }

    @Bean
    open fun dataSource(): DataSource {
        val dataSource = HikariDataSource()
        dataSource.driverClassName = "org.postgresql.Driver"
        dataSource.jdbcUrl = "jdbc:postgresql://192.168.99.100:9499/postgres"
        dataSource.username = "postgres"
        dataSource.password = "4e7d2dfx"
        //dataSource.maximumPoolSize = 128
        //dataSource.leakDetectionThreshold = 2000
        //dataSource.connectionTimeout = 15000

        return dataSource
    }

    @Bean
    fun clientHttpRequestFactory(): ClientHttpRequestFactory {
        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(10000)
        clientHttpRequestFactory.setReadTimeout(10000)
        clientHttpRequestFactory.setBufferRequestBody(false)
        return clientHttpRequestFactory
    }

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.requestFactory = clientHttpRequestFactory()

        return restTemplate
    }
}