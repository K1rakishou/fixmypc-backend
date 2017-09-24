package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

@Component
class ClientProfileCacheImpl : ClientProfileCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var clientProfileCache: IgniteCache<Long, ClientProfile>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, ClientProfile>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.CLIENT_PROFILE_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        clientProfileCache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(clientProfile: ClientProfile) {
        clientProfileCache.put(clientProfile.userId, clientProfile)
    }

    override fun findOne(userId: Long): Fickle<ClientProfile> {
        return Fickle.of(clientProfileCache[userId])
    }

    /*override fun deleteOne(userId: Long) {
        clientProfileCache.remove(userId)
    }*/
}