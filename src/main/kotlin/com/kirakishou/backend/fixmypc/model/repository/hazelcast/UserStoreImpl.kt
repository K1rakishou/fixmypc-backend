package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.EntryEvent
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import com.hazelcast.map.listener.EntryExpiredListener
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
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

    @Autowired
    private lateinit var userMalfunctionStore: UserMalfunctionsStore

    private lateinit var userStore: IMap<String, User>

    @PostConstruct
    fun init() {
        userStore = hazelcast.getMap<String, User>(Constant.HazelcastNames.USER_CACHE_KEY)
        userStore.addEntryListener(UserListener(), true)
    }

    override fun saveOne(sessionId: String, user: User) {
        userStore.put(sessionId, user)
    }

    override fun findOne(sessionId: String): Fickle<User> {
        val value = userStore[sessionId] ?: return Fickle.empty()
        return Fickle.of(value)
    }

    override fun deleteOne(sessionId: String) {
        userStore.remove(sessionId)
    }

    inner class UserListener : EntryExpiredListener<String, User> {
        override fun entryExpired(entry: EntryEvent<String, User>) {
            //once user removed from the cache, remove all of their malfunctions as well

            val id = if (entry.oldValue != null) {
                entry.oldValue.id
            } else {
                entry.value.id
            }

            userMalfunctionStore.deleteAll(id)
        }
    }
}