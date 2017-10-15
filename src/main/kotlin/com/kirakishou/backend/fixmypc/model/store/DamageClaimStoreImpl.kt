package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteAtomicSequence
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.SqlQuery
import org.apache.ignite.configuration.AtomicConfiguration
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DamageClaimStoreImpl : DamageClaimStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    private val tableName = "DamageClaim"
    lateinit var damageClaimIdGenerator: IgniteAtomicSequence
    lateinit var damageClaimStore: IgniteCache<Long, DamageClaim>

    @PostConstruct
    fun init() {
        val damageClaimStoreConfig = CacheConfiguration<Long, DamageClaim>(Constant.IgniteNames.DAMAGE_CLAIM_STORE)
        damageClaimStoreConfig.backups = 1
        damageClaimStoreConfig.cacheMode = CacheMode.PARTITIONED
        damageClaimStoreConfig.setIndexedTypes(Long::class.java, DamageClaim::class.java)
        damageClaimStore = ignite.getOrCreateCache(damageClaimStoreConfig)

        val atomicConfig = AtomicConfiguration()
        atomicConfig.backups = 3
        atomicConfig.cacheMode = CacheMode.PARTITIONED
        damageClaimIdGenerator = ignite.atomicSequence(Constant.IgniteNames.DAMAGE_CLAIM_GENERATOR, atomicConfig, 0L, true)
    }

    override fun saveOne(damageClaim: DamageClaim): Boolean {
        try {
            damageClaim.id = damageClaimIdGenerator.andIncrement
            damageClaimStore.put(damageClaim.id, damageClaim)

            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun saveMany(damageClaimList: List<DamageClaim>): Boolean {
        try {
            val damageClaimMap = hashMapOf<Long, DamageClaim>()
            for (malfunction in damageClaimList) {
                damageClaimMap.put(malfunction.id, malfunction)
            }

            damageClaimStore.putAll(damageClaimMap)

            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun findOne(damageClaimId: Long): Fickle<DamageClaim> {
        return Fickle.of(damageClaimStore[damageClaimId])
    }

    override fun findMany(isActive: Boolean, damageClaimIdList: List<Long>): List<DamageClaim> {
        val set = damageClaimIdList.toSet()
        val clientDamageClaims = damageClaimStore.getAll(set).values
        val filtered = clientDamageClaims.filter { it.isActive == isActive }

        return ArrayList(filtered)
    }

    override fun findManyPaged(isActive: Boolean, userId: Long, offset: Long, count: Long): List<DamageClaim> {
        val sql = "SELECT * FROM $tableName WHERE user_id = ? AND is_active = ? OFFSET ? LIMIT ?"
        val sqlQuery = SqlQuery<Long, DamageClaim>(DamageClaim::class.java, sql).setArgs(userId, isActive, offset, count)

        return damageClaimStore.query(sqlQuery)
                .all
                .map { it.value }
    }

    override fun findAll(isActive: Boolean): List<DamageClaim> {
        val sql = "SELECT * FROM $tableName WHERE is_active = ?"
        val sqlQuery = SqlQuery<Long, DamageClaim>(DamageClaim::class.java, sql).setArgs(isActive)

        return damageClaimStore.query(sqlQuery)
                .all
                .map { it.value }
    }

    override fun findAll(): List<DamageClaim> {
        val sql = "SELECT * FROM $tableName"
        val sqlQuery = SqlQuery<Long, DamageClaim>(DamageClaim::class.java, sql)

        return damageClaimStore.query(sqlQuery)
                .all
                .map { it.value }
    }

    override fun deleteOne(damageClaim: DamageClaim): Boolean {
        try {
            damageClaimStore.remove(damageClaim.id)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun clear() {
        damageClaimStore.clear()
    }
}