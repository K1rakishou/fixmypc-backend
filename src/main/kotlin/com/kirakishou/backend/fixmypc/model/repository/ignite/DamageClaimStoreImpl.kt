package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.SqlQuery
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DamageClaimStoreImpl : DamageClaimStore {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var damageClaimStore: IgniteCache<Long, DamageClaim>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, DamageClaim>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.MALFUNCTION_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setIndexedTypes(Long::class.java, DamageClaim::class.java)
        //cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        damageClaimStore = ignite.createCache(cacheConfig)
    }

    override fun saveOne(damageClaim: DamageClaim) {
        damageClaimStore.put(damageClaim.id, damageClaim)
    }

    override fun saveMany(damageClaimList: List<DamageClaim>) {
        val damageClaimMap = hashMapOf<Long, DamageClaim>()

        for (malfunction in damageClaimList) {
            damageClaimMap.put(malfunction.id, malfunction)
        }

        damageClaimStore.putAll(damageClaimMap)
    }

    override fun findOne(malfunctionId: Long): Fickle<DamageClaim> {
        return Fickle.of(damageClaimStore[malfunctionId])
    }

    override fun findMany(isActive: Boolean, malfunctionIdList: List<Long>): List<DamageClaim> {
        val set = malfunctionIdList.toSet()
        val clientDamageClaims = damageClaimStore.getAll(set).values
        val filtered = clientDamageClaims.filter { it.isActive == isActive }

        return ArrayList(filtered)
    }

    override fun findAll(isActive: Boolean): List<DamageClaim> {
        val sql = "SELECT * FROM DamageClaim WHERE is_active = ?"
        val sqlQuery = SqlQuery<Long, DamageClaim>(DamageClaim::class.java, sql).setArgs(isActive)

        return damageClaimStore.query(sqlQuery)
                .all
                .map { it.value }
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