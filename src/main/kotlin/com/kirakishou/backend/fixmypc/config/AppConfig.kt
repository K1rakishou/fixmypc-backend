package com.kirakishou.backend.fixmypc.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.log.FileLogImpl
import com.zaxxer.hikari.HikariDataSource
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.DeploymentMode
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.web.client.AsyncRestTemplate
import redis.clients.jedis.JedisShardInfo
import javax.sql.DataSource








/**
 * Created by kirakishou on 7/9/2017.
 */

@Configuration
class AppConfig {

    @Bean
    fun provideKotlinModule() = KotlinModule()

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
    fun provideDataSource(): DataSource {
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
    fun provideJedisConnectionFactory(): JedisConnectionFactory {
        val shardInfo = JedisShardInfo("192.168.99.100", 9119)
        return JedisConnectionFactory(shardInfo)
    }

    @Bean
    fun provideRedisTemplate(): RedisTemplate<String, Long> {
        val template = RedisTemplate<String, Long>()
        template.connectionFactory = provideJedisConnectionFactory()
        template.keySerializer = JdkSerializationRedisSerializer()
        template.hashKeySerializer = JdkSerializationRedisSerializer()
        template.valueSerializer = JdkSerializationRedisSerializer()
        template.hashValueSerializer = JdkSerializationRedisSerializer()
        template.afterPropertiesSet()

        return template
    }

    @Bean
    fun provideRestTemplate(): AsyncRestTemplate {
        return AsyncRestTemplate()
    }

    @Bean
    fun provideFileLog(): FileLog {
        return FileLogImpl()
    }
}