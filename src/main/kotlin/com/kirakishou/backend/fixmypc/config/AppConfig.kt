package com.kirakishou.backend.fixmypc.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.MapConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.HazelcastInstance
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.log.FileLogImpl
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.serializer.MalfunctionSerializer
import com.kirakishou.backend.fixmypc.serializer.UserSerializer
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.AsyncRestTemplate
import javax.sql.DataSource


/**
 * Created by kirakishou on 7/9/2017.
 */

@Configuration
open class AppConfig {

    @Bean
    fun kotlinModule() = KotlinModule()

    @Bean
    fun provideHazelcast(): HazelcastInstance {
        val config = ClientConfig()
        //config.networkConfig.addAddress("192.168.99.100:9230")
        config.networkConfig.addAddress("192.168.99.100:9229")

        config.serializationConfig.addSerializerConfig(SerializerConfig()
                .setImplementation(UserSerializer())
                .setTypeClass(User::class.java))

        config.serializationConfig.addSerializerConfig(SerializerConfig()
                .setImplementation(MalfunctionSerializer())
                .setTypeClass(Malfunction::class.java))

        val instance = HazelcastClient.newHazelcastClient(config)

        val userCacheConfig = MapConfig(Constant.HazelcastNames.USER_CACHE_KEY)
        userCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.USER_ENTRY_TTL
        userCacheConfig.backupCount = 0
        userCacheConfig.asyncBackupCount = 0

        val malfunctionCacheConfig = MapConfig(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY)
        malfunctionCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.MALFUNCTION_ENTRY_TTL
        malfunctionCacheConfig.backupCount = 0
        malfunctionCacheConfig.asyncBackupCount = 0

        instance.config.mapConfigs.put(Constant.HazelcastNames.USER_CACHE_KEY, userCacheConfig)
        instance.config.mapConfigs.put(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY, malfunctionCacheConfig)

        return instance
    }

    @Bean
    fun dataSource(): DataSource {
        val dataSource = HikariDataSource()
        dataSource.driverClassName = "org.postgresql.Driver"
        dataSource.jdbcUrl = "jdbc:postgresql://192.168.99.100:9499/postgres"
        dataSource.username = "postgres"
        dataSource.password = "4e7d2dfx"
        /*dataSource.maximumPoolSize = 128
        dataSource.leakDetectionThreshold = 2000
        dataSource.connectionTimeout = 20000*/

        return dataSource
    }

    @Bean
    fun restTemplate(): AsyncRestTemplate {
        return AsyncRestTemplate()
    }

    @Bean
    fun provideFileLog(): FileLog {
        return FileLogImpl()
    }
}