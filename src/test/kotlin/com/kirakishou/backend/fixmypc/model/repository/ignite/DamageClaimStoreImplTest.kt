package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.util.ServerUtils
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.springframework.test.util.ReflectionTestUtils
import javax.cache.expiry.Duration

class DamageClaimStoreImplTest {

    val cache = DamageClaimCacheImpl()

    private lateinit var damageClaimStore: IgniteCache<Long, DamageClaim>

    private fun provideIgnite(): Ignite {
        Ignition.setClientMode(false)
        return Ignition.start()
    }

    @Before
    fun init() {
        val ignite = provideIgnite()

        val cacheConfig = CacheConfiguration<Long, DamageClaim>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.USER_MALFUNCTION_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.ONE_MINUTE, Duration.ONE_MINUTE, Duration.ONE_MINUTE))

        damageClaimStore = ignite.createCache(cacheConfig)
        ReflectionTestUtils.setField(cache, "malfunctionStore", damageClaimStore)
    }

    @After
    fun tearDown() {
        Ignition.stopAll(true)
    }

    @Test
    fun testSaveOne() {
        val malfunction = DamageClaim(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, ServerUtils.getTimeFast(), arrayListOf())

        cache.saveOne(malfunction)
        val malfunctionFromStore = cache.findOne(malfunction.id)

        assertEquals(true, malfunctionFromStore.isPresent())
        assertEquals(true, malfunctionFromStore.get().id == malfunction.id)
    }

    @Test
    fun testSaveMany() {
        val malfunction = DamageClaim(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, ServerUtils.getTimeFast(), arrayListOf())
        val malfunction2 = DamageClaim(1, 0, true, "436erydfawryu", 0, "test description2", 55.6, 44.2, ServerUtils.getTimeFast(), arrayListOf())

        cache.saveMany(listOf(malfunction, malfunction2))
        val malfunctionsFromStore = cache.findMany(listOf(0, 1))

        assertEquals(2, malfunctionsFromStore.size)
    }

    @Test
    fun testFindMany() {
        cache.saveMany(listOf(
                DamageClaim(13, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(14, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(15, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(16, 0, createdOn = ServerUtils.getTimeFast())))

        cache.saveMany(listOf(
                DamageClaim(9, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(10, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(11, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(12, 0, createdOn = ServerUtils.getTimeFast())))

        cache.saveMany(listOf(
                DamageClaim(7, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(7, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(8, 0, createdOn = ServerUtils.getTimeFast())))

        cache.saveMany(listOf(DamageClaim(6, 0, createdOn = ServerUtils.getTimeFast())))

        cache.saveMany(listOf(
                DamageClaim(1, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(2, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(3, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(4, 0, createdOn = ServerUtils.getTimeFast()),
                DamageClaim(5, 0, createdOn = ServerUtils.getTimeFast())))


        val result = cache.findMany(listOf(1, 13, 2, 5, 6, 4, 3))

        assertEquals(7, result.size)
        assertEquals(1, result.first().id)
        assertEquals(13, result.last().id)
    }

    @Test
    fun testDeleteOne() {
        val malfunction = DamageClaim(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, ServerUtils.getTimeFast(), arrayListOf())

        cache.saveOne(malfunction)
        val malfunctionFromStore = cache.findOne(malfunction.id)

        assertEquals(true, malfunctionFromStore.isPresent())
        assertEquals(true, malfunctionFromStore.get().id == malfunction.id)

        cache.deleteOne(malfunction.id)
        val malfunctionFromStore2 = cache.findOne(malfunction.id)

        assertEquals(false, malfunctionFromStore2.isPresent())
    }

    @Test
    fun testDeleteMany() {
        val malfunction = DamageClaim(0, 0, true, "436erydfyu", 0, "test description", 55.6, 44.2, ServerUtils.getTimeFast(), arrayListOf())
        val malfunction2 = DamageClaim(1, 0, true, "436erydfawryu", 0, "test description2", 55.6, 44.2, ServerUtils.getTimeFast(), arrayListOf())

        cache.saveMany(listOf(malfunction, malfunction2))
        val malfunctionsFromStore = cache.findMany(listOf(0, 1))

        assertEquals(2, malfunctionsFromStore.size)

        cache.deleteMany(listOf(0, 1))
        val malfunctionsFromStore2 = cache.findMany(listOf(0, 1))

        assertEquals(0, malfunctionsFromStore2.size)
    }
}




























