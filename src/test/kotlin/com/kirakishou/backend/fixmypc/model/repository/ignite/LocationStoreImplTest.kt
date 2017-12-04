package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.store.LocationStoreImpl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.test.util.ReflectionTestUtils
import redis.clients.jedis.JedisShardInfo
import java.util.*

class LocationStoreImplTest {

    val locationCache = LocationStoreImpl()

    lateinit var redisTemplate: RedisTemplate<String, Long>

    private fun provideRedis(): RedisTemplate<String, Long> {
        val shardInfo = JedisShardInfo("192.168.99.100", 9119)
        val connectionFactory = JedisConnectionFactory(shardInfo)
        connectionFactory.usePool = true
        connectionFactory.afterPropertiesSet()

        val template = RedisTemplate<String, Long>()
        template.connectionFactory = connectionFactory
        template.keySerializer = JdkSerializationRedisSerializer()
        template.hashKeySerializer = JdkSerializationRedisSerializer()
        template.valueSerializer = JdkSerializationRedisSerializer()
        template.hashValueSerializer = JdkSerializationRedisSerializer()
        template.afterPropertiesSet()

        return template
    }

    @Before
    fun init() {
        redisTemplate = provideRedis()

        ReflectionTestUtils.setField(locationCache, "template", redisTemplate)
    }

    @Test
    fun testFindOneItemWithinRadius() {
        locationCache.saveOne(LatLon(44.6, 55.2), 0)

        val result = locationCache.findWithin(0, LatLon(44.6, 55.2), 1.0, 1)
        assertEquals(1, result.size)
    }

    @Test
    fun testFindManyItemsPagedWithinRadius() {
        val random = Random()

        for (i in 0 until 100) {
            val x = random.nextDouble() * 0.012
            val y = random.nextDouble() * 0.007

            locationCache.saveOne(LatLon(y, x), i.toLong())
        }

        val result = arrayListOf<List<Long>>()

        result += locationCache.findWithin(0, LatLon(0.0, 0.0), 100000.0, 5)
        result += locationCache.findWithin(5, LatLon(0.0, 0.0), 100000.0, 5)
        result += locationCache.findWithin(10, LatLon(0.0, 0.0), 100000.0, 5)
        result += locationCache.findWithin(15, LatLon(0.0, 0.0), 100000.0, 5)
        result += locationCache.findWithin(20, LatLon(0.0, 0.0), 100000.0, 5)
        result += locationCache.findWithin(25, LatLon(0.0, 0.0), 100000.0, 5)
        result += locationCache.findWithin(30, LatLon(0.0, 0.0), 100000.0, 5)
        result += locationCache.findWithin(35, LatLon(0.0, 0.0), 100000.0, 5)

        for (res in result) {
            for (id in res) {
                println("id: $id")
            }

            println()
        }
    }
}






















