package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
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
import java.sql.Timestamp
import java.util.*

class MalfunctionStoreImplTest {

    val store = MalfunctionStoreImpl()

    private lateinit var malfunctionStore: IMap<Long, Malfunction>

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
        malfunctionStore = hazelcast.getMap<Long, Malfunction>(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY)
        ReflectionTestUtils.setField(store, "malfunctionStore", malfunctionStore)
    }

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun testSaveOne() {
        store.clear()
        val malfunction = Malfunction(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, Timestamp(Date().time), arrayListOf())

        store.saveOne(malfunction)
        val malfunctionFromStore = store.findOne(malfunction.id)

        assertEquals(true, malfunctionFromStore.isPresent())
        assertEquals(true, malfunctionFromStore.get().id == malfunction.id)
    }

    @Test
    fun testSaveMany() {
        store.clear()
        val malfunction = Malfunction(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, Timestamp(Date().time), arrayListOf())
        val malfunction2 = Malfunction(1, 0, true, "436erydfawryu", 0, "test description2", 55.6, 44.2, Timestamp(Date().time), arrayListOf())

        store.saveMany(listOf(malfunction, malfunction2))
        val malfunctionsFromStore = store.findMany(listOf(0, 1))

        assertEquals(2, malfunctionsFromStore.size)
    }

    @Test
    fun testFindMany() {
        store.clear()

        store.saveMany(listOf(
                Malfunction(13, 0, createdOn = Timestamp(Date().time)),
                Malfunction(14, 0, createdOn = Timestamp(Date().time)),
                Malfunction(15, 0, createdOn = Timestamp(Date().time)),
                Malfunction(16, 0, createdOn = Timestamp(Date().time))))

        store.saveMany(listOf(
                Malfunction(9, 0, createdOn = Timestamp(Date().time)),
                Malfunction(10, 0, createdOn = Timestamp(Date().time)),
                Malfunction(11, 0, createdOn = Timestamp(Date().time)),
                Malfunction(12, 0, createdOn = Timestamp(Date().time))))

        store.saveMany(listOf(
                Malfunction(7, 0, createdOn = Timestamp(Date().time)),
                Malfunction(7, 0, createdOn = Timestamp(Date().time)),
                Malfunction(8, 0, createdOn = Timestamp(Date().time))))

        store.saveMany(listOf(Malfunction(6, 0, createdOn = Timestamp(Date().time))))

        store.saveMany(listOf(
                Malfunction(1, 0, createdOn = Timestamp(Date().time)),
                Malfunction(2, 0, createdOn = Timestamp(Date().time)),
                Malfunction(3, 0, createdOn = Timestamp(Date().time)),
                Malfunction(4, 0, createdOn = Timestamp(Date().time)),
                Malfunction(5, 0, createdOn = Timestamp(Date().time))))


        val result = store.findMany(listOf(1, 13, 2, 5, 6, 4, 3))

        println(result)
    }

    @Test
    fun testDeleteOne() {
        store.clear()
        val malfunction = Malfunction(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, Timestamp(Date().time), arrayListOf())

        store.saveOne(malfunction)
        val malfunctionFromStore = store.findOne(malfunction.id)

        assertEquals(true, malfunctionFromStore.isPresent())
        assertEquals(true, malfunctionFromStore.get().id == malfunction.id)

        store.deleteOne(malfunction.id)
        val malfunctionFromStore2 = store.findOne(malfunction.id)

        assertEquals(false, malfunctionFromStore2.isPresent())
    }

    @Test
    fun testDeleteMany() {
        store.clear()
        val malfunction = Malfunction(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, Timestamp(Date().time), arrayListOf())
        val malfunction2 = Malfunction(1, 0, true, "436erydfawryu", 0, "test description2", 55.6, 44.2, Timestamp(Date().time), arrayListOf())

        store.saveMany(listOf(malfunction, malfunction2))
        val malfunctionsFromStore = store.findMany(listOf(0, 1))

        assertEquals(2, malfunctionsFromStore.size)

        store.deleteMany(listOf(0, 1))
        val malfunctionsFromStore2 = store.findMany(listOf(0, 1))

        assertEquals(0, malfunctionsFromStore2.size)
    }
}




























