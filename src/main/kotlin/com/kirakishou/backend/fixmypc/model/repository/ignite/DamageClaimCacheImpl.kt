package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

@Component
class DamageClaimCacheImpl : DamageClaimCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var damageClaimCache: IgniteCache<Long, DamageClaim>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, DamageClaim>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.MALFUNCTION_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        damageClaimCache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(damageClaim: DamageClaim) {
        damageClaimCache.put(damageClaim.id, damageClaim)
    }

    override fun saveMany(damageClaimList: List<DamageClaim>) {
        val malfunctionMap = hashMapOf<Long, DamageClaim>()

        for (malfunction in damageClaimList) {
            malfunctionMap.put(malfunction.id, malfunction)
        }

        damageClaimCache.putAll(malfunctionMap)
    }

    override fun findOne(malfunctionId: Long): Fickle<DamageClaim> {
        return Fickle.of(damageClaimCache[malfunctionId])
    }

    override fun findMany(malfunctionIdList: List<Long>): List<DamageClaim> {
        val set = malfunctionIdList.toSet()
        return ArrayList(damageClaimCache.getAll(set).values)
    }

    override fun deleteOne(malfunctionId: Long) {
        damageClaimCache.remove(malfunctionId)
    }

    override fun deleteMany(malfunctionIdList: List<Long>) {
        damageClaimCache.removeAll(malfunctionIdList.toSet())
    }

    override fun clear() {
        damageClaimCache.clear()
    }
}