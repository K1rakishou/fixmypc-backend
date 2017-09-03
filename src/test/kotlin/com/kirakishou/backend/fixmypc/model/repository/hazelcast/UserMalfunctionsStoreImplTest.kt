package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.MultiMap
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.serializer.MalfunctionSerializer
import com.kirakishou.backend.fixmypc.serializer.UserSerializer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.springframework.test.util.ReflectionTestUtils

class UserMalfunctionsStoreImplTest {

    val store = UserMalfunctionsStoreImpl()

    private lateinit var userMalfunctionStore: MultiMap<Long, Long>

    fun provideHazelcast(): HazelcastInstance {
        val config = Config()
        config.networkConfig.publicAddress = "192.168.99.100:9229"

        //config.networkConfig.addAddress("192.168.99.100:9230")
        //clientConfig.networkConfig.addAddress("192.168.99.100:9229")

        config.serializationConfig.addSerializerConfig(SerializerConfig()
                .setImplementation(UserSerializer())
                .setTypeClass(User::class.java))

        config.serializationConfig.addSerializerConfig(SerializerConfig()
                .setImplementation(MalfunctionSerializer())
                .setTypeClass(Malfunction::class.java))

        val instance = Hazelcast.newHazelcastInstance(config)

        val userCacheConfig = MapConfig(Constant.HazelcastNames.USER_CACHE_KEY)
        userCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.USER_ENTRY_TTL
        userCacheConfig.backupCount = 1
        userCacheConfig.asyncBackupCount = 0

        val malfunctionCacheConfig = MapConfig(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY)
        malfunctionCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.MALFUNCTION_ENTRY_TTL
        malfunctionCacheConfig.backupCount = 1
        malfunctionCacheConfig.asyncBackupCount = 0

        val userMalfunctionStoreConfig = MapConfig(Constant.HazelcastNames.USER_MALFUNCTION_KEY)
        malfunctionCacheConfig.timeToLiveSeconds = Constant.HazelcastTTL.USER_MALFUNCTION_ENTRY_TTL
        malfunctionCacheConfig.backupCount = 2
        malfunctionCacheConfig.asyncBackupCount = 0

        instance.config.mapConfigs.put(Constant.HazelcastNames.USER_CACHE_KEY, userCacheConfig)
        instance.config.mapConfigs.put(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY, malfunctionCacheConfig)
        instance.config.mapConfigs.put(Constant.HazelcastNames.USER_MALFUNCTION_KEY, userMalfunctionStoreConfig)

        return instance
    }

    @Before
    fun init() {
        val hazelcast = provideHazelcast()
        userMalfunctionStore = hazelcast.getMultiMap<Long, Long>(Constant.HazelcastNames.USER_MALFUNCTION_KEY)

        ReflectionTestUtils.setField(store, "hazelcast", hazelcast)
        ReflectionTestUtils.setField(store, "userMalfunctionStore", userMalfunctionStore)
    }

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun testFindMany() {
        store.clear()

        store.saveMany(0, listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))

        val firstFive = store.findMany(0, 0, 5)
        assertEquals(5, firstFive.size)
        assertEquals(1, firstFive[0])

        val secondFive = store.findMany(0, 5, 5)
        assertEquals(5, secondFive.size)
        assertEquals(6, secondFive[0])

        val thirdSix = store.findMany(0, 10, 6)
        assertEquals(6, thirdSix.size)
        assertEquals(11, thirdSix[0])
    }

    @Test
    fun testFindAll() {
        store.clear()

        store.saveMany(0, listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))

        val values = store.findAll(0)
        assertEquals(16, values.size)
    }

    @Test
    fun testDeleteOne() {
        store.clear()

        store.saveMany(0, listOf(1, 2, 3))
        store.deleteOne(0, 1)

        assertEquals(2, store.findAll(0).size)

        store.deleteOne(0, 2)
        store.deleteOne(0, 3)
        assertEquals(0, store.findAll(0).size)
    }
}






















