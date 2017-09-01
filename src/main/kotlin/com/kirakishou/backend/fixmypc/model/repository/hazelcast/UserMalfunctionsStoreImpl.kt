package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.MultiMap
import com.kirakishou.backend.fixmypc.extension.doInTransaction
import com.kirakishou.backend.fixmypc.model.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class UserMalfunctionsStoreImpl : UserMalfunctionsStore {

    @Autowired
    private lateinit var hazelcast: HazelcastInstance

    private lateinit var userMalfunctionStore: MultiMap<Long, Long>

    @PostConstruct
    fun init() {
        userMalfunctionStore = hazelcast.getMultiMap<Long, Long>(Constant.HazelcastNames.USER_MALFUNCTION_KEY)
    }

    override fun saveOne(ownerId: Long, malfunctionId: Long) {
        userMalfunctionStore.lock(ownerId)

        try {
            if (!userMalfunctionStore.containsEntry(ownerId, malfunctionId)) {
                userMalfunctionStore.put(ownerId, malfunctionId)
            }
        } finally {
            userMalfunctionStore.unlock(ownerId)
        }
    }

    override fun saveMany(ownerId: Long, malfunctionIdList: List<Long>) {
        hazelcast.doInTransaction {
            for (id in malfunctionIdList) {
                if (!userMalfunctionStore.containsEntry(ownerId, id)) {
                    userMalfunctionStore.put(ownerId, id)
                }
            }
        }
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val userAllMalfunctions = userMalfunctionStore.get(ownerId) ?: return emptyList()

        return userAllMalfunctions.stream()
                .skip(offset)
                .limit(count)
                .collect(Collectors.toList())
    }

    override fun findAll(ownerId: Long): List<Long> {
        val userAllMalfunctions = userMalfunctionStore.get(ownerId) ?: return emptyList()
        return ArrayList(userAllMalfunctions)
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long) {
        userMalfunctionStore.remove(ownerId, malfunctionId)
    }

}