package com.kirakishou.backend.fixmypc.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.log.FileLogImpl
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.serializer.MalfunctionSerializer
import com.kirakishou.backend.fixmypc.serializer.UserSerializer
import com.zaxxer.hikari.HikariDataSource
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.DeploymentMode
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.AsyncRestTemplate
import javax.sql.DataSource




/**
 * Created by kirakishou on 7/9/2017.
 */

@Configuration
class AppConfig {

    @Bean
    fun kotlinModule() = KotlinModule()

    @Bean
    fun provideHazelcast(): HazelcastInstance {
        val clientConfig = Config()
        clientConfig.networkConfig.publicAddress = "192.168.99.100:9229"

        //config.networkConfig.addAddress("192.168.99.100:9230")
        //clientConfig.networkConfig.addAddress("192.168.99.100:9229")

        clientConfig.serializationConfig.addSerializerConfig(SerializerConfig()
                .setImplementation(UserSerializer())
                .setTypeClass(User::class.java))

        clientConfig.serializationConfig.addSerializerConfig(SerializerConfig()
                .setImplementation(MalfunctionSerializer())
                .setTypeClass(Malfunction::class.java))

        val instance = Hazelcast.newHazelcastInstance(clientConfig)

        val userCacheConfig = MapConfig(Constant.HazelcastNames.USER_CACHE_KEY)
        userCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.USER_ENTRY_TTL
        userCacheConfig.backupCount = 1
        userCacheConfig.asyncBackupCount = 0

        val malfunctionCacheConfig = MapConfig(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY)
        malfunctionCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.MALFUNCTION_ENTRY_TTL
        malfunctionCacheConfig.backupCount = 1
        malfunctionCacheConfig.asyncBackupCount = 0

        val userMalfunctionStoreConfig = MapConfig(Constant.HazelcastNames.ACTIVE_USER_MALFUNCTION_KEY)
        malfunctionCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.USER_MALFUNCTION_ENTRY_TTL
        malfunctionCacheConfig.backupCount = 2
        malfunctionCacheConfig.asyncBackupCount = 0

        instance.config.mapConfigs.put(Constant.HazelcastNames.USER_CACHE_KEY, userCacheConfig)
        instance.config.mapConfigs.put(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY, malfunctionCacheConfig)
        instance.config.mapConfigs.put(Constant.HazelcastNames.ACTIVE_USER_MALFUNCTION_KEY, userMalfunctionStoreConfig)

        return instance
    }

    @Bean
    fun provideIgnite(): Ignite {
        val ipFinder = TcpDiscoveryVmIpFinder()
        ipFinder.setAddresses(arrayListOf("192.168.99.100:9339"))

        val discoSpi = TcpDiscoverySpi()
        discoSpi.ipFinder = ipFinder

        val igniteConfiguration = IgniteConfiguration()
        igniteConfiguration.discoverySpi = discoSpi
        igniteConfiguration.deploymentMode = DeploymentMode.SHARED

        return Ignition.start()
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