package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
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
class AssignedSpecialistStoreImpl : AssignedSpecialistStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    lateinit var assignedSpecialistStore: IgniteCache<Long, AssignedSpecialist>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, AssignedSpecialist>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.DAMAGE_CLAIM_ASSIGNED_SPECIALIST_STORE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setIndexedTypes(Long::class.java, AssignedSpecialist::class.java)

        assignedSpecialistStore = ignite.getOrCreateCache(cacheConfig)
    }

    override fun saveOne(assignedSpecialist: AssignedSpecialist): Boolean {
        try {
            assignedSpecialistStore.put(assignedSpecialist.damageClaimId, assignedSpecialist)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun saveMany(assignedSpecialistList: List<AssignedSpecialist>): Boolean {
        val assignedSpecialistMap = hashMapOf<Long, AssignedSpecialist>()

        for (assignedSpecialist in assignedSpecialistList) {
            assignedSpecialistMap.put(assignedSpecialist.damageClaimId, assignedSpecialist)
        }

        try {
            assignedSpecialistStore.putAll(assignedSpecialistMap)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun findOne(damageClaimId: Long, isWorkDone: Boolean): Fickle<AssignedSpecialist> {
        val assignedSpecialist = assignedSpecialistStore[damageClaimId]
        if (assignedSpecialist == null) {
            return Fickle.empty()
        }

        if (assignedSpecialist.isWorkDone != isWorkDone) {
            return Fickle.empty()
        }

        return Fickle.of(assignedSpecialist)
    }

    override fun findMany(damageClaimIdList: List<Long>, isWorkDone: Boolean): List<AssignedSpecialist> {
        val assignedSpecialistList = assignedSpecialistStore.getAll(damageClaimIdList.toSet())
        if (assignedSpecialistList == null || assignedSpecialistList.isEmpty()) {
            return emptyList()
        }

        return assignedSpecialistList.values
                .stream()
                .filter { it.isWorkDone == isWorkDone }
                .collect(Collectors.toList())
    }
}























