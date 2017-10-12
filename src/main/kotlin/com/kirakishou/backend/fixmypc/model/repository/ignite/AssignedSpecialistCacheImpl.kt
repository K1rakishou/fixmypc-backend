package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class AssignedSpecialistCacheImpl : AssignedSpecialistCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var assignedSpecialistCache: IgniteCache<Long, AssignedSpecialist>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, AssignedSpecialist>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.DAMAGE_CLAIM_ASSIGNED_SPECIALIST_CACHE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setIndexedTypes(Long::class.java, AssignedSpecialist::class.java)
        //cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.THIRTY_MINUTES, Duration.THIRTY_MINUTES, Duration.THIRTY_MINUTES))

        assignedSpecialistCache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(assignedSpecialist: AssignedSpecialist) {
        assignedSpecialistCache.put(assignedSpecialist.damageClaimId, assignedSpecialist)
    }

    override fun saveMany(assignedSpecialistList: List<AssignedSpecialist>) {
        val assignedSpecialistMap = hashMapOf<Long, AssignedSpecialist>()

        for (assignedSpecialist in assignedSpecialistList) {
            assignedSpecialistMap.put(assignedSpecialist.damageClaimId, assignedSpecialist)
        }

        assignedSpecialistCache.putAll(assignedSpecialistMap)
    }

    override fun findOne(damageClaimId: Long, isWorkDone: Boolean): Fickle<AssignedSpecialist> {
        val assignedSpecialist = assignedSpecialistCache[damageClaimId]
        if (assignedSpecialist == null) {
            return Fickle.empty()
        }

        if (assignedSpecialist.isWorkDone != isWorkDone) {
            return Fickle.empty()
        }

        return Fickle.of(assignedSpecialist)
    }

    override fun findMany(damageClaimIdList: List<Long>, isWorkDone: Boolean): List<AssignedSpecialist> {
        val assignedSpecialistList = assignedSpecialistCache.getAll(damageClaimIdList.toSet())
        if (assignedSpecialistList == null || assignedSpecialistList.isEmpty()) {
            return emptyList()
        }

        return assignedSpecialistList.values
                .stream()
                .filter { it.isWorkDone == isWorkDone }
                .collect(Collectors.toList())
    }
}























