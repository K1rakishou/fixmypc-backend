package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


/**
 * Created by kirakishou on 7/11/2017.
 */

@Component
class UserStoreImpl : UserStore {

    @Autowired
    private lateinit var hazelcast: HazelcastInstance

    private lateinit var userStore: IMap<String, User>

    @PostConstruct
    fun init() {
        userStore = hazelcast.getMap<String, User>(Constant.HazelcastNames.USER_CACHE_KEY)
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