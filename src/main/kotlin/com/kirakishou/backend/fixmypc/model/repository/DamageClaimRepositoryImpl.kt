package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.repository.ignite.DamageClaimStore
import com.kirakishou.backend.fixmypc.model.repository.ignite.LocationStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class DamageClaimRepositoryImpl : DamageClaimRepository {

    @Autowired
    private lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    private lateinit var locationStore: LocationStore

    @Autowired
    private lateinit var userToDamageClaimKeyAffinityRepository: UserToDamageClaimKeyAffinityRepository

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(damageClaim: DamageClaim): Boolean {
        damageClaimStore.saveOne(damageClaim)
        userToDamageClaimKeyAffinityRepository.saveOne(damageClaim.ownerId, damageClaim.id)
        return true
    }

    override fun findOne(damageClaimId: Long): Fickle<DamageClaim> {
        return damageClaimStore.findOne(damageClaimId)
    }

    override fun findMany(isActive: Boolean, ownerId: Long, offset: Long, count: Long): List<DamageClaim> {
        val malfunctionIdList = userToDamageClaimKeyAffinityRepository.findMany(ownerId, offset, count)
        if (malfunctionIdList.isEmpty()) {
            return emptyList()
        }

        return damageClaimStore.findMany(isActive, malfunctionIdList)
    }

    override fun findMany(isActive: Boolean, idsToSearch: List<Long>): List<DamageClaim> {
        return damageClaimStore.findMany(isActive, idsToSearch) as ArrayList
    }

    override fun deleteOne(ownerId: Long, damageClaimId: Long): Boolean {
        userToDamageClaimKeyAffinityRepository.deleteOne(ownerId, damageClaimId)
        locationStore.deleteOne(damageClaimId)
        return true
    }
}