package com.kirakishou.backend.fixmypc.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kirakishou.backend.fixmypc.handlers.*
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.log.FileLogImpl
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.cache.SessionCacheImpl
import com.kirakishou.backend.fixmypc.model.dao.UserDao
import com.kirakishou.backend.fixmypc.model.dao.UserDaoImpl
import com.kirakishou.backend.fixmypc.routers.Router
import com.kirakishou.backend.fixmypc.service.GeneratorImpl
import com.kirakishou.backend.fixmypc.service.JsonConverterService
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import javax.sql.DataSource


/**
 * Created by kirakishou on 7/9/2017.
 */

val PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME = "pgsql"

fun myBeans() = beans {
    bean<Router>()
    bean<GetClientProfileHandler>()
    bean<UpdateClientProfileHandler>()
    bean<IsClientProfileFilledInHandler>()
    bean<CreateDamageClaimHandler>()
    bean<GetDamageClaimsWithinRadiusPagedHandler>()
    bean<HasAlreadyRespondedHandler>()
    bean<RespondToDamageClaimHandler>()
    bean<GetClientDamageClaimsPagedHandler>()
    bean<ServeImageHandler>()
    bean<LoginHandler>()
    bean<SignupHandler>()
    bean<GetAllRespondedSpecialistsPagedHandler>()
    bean<MarkResponseViewedHandler>()
    bean<AssignSpecialistHandler>()
    bean<GetSpecialistProfileHandler>()
    bean<GetSpecialistProfileByIdHandler>()
    bean<UpdateSpecialistProfileHandler>()
    bean<IsSpecialistProfileFilledInHandler>()
    bean<GetAssignedSpecialistHandler>()
    bean {
        provideFileLog()
    }
    bean(PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME) {
        provideDatabaseThreadPoolDispatched()
    }
    bean<UserDao> {
        UserDaoImpl(ref(), ref(PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME), ref())
    }
    bean {
        GeneratorImpl()
    }
    bean {
        provideDataSource()
    }
    bean {
        provideIgnite()
    }
    bean<SessionCache> {
        SessionCacheImpl(ref())
    }
    bean<Gson> {
        GsonBuilder().create()
    }
    bean {
        JsonConverterService(ref())
    }
    bean("webHandler") {
        RouterFunctions.toWebHandler(ref<Router>().setUpRouter(), HandlerStrategies.builder().viewResolver(ref()).build())
    }
}

fun provideDatabaseThreadPoolDispatched(): ThreadPoolDispatcher {
    return newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "pgsql")
}

fun provideDataSource(): DataSource {
    val dataSource = HikariDataSource()
    dataSource.driverClassName = "org.postgresql.Driver"
    dataSource.jdbcUrl = "jdbc:postgresql://192.168.99.100:9499/postgres"
    dataSource.username = "postgres"
    dataSource.password = "4e7d2dfx"
    dataSource.maximumPoolSize = 1
    /*dataSource.leakDetectionThreshold = 2000
    dataSource.connectionTimeout = 20000*/

    return dataSource
}

fun provideIgnite(): Ignite {
    val ipFinder = TcpDiscoveryVmIpFinder()
    ipFinder.setAddresses(arrayListOf("192.168.99.100:9339"))

    val discoSpi = TcpDiscoverySpi()
    discoSpi.ipFinder = ipFinder

    val igniteConfiguration = IgniteConfiguration()
    //igniteConfiguration.discoverySpi = discoSpi
    //igniteConfiguration.deploymentMode = DeploymentMode.SHARED
    igniteConfiguration.metricsLogFrequency = 0


    val ignite = Ignition.start(igniteConfiguration)
    ignite.active(true)

    return ignite
}

fun provideFileLog(): FileLog {
    return FileLogImpl(true)
}

/*@Configuration
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

        //TODO:
        igniteConfiguration.persistentStoreConfiguration = PersistentStoreConfiguration()

        val ignite = Ignition.start(igniteConfiguration)
        ignite.active(true)

        return ignite
    }

    @Bean
    fun provideFileSystem(): FileSystem {
        val conf = org.apache.hadoop.conf.Configuration()
        //conf.set("fs.defaultFS", host)
        return FileSystem.newInstance(conf)
    }

    @Bean
    fun provideRedisTemplate(): RedisTemplate<String, Long> {
        val shardInfo = JedisShardInfo("192.168.99.100", 9119)
        val jedisConnectionFactory = JedisConnectionFactory(shardInfo)

        val template = RedisTemplate<String, Long>()
        template.connectionFactory = jedisConnectionFactory
        template.keySerializer = JdkSerializationRedisSerializer()
        template.hashKeySerializer = JdkSerializationRedisSerializer()
        template.valueSerializer = JdkSerializationRedisSerializer()
        template.hashValueSerializer = JdkSerializationRedisSerializer()
        template.afterPropertiesSet()

        return template
    }

    @Bean
    fun provideFileLog(): FileLog {
        return FileLogImpl(printLog)
    }
}*/

