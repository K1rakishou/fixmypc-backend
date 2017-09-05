package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
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
import java.util.*
import javax.cache.expiry.Duration

class UserMalfunctionsCacheImplTest {

    val store = UserMalfunctionsCacheImpl()

    private var userMalfunctionStore: IgniteCache<Long, SortedSet<Long>>? = null

    private fun provideIgnite(): Ignite {
        Ignition.setClientMode(false)
        return Ignition.start()
    }

    @Before
    fun init() {
        val ignite = provideIgnite()

        val cacheConfig = CacheConfiguration<Long, SortedSet<Long>>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.USER_MALFUNCTION_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.ONE_MINUTE, Duration.ONE_MINUTE, Duration.ONE_MINUTE))

        userMalfunctionStore = ignite.createCache(cacheConfig)

        ReflectionTestUtils.setField(store, "userMalfunctionStore", userMalfunctionStore)
    }

    @After
    fun tearDown() {
        Ignition.stopAll(true)
    }

    @Test
    fun testFindMany() {
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
    fun testFindMany2() {
        store.saveMany(0, listOf(12, 13, 14, 15, 16))
        store.saveMany(0, listOf(7, 8, 9, 10, 11))
        store.saveMany(0, listOf(4, 5, 6))
        store.saveMany(0, listOf(1, 2, 3))

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
    fun testFindMany3() {
        store.saveOne(0, 6)
        store.saveOne(0, 5)
        store.saveOne(0, 4)
        store.saveOne(0, 3)
        store.saveOne(0, 2)
        store.saveOne(0, 1)
        store.saveOne(0, 0)

        val firstFive = store.findMany(0, 0, 5)
        assertEquals(5, firstFive.size)
        assertEquals(0, firstFive[0])
        assertEquals(4, firstFive[4])
    }

    @Test
    fun testFindAll() {
        store.saveMany(0, listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))

        val values = store.findAll(0)
        assertEquals(16, values.size)
    }

    @Test
    fun testDeleteOne() {
        store.saveMany(0, listOf(1, 2, 3))
        store.deleteOne(0, 1)

        assertEquals(2, store.findAll(0).size)

        store.deleteOne(0, 2)
        store.deleteOne(0, 3)
        assertEquals(0, store.findAll(0).size)
    }
}






















