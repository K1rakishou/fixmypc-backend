package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.MultiMap
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
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

    override fun save(key: Long, malfunction: Malfunction) {
        malfunctionCache.put(key, malfunction)
    }

    override fun get(key: Long): List<Malfunction> {
        val value = malfunctionCache[key] ?: return emptyList()
        return ArrayList(value)
    }

    override fun delete(key: Long) {
        malfunctionCache.remove(key)
    }
}