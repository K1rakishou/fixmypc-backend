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
    fun provideIgnite(): Ignite {
        val ipFinder = TcpDiscoveryVmIpFinder()
        ipFinder.setAddresses(arrayListOf("192.168.99.100:9339"))

        val discoSpi = TcpDiscoverySpi()
        discoSpi.ipFinder = ipFinder

        val igniteConfiguration = IgniteConfiguration()
        igniteConfiguration.discoverySpi = discoSpi
        igniteConfiguration.deploymentMode = DeploymentMode.SHARED

        Ignition.setClientMode(true)
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