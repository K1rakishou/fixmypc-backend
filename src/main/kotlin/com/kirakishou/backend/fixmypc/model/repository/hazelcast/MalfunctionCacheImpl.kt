package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.MultiMap
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class MalfunctionCacheImpl : MalfunctionCache {

    @Autowired
    private lateinit var hazelcast: HazelcastInstance

    private lateinit var malfunctionCache: MultiMap<Long, Malfunction>

    @PostConstruct
    fun init() {
        malfunctionCache = hazelcast.getMultiMap<Long, Malfunction>(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY)
    }

    override fun save(ownerId: Long, malfunction: Malfunction) {
        malfunctionCache.lock(ownerId)

        try {
            if (!malfunctionCache.containsValue(malfunction)) {
                malfunctionCache.put(ownerId, malfunction)
            }
        } finally {
            malfunctionCache.unlock(ownerId)
        }
    }

    override fun saveMany(ownerId: Long, malfunctionList: List<Malfunction>) {
        malfunctionCache.lock(ownerId)

        try {
            for (malfunction in malfunctionList) {
                if (!malfunctionCache.containsValue(malfunction)) {
                    malfunctionCache.put(ownerId, malfunction)
                }
            }
        } finally {
            malfunctionCache.unlock(ownerId)
        }
    }

    override fun get(ownerId: Long, malfunctionId: Long): Fickle<Malfunction> {
        val malfunctionList = malfunctionCache[ownerId] ?: return Fickle.empty()

        val malfunction = malfunctionList.stream()
                .filter { mf -> mf.id == malfunctionId }
                .findFirst()

        if (!malfunction.isPresent) {
            return Fickle.empty()
        }

        return Fickle.of(malfunction.get())
    }

    override fun getAll(ownerId: Long): List<Malfunction> {
        val value = malfunctionCache[ownerId] ?: return emptyList()
        return ArrayList(value)
    }

    override fun getSome(ownerId: Long, offset: Long, count: Long): List<Malfunction> {
        val value = malfunctionCache[ownerId] ?: return emptyList()
        return value.stream()
                .skip(offset)
                .limit(count)
                .collect(Collectors.toList())
    }

    override fun delete(ownerId: Long, malfunctionId: Long) {
        malfunctionCache.lock(ownerId)

        try {
            val values = malfunctionCache.get(ownerId)
            val value = values.firstOrNull { it.id == malfunctionId }

            if (value != null) {
                malfunctionCache.remove(ownerId, value)
            }
        } finally {
            malfunctionCache.unlock(ownerId)
        }
    }

    override fun deleteAll(ownerId: Long) {
        malfunctionCache.remove(ownerId)
    }
}