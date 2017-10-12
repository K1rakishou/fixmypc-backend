package com.kirakishou.backend.fixmypc.model.repository.cache

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.User
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

@Component
class SessionCacheImpl : SessionCache {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    //damageClaimId, DamageClaim
    lateinit var sessionCache: IgniteCache<String, User>

    @PostConstruct
    fun init() {
        val sessionCacheConfig = CacheConfiguration<String, User>()
        sessionCacheConfig.backups = 0
        sessionCacheConfig.name = Constant.IgniteNames.SESSION_CACHE
        sessionCacheConfig.cacheMode = CacheMode.PARTITIONED
        sessionCacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.ONE_HOUR, Duration.ONE_HOUR, Duration.ONE_HOUR))

        sessionCache = ignite.createCache(sessionCacheConfig)
    }

    override fun saveOne(sessionId: String, user: User) {
        sessionCache.put(sessionId, user)
    }

    override fun findOne(sessionId: String): Fickle<User> {
        return Fickle.of(sessionCache[sessionId])
    }
}