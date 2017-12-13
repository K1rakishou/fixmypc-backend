package com.kirakishou.backend.fixmypc.model.cache

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.User
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import javax.cache.expiry.Duration

class SessionCacheImpl(
        val ignite: Ignite
) : SessionCache {

    val sessionCache: IgniteCache<String, User>

    init {
        val sessionCacheConfig = CacheConfiguration<String, User>()
        sessionCacheConfig.backups = 0
        sessionCacheConfig.name = Constant.IgniteNames.SESSION_CACHE
        sessionCacheConfig.cacheMode = CacheMode.PARTITIONED
        sessionCacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.ONE_HOUR, Duration.ONE_HOUR, Duration.ONE_HOUR))

        sessionCache = ignite.getOrCreateCache(sessionCacheConfig)
    }

    override fun saveOne(sessionId: String, user: User) {
        sessionCache.put(sessionId, user)
    }

    override fun findOne(sessionId: String): Fickle<User> {
        return Fickle.of(sessionCache[sessionId])
    }
}