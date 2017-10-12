package com.kirakishou.backend.fixmypc.model.repository.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class RespondedSpecialistsStoreImpl : RespondedSpecialistsStore {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var respondedSpecialistsCache: IgniteCache<Long, MutableSet<RespondedSpecialist>>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, MutableSet<RespondedSpecialist>>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.RESPONDED_SPECIALISTS_STORE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setIndexedTypes(Long::class.java, MutableSet::class.java)
        //cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        respondedSpecialistsCache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(respondedSpecialist: RespondedSpecialist) {
        val lock = respondedSpecialistsCache.lock(respondedSpecialist.damageClaimId)
        lock.lock()

        try {
            val allRespondedSpecialists = get(respondedSpecialist.damageClaimId)
            allRespondedSpecialists.add(respondedSpecialist)
            respondedSpecialistsCache.put(respondedSpecialist.damageClaimId, allRespondedSpecialists)

        } finally {
            lock.unlock()
        }
    }

    override fun saveMany(damageClaimId: Long, respondedSpecialistList: List<RespondedSpecialist>) {
        val lock = respondedSpecialistsCache.lock(damageClaimId)
        lock.lock()

        try {
            val userMalfunctions = get(damageClaimId)
            userMalfunctions.addAll(respondedSpecialistList)
            respondedSpecialistsCache.put(damageClaimId, userMalfunctions)
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

    override fun deleteAllForDamageClaim(damageClaimId: Long) {
        respondedSpecialistsCache.remove(damageClaimId)
    }

    private fun get(ownerId: Long): MutableSet<RespondedSpecialist> {
        var allRespondedSpecialists = respondedSpecialistsCache.get(ownerId)
        if (allRespondedSpecialists == null) {
            allRespondedSpecialists = HashSet()
        }
        return allRespondedSpecialists
    }
}





































