package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import com.kirakishou.backend.fixmypc.util.TextUtils
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

    override fun containsOne(damageClaimId: Long, userId: Long): Boolean {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? AND user_id = ?"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId, userId)

        val result = respondedSpecialistsCache.query(sqlQuery).use {
            it.all.firstOrNull {
                it.value.userId == userId
            }
        }

        return result != null
    }

    override fun findOne(damageClaimId: Long, userId: Long): Fickle<RespondedSpecialist> {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? AND user_id = ?"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId)

        val respondedSpecialist = respondedSpecialistsCache.query(sqlQuery).use { it.all }
        if (respondedSpecialist.isEmpty()) {
            return Fickle.empty()
        }

        return Fickle.of(respondedSpecialist.map { it.value }.first())
    }

    override fun findMany(damageClaimIdList: List<Long>): List<RespondedSpecialist> {
        val statement = TextUtils.createStatementForList(damageClaimIdList.size)
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id IN ($statement)"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(*damageClaimIdList.toTypedArray())

        return respondedSpecialistsCache.query(sqlQuery).use { query ->
            query.all.map { it.value }
        }
    }

    override fun findManyForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist> {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? OFFSET ? LIMIT ?"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId, skip, count)

        return respondedSpecialistsCache.query(sqlQuery).use { entries ->
            entries.all.map { it.value }
        }
    }

    override fun updateSetViewed(responseId: Long): Boolean {
        val lock = respondedSpecialistsCache.lock(responseId)
        lock.lock()

        try {
            val respondedSpecialist = respondedSpecialistsCache[responseId]
                    ?: return false

            respondedSpecialist.wasViewed = true
            respondedSpecialistsCache.put(responseId, respondedSpecialist)
        } catch (e: Throwable) {
            return false
        } finally {
            lock.unlock()
        }

        return true
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





































