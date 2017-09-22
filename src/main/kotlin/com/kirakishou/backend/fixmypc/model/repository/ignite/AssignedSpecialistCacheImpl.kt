package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

class AssignedSpecialistCacheImpl : AssignedSpecialistCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var clientProfileCache: IgniteCache<Long, AssignedSpecialist>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, AssignedSpecialist>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.DAMAGE_CLAIM_ASSIGNED_SPECIALIST
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.THIRTY_MINUTES, Duration.THIRTY_MINUTES, Duration.THIRTY_MINUTES))

        clientProfileCache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(assignedSpecialist: AssignedSpecialist) {
        clientProfileCache.put(assignedSpecialist.damageClaimId, assignedSpecialist)
    }

    override fun saveMany(assignedSpecialistList: List<AssignedSpecialist>) {
        val assignedSpecialistMap = hashMapOf<Long, AssignedSpecialist>()

        for (assignedSpecialist in assignedSpecialistList) {
            assignedSpecialistMap.put(assignedSpecialist.damageClaimId, assignedSpecialist)
        }

        clientProfileCache.putAll(assignedSpecialistMap)
    }

    override fun findOne(damageClaimId: Long, isActive: Boolean): Fickle<AssignedSpecialist> {
        val assignedSpecialist = clientProfileCache[damageClaimId]
        if (assignedSpecialist == null) {
            return Fickle.empty()
        }

        if (assignedSpecialist.isActive != isActive) {
            return Fickle.empty()
        }

        return Fickle.of(assignedSpecialist)
    }

    override fun findMany(damageClaimIdList: List<Long>, isActive: Boolean): List<AssignedSpecialist> {
        val assignedSpecialistList = clientProfileCache.getAll(damageClaimIdList.toSet())
        if (assignedSpecialistList == null || assignedSpecialistList.isEmpty()) {
            return emptyList()
        }

        return assignedSpecialistList.values
                .stream()
                .filter { it.isActive == isActive }
                .collect(Collectors.toList())
    }
}























