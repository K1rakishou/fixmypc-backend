package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class MalfunctionStoreImpl : MalfunctionStore {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var malfunctionStore: IgniteCache<Long, Malfunction>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, Malfunction>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.MALFUNCTION_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED

        malfunctionStore = ignite.createCache(cacheConfig)
    }

    override fun saveOne(malfunction: Malfunction) {
        malfunctionStore.put(malfunction.id, malfunction)
    }

    override fun saveMany(malfunctionList: List<Malfunction>) {
        val malfunctionMap = hashMapOf<Long, Malfunction>()

        for (malfunction in malfunctionList) {
            malfunctionMap.put(malfunction.id, malfunction)
        }

        malfunctionStore.putAll(malfunctionMap)
    }

    override fun findOne(malfunctionId: Long): Fickle<Malfunction> {
        return Fickle.of(malfunctionStore[malfunctionId])
    }

    override fun findMany(malfunctionIdList: List<Long>): List<Malfunction> {
        val set = malfunctionIdList.toSet()
        return ArrayList(malfunctionStore.getAll(set).values)
    }

    override fun deleteOne(malfunctionId: Long) {
        malfunctionStore.remove(malfunctionId)
    }

    override fun deleteMany(malfunctionIdList: List<Long>) {
        malfunctionStore.removeAll(malfunctionIdList.toSet())
    }

    override fun clear() {
        malfunctionStore.clear()
    }
}