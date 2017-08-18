package com.kirakishou.backend.fixmypc.model.repository.redis

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct


/**
 * Created by kirakishou on 7/11/2017.
 */

@Component
class UserCacheImpl : UserCache {

    @Autowired
    lateinit var hazelcast: HazelcastInstance

    lateinit var cache: IMap<String, User>

    @PostConstruct
    fun init() {
        cache = hazelcast.getMap<String, User>("users_cache")
    }

    override fun save(key: String, user: User) {
        cache.put(key, user, 10, TimeUnit.SECONDS)
    }

    override fun get(key: String): Fickle<User> {
        val value = cache[key] ?: return Fickle.empty()
        return Fickle.of(value)
    }

    override fun delete(key: String) {
        cache.remove(key)
    }
}