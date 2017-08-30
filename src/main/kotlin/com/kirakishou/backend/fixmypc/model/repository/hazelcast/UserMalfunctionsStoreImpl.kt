package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.MultiMap
import com.kirakishou.backend.fixmypc.extension.doInTransaction
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Collectors

class UserMalfunctionsStoreImpl : UserMalfunctionsStore {

    @Autowired
    private lateinit var hazelcast: HazelcastInstance

    private lateinit var userMalfunctionStore: MultiMap<Long, Long>

    override fun addMalfunction(ownerId: Long, malfunctionId: Long) {
        userMalfunctionStore.lock(ownerId)

        try {
            if (!userMalfunctionStore.containsEntry(ownerId, malfunctionId)) {
                userMalfunctionStore.put(ownerId, malfunctionId)
            }
        } finally {
            userMalfunctionStore.unlock(ownerId)
        }
    }

    override fun addMany(ownerId: Long, malfunctionIdList: List<Long>) {
        hazelcast.doInTransaction {
            for (id in malfunctionIdList) {
                if (!userMalfunctionStore.containsEntry(ownerId, id)) {
                    userMalfunctionStore.put(ownerId, id)
                }
            }
        }
    }

    override fun getMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val userAllMalfunctions = userMalfunctionStore.get(ownerId) ?: return emptyList()

        return userAllMalfunctions.stream()
                .skip(offset)
                .limit(count)
                .collect(Collectors.toList())
    }

    override fun removeMalfunction(ownerId: Long, malfunctionId: Long) {
        userMalfunctionStore.remove(ownerId, malfunctionId)
    }

}