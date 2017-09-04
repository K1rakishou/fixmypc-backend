package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
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

    lateinit var damageClaimStore: IgniteCache<Long, DamageClaim>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, DamageClaim>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.MALFUNCTION_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED

        damageClaimStore = ignite.createCache(cacheConfig)
    }

    override fun saveOne(damageClaim: DamageClaim) {
        damageClaimStore.put(damageClaim.id, damageClaim)
    }

    override fun saveMany(damageClaimList: List<DamageClaim>) {
        val malfunctionMap = hashMapOf<Long, DamageClaim>()

        for (malfunction in damageClaimList) {
            malfunctionMap.put(malfunction.id, malfunction)
        }

        damageClaimStore.putAll(malfunctionMap)
    }

    override fun findOne(malfunctionId: Long): Fickle<DamageClaim> {
        return Fickle.of(damageClaimStore[malfunctionId])
    }

    override fun findMany(malfunctionIdList: List<Long>): List<DamageClaim> {
        val set = malfunctionIdList.toSet()
        return ArrayList(damageClaimStore.getAll(set).values)
    }

    override fun deleteOne(malfunctionId: Long) {
        damageClaimStore.remove(malfunctionId)
    }

    override fun deleteMany(malfunctionIdList: List<Long>) {
        damageClaimStore.removeAll(malfunctionIdList.toSet())
    }

    override fun clear() {
        damageClaimStore.clear()
    }
}