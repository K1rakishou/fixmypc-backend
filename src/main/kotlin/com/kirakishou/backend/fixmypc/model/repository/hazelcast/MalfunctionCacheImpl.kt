package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class MalfunctionCacheImpl : MalfunctionCache {

    @Autowired
    private lateinit var hazelcast: HazelcastInstance

    private lateinit var malfunctionCache: IMap<String, Malfunction>

    @PostConstruct
    fun init() {
        malfunctionCache = hazelcast.getMap<String, Malfunction>(Constant.HazelcastNames.MALFUNCTION_CACHE_KEY)
    }

    override fun save(key: String, malfunction: Malfunction) {
        malfunctionCache.put(key, malfunction, 1, TimeUnit.HOURS)
    }

    override fun get(key: String): Fickle<Malfunction> {
        val value = malfunctionCache[key] ?: return Fickle.empty()
        return Fickle.of(value)
    }

    override fun delete(key: String) {
        malfunctionCache.remove(key)
    }
}