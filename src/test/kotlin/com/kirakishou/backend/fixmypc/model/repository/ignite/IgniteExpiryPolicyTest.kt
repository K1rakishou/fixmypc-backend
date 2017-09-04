package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import org.apache.ignite.Ignition
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit
import javax.cache.expiry.Duration

class IgniteExpiryPolicyTest {

    @Test
    fun testExpiryPolicy() {
        val ignite = Ignition.start()

        val cacheConfig = CacheConfiguration<String, String>()
        cacheConfig.backups = 0
        cacheConfig.name = "test_cache"
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(
                Duration(TimeUnit.MILLISECONDS, 200L), //update
                Duration(TimeUnit.MILLISECONDS, 200L), //create
                Duration(TimeUnit.MILLISECONDS, 200L)))    //access

        val testCache = ignite.createCache<String, String>(cacheConfig)

        testCache.put("testkey1", "value1")
        val value1 = testCache.get("testkey1")
        assertEquals(false, value1 == null)

        testCache.put("testkey2", "value2")
        Thread.sleep(350)
        val value2 = testCache.get("testkey2")
        assertEquals(true, value2 == null)

        testCache.put("testkey3", "value3")
        Thread.sleep(150)
        var value3 = testCache.get("testkey3")
        assertEquals(false, value3 == null)
        Thread.sleep(150)
        value3 = testCache.get("testkey3")
        assertEquals(false, value3 == null)
    }
}