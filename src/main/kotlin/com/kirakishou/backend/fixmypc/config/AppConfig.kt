package com.kirakishou.backend.fixmypc.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kirakishou.backend.fixmypc.handlers.*
import com.kirakishou.backend.fixmypc.routers.Router
import com.kirakishou.backend.fixmypc.service.JsonConverterService
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions


/**
 * Created by kirakishou on 7/9/2017.
 */

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

