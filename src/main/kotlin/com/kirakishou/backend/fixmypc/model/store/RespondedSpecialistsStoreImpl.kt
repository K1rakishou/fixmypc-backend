package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteAtomicSequence
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.SqlQuery
import org.apache.ignite.configuration.AtomicConfiguration
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class RespondedSpecialistsStoreImpl : RespondedSpecialistsStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    private val tableName = "RespondedSpecialist"

    //key is RespondedSpecialistId
    lateinit var respondedSpecialistsCache: IgniteCache<Long, RespondedSpecialist>
    lateinit var respondedSpecialistIdGenerator: IgniteAtomicSequence

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, RespondedSpecialist>(Constant.IgniteNames.RESPONDED_SPECIALISTS_STORE)
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.RESPONDED_SPECIALISTS_STORE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setIndexedTypes(Long::class.java, RespondedSpecialist::class.java)
        respondedSpecialistsCache = ignite.getOrCreateCache(cacheConfig)

        val atomicConfig = AtomicConfiguration()
        atomicConfig.backups = 3
        atomicConfig.cacheMode = CacheMode.PARTITIONED
        respondedSpecialistIdGenerator = ignite.atomicSequence(Constant.IgniteNames.RESPONDED_SPECIALIST_ID_GENERATOR, atomicConfig, 0L, true)
    }

    override fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean {
        try {
            respondedSpecialist.id = respondedSpecialistIdGenerator.andIncrement
            respondedSpecialistsCache.put(respondedSpecialist.id, respondedSpecialist)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun containsOne(damageClaimId: Long): Boolean {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? LIMIT 1"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId)

        return respondedSpecialistsCache.query(sqlQuery).use { it.all.size == 1 }
    }

    override fun findOne(damageClaimId: Long): Fickle<RespondedSpecialist> {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? LIMIT 1"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId)

        val respondedSpecialist = respondedSpecialistsCache.query(sqlQuery).use { it.all }
        if (respondedSpecialist.isEmpty()) {
            return Fickle.empty()
        }

        return Fickle.of(respondedSpecialist.map { it.value }.first())
    }

    override fun findManyForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist> {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? OFFSET ? LIMIT ?"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId, skip, count)

        return respondedSpecialistsCache.query(sqlQuery).use { entries -> entries.all.map { it.value } }
    }

    override fun deleteAllForDamageClaim(damageClaimId: Long): Boolean {
        try {
            val sql = "DELETE FROM $tableName WHERE damage_claim_id = ?"
            val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId)

            respondedSpecialistsCache.query(sqlQuery).use { it.all }
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }
}





































