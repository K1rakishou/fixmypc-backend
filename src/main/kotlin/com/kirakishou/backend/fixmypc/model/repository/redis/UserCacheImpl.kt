package com.kirakishou.backend.fixmypc.model.repository.redis

import com.kirakishou.backend.fixmypc.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import java.util.*


/**
 * Created by kirakishou on 7/11/2017.
 */

@Component
class UserCacheImpl : UserCache {

    @Autowired
    lateinit var cacheManager: CacheManager

    val cache: Cache by lazy {
        cacheManager.getCache("users")
    }

    override fun save(key: String, user: User) {
        cache.put(key, user)
    }

    override fun get(key: String): Optional<User> {
        val cachedValWrapped = cache.get(key) ?:
                return Optional.ofNullable(null)

        return Optional.of(cachedValWrapped.get() as User)
    }

    override fun delete(key: String) {
        cache.evict(key)
    }
}