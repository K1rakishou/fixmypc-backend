package com.kirakishou.backend.fixmypc.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kirakishou.backend.fixmypc.log.FileLogImpl
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.User
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.client.AsyncRestTemplate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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
    fun restTemplate(): AsyncRestTemplate {
        val restTemplate = AsyncRestTemplate()
        return restTemplate
    }

    @Bean
    fun provideFileLog(): FileLogImpl {
        return FileLogImpl()
    }

    @Bean
    @Qualifier("file_server_executor")
    fun proovideFileServerExecutor(): ExecutorService {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }
}