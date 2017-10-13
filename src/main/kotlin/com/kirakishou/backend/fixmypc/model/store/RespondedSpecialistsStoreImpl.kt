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
import org.apache.ignite.configuration.AtomicConfiguration
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class RespondedSpecialistsStoreImpl : RespondedSpecialistsStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    lateinit var respondedSpecialistsCache: IgniteCache<Long, MutableSet<RespondedSpecialist>>
    lateinit var respondedSpecialistIdGenerator: IgniteAtomicSequence

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, MutableSet<RespondedSpecialist>>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.RESPONDED_SPECIALISTS_STORE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setIndexedTypes(Long::class.java, MutableSet::class.java)
        respondedSpecialistsCache = ignite.getOrCreateCache(cacheConfig)

        val atomicConfig = AtomicConfiguration()
        atomicConfig.backups = 3
        atomicConfig.cacheMode = CacheMode.PARTITIONED
        respondedSpecialistIdGenerator = ignite.atomicSequence(Constant.IgniteNames.RESPONDED_SPECIALIST_ID_GENERATOR, atomicConfig, 0L, true)
    }

    override fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean {
        try {
            val lock = respondedSpecialistsCache.lock(respondedSpecialist.damageClaimId)
            lock.lock()

            try {
                respondedSpecialist.id = respondedSpecialistIdGenerator.andIncrement

                val allRespondedSpecialists = get(respondedSpecialist.damageClaimId)
                allRespondedSpecialists.add(respondedSpecialist)
                respondedSpecialistsCache.put(respondedSpecialist.damageClaimId, allRespondedSpecialists)

            } finally {
                lock.unlock()
            }

            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun saveMany(damageClaimId: Long, respondedSpecialistList: List<RespondedSpecialist>): Boolean {
        try {
            val lock = respondedSpecialistsCache.lock(damageClaimId)
            lock.lock()

            try {
                val allRespondedSpecialists = get(damageClaimId)
                allRespondedSpecialists.addAll(respondedSpecialistList)
                respondedSpecialistsCache.put(damageClaimId, allRespondedSpecialists)
            } finally {
                lock.unlock()
            }

            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun containsOne(userId: Long, damageClaimId: Long): Boolean {
        val lock = respondedSpecialistsCache.lock(damageClaimId)
        lock.lock()

        try {
            val allRespondedSpecialists = respondedSpecialistsCache.get(userId) ?: return false
            val respondedSpecialist = allRespondedSpecialists.firstOrNull { it.damageClaimId == damageClaimId }
            if (respondedSpecialist == null) {
                return false
            }

            return true
        } finally {
            lock.unlock()
        }
    }

    override fun findOne(userId: Long, damageClaimId: Long): Fickle<RespondedSpecialist> {
        val allRespondedSpecialists = respondedSpecialistsCache.get(damageClaimId)
        if (allRespondedSpecialists == null || allRespondedSpecialists.isEmpty()) {
            return Fickle.empty()
        }

        return Fickle.of(allRespondedSpecialists.firstOrNull { it.userId == userId })
    }

    override fun findManyForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist> {
        val allRespondedSpecialists = respondedSpecialistsCache.get(damageClaimId) ?: return emptyList()

        return allRespondedSpecialists.stream()
                .skip(skip)
                .limit(count)
                .collect(Collectors.toList())
    }

    override fun deleteAllForDamageClaim(damageClaimId: Long): Boolean {
        try {
            respondedSpecialistsCache.remove(damageClaimId)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    private fun get(ownerId: Long): MutableSet<RespondedSpecialist> {
        var allRespondedSpecialists = respondedSpecialistsCache.get(ownerId)
        if (allRespondedSpecialists == null) {
            allRespondedSpecialists = HashSet()
        }
        return allRespondedSpecialists
    }
}





































