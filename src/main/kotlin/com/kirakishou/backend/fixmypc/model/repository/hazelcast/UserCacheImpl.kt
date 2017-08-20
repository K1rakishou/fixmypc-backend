package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
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

    lateinit var userCache: IMap<String, User>

    @PostConstruct
    fun init() {
        userCache = hazelcast.getMap<String, User>(Constant.HazelcastNames.USER_CACHE_KEY)
    }

    override fun save(key: String, user: User) {
        userCache.put(key, user, 20, TimeUnit.SECONDS)
    }

    override fun get(key: String): Fickle<User> {
        val value = userCache[key] ?: return Fickle.empty()
        return Fickle.of(value)
    }

    override fun delete(key: String) {
        userCache.remove(key)
    }
}