package com.kirakishou.backend.fixmypc.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kirakishou.backend.fixmypc.model.Constants
import com.kirakishou.backend.fixmypc.model.User
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

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
        cacheManager.setDefaultExpiration(Constants.USER_CACHE_LIFE_TIME_SECONDS)
        cacheManager.setUsePrefix(true)
        return cacheManager
    }

    /*@Bean
    open fun hikariCP(): DataSource {
        val dataSource = HikariDataSource()
        dataSource.driverClassName = "org.postgresql.Driver"
        dataSource.jdbcUrl = "jdbc:postgresql://192.168.99.100:9499/postgres"
        dataSource.username = "postgres"
        dataSource.password = "4e7d2dfx"
        //dataSource.maximumPoolSize = 128
        //dataSource.leakDetectionThreshold = 2000
        //dataSource.connectionTimeout = 15000

        return dataSource
    }*/
}