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

    private lateinit var activeUserMalfunctionStore: MultiMap<Long, Long>

    @PostConstruct
    fun init() {
        activeUserMalfunctionStore = hazelcast.getMultiMap<Long, Long>(Constant.HazelcastNames.ACTIVE_USER_MALFUNCTION_KEY)
    }

    override fun saveOne(ownerId: Long, malfunctionId: Long) {
        activeUserMalfunctionStore.lock(ownerId)

        try {
            if (!activeUserMalfunctionStore.containsEntry(ownerId, malfunctionId)) {
                activeUserMalfunctionStore.put(ownerId, malfunctionId)
            }
        } finally {
            activeUserMalfunctionStore.unlock(ownerId)
        }
    }

    override fun saveMany(ownerId: Long, malfunctionIdList: List<Long>) {
        hazelcast.doInTransaction {
            for (id in malfunctionIdList) {
                if (!activeUserMalfunctionStore.containsEntry(ownerId, id)) {
                    activeUserMalfunctionStore.put(ownerId, id)
                }
            }
        }
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val userAllMalfunctions = activeUserMalfunctionStore.get(ownerId) ?: return emptyList()

        return userAllMalfunctions.stream()
                .sorted { id1, id2 -> comparator(id1, id2) }
                .skip(offset)
                .limit(count)
                .collect(Collectors.toList())
    }

    override fun findAll(ownerId: Long): List<Long> {
        val userAllMalfunctions = activeUserMalfunctionStore.get(ownerId) ?: return emptyList()
        return ArrayList(userAllMalfunctions)
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long) {
        activeUserMalfunctionStore.remove(ownerId, malfunctionId)
    }

    override fun clear() {
        activeUserMalfunctionStore.clear()
    }

    private fun comparator(id1: Long, id2: Long): Int {
        if (id1 < id2) {
            return -1
        } else if (id1 > id2) {
            return 1
        }

        return 0
    }
}