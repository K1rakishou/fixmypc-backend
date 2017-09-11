package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.User
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration


/**
 * Created by kirakishou on 7/11/2017.
 */

@Component
class UserCacheImpl : UserCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var userStore: IgniteCache<String, User>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<String, User>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.USER_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        userStore = ignite.createCache(cacheConfig)
    }

    override fun saveOne(sessionId: String, user: User) {
        userStore.put(sessionId, user)
    }

    override fun findOne(sessionId: String): Fickle<User> {
        return Fickle.of(userStore[sessionId])
    }

    override fun deleteOne(sessionId: String) {
        userStore.remove(sessionId)
    }
}