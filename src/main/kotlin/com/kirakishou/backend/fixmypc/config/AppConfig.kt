package com.kirakishou.backend.fixmypc.config

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.log.FileLogImpl
import com.zaxxer.hikari.HikariDataSource
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.web.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.web.client.AsyncRestTemplate
import redis.clients.jedis.JedisShardInfo
import java.util.*
import javax.sql.DataSource



/**
 * Created by kirakishou on 7/9/2017.
 */

@Configuration
class AppConfig {

    @Value("\${fixmypc.backend.log.print_log_in_console}")
    private val printLog: Boolean = true

    @Bean
    fun customConverters(): HttpMessageConverters {
        val messageConverters = ArrayList<HttpMessageConverter<*>>()
        val gsonHttpMessageConverter = GsonHttpMessageConverter()
        messageConverters.add(gsonHttpMessageConverter)

        return HttpMessageConverters(true, messageConverters)
    }

    @Bean
    fun provideIgnite(): Ignite {
        val ipFinder = TcpDiscoveryVmIpFinder()
        ipFinder.setAddresses(arrayListOf("192.168.99.100:9339"))

        val discoSpi = TcpDiscoverySpi()
        discoSpi.ipFinder = ipFinder

        val igniteConfiguration = IgniteConfiguration()
        //igniteConfiguration.discoverySpi = discoSpi
        //igniteConfiguration.deploymentMode = DeploymentMode.SHARED
        igniteConfiguration.metricsLogFrequency = 0

        return Ignition.start(igniteConfiguration)
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
        return FileLogImpl(printLog)
    }
}