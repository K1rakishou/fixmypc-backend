package com.kirakishou.backend.fixmypc.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kirakishou.backend.fixmypc.handlers.*
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.log.FileLogImpl
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.cache.SessionCacheImpl
import com.kirakishou.backend.fixmypc.model.dao.*
import com.kirakishou.backend.fixmypc.model.store.LocationStoreImpl
import com.kirakishou.backend.fixmypc.routers.Router
import com.kirakishou.backend.fixmypc.service.GeneratorImpl
import com.kirakishou.backend.fixmypc.service.ImageService
import com.kirakishou.backend.fixmypc.service.ImageServiceImpl
import com.kirakishou.backend.fixmypc.service.JsonConverterService
import com.samskivert.mustache.Mustache
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import org.apache.hadoop.fs.FileSystem
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader
import org.springframework.boot.web.reactive.result.view.MustacheViewResolver
import org.springframework.context.support.beans
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import redis.clients.jedis.JedisShardInfo
import javax.sql.DataSource


/**
 * Created by kirakishou on 7/9/2017.
 */

val PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME = "pgsql"
val HADOOP_THREAD_POOL_DISPATCHER_BEAN_NAME = "hadoop"

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
    bean(HADOOP_THREAD_POOL_DISPATCHER_BEAN_NAME) {
        provideHadoopThreadPoolDispatcher()
    }
    bean<UserDao> {
        UserDaoImpl(ref(PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME), ref())
    }
    bean<ClientProfileDao> {
        ClientProfileDaoImpl(ref(), ref(PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME), ref())
    }
    bean<SpecialistProfileDao> {
        SpecialistProfileDaoImpl(ref(), ref(PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME), ref())
    }
    bean<DamageClaimDao> {
        DamageClaimDaoImpl(ref(PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME), ref())
    }
    bean<ImageService> {
        ImageServiceImpl(ref(), ref(), ref(HADOOP_THREAD_POOL_DISPATCHER_BEAN_NAME))
    }
    bean {
        LocationStoreImpl(ref(), ref(), ref(), ref())
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
    bean {
        provideFileSystem()
    }
    bean {
        provideRedisTemplate()
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
    bean {
        val prefix = "classpath:/templates/"
        val suffix = ".mustache"
        val loader = MustacheResourceTemplateLoader(prefix, suffix)
        MustacheViewResolver(Mustache.compiler().withLoader(loader)).apply {
            setPrefix(prefix)
            setSuffix(suffix)
        }
    }
}

fun provideDatabaseThreadPoolDispatched(): ThreadPoolDispatcher {
    return newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors() / 2, PGSQL_THREAD_POOL_DISPATCHED_BEAN_NAME)
}

fun provideHadoopThreadPoolDispatcher(): ThreadPoolDispatcher {
    return newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors() / 2, HADOOP_THREAD_POOL_DISPATCHER_BEAN_NAME)
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

fun provideFileSystem(): FileSystem {
    val conf = org.apache.hadoop.conf.Configuration()
    //conf.set("fs.defaultFS", host)
    return FileSystem.newInstance(conf)
}

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

fun provideFileLog(): FileLog {
    return FileLogImpl(true)
}

