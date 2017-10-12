package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.store.DamageClaimStore
import com.kirakishou.backend.fixmypc.model.store.LocationStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class DamageClaimRepositoryImpl : DamageClaimRepository {

    @Autowired
    private lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    private lateinit var locationStore: LocationStore

    override fun saveOne(damageClaim: DamageClaim): Boolean {
        damageClaimStore.saveOne(damageClaim)
        return true
    }

    override fun findOne(damageClaimId: Long): Fickle<DamageClaim> {
        return damageClaimStore.findOne(damageClaimId)
    }

    override fun findMany(isActive: Boolean, ownerId: Long, offset: Long, count: Long): List<DamageClaim> {
        return damageClaimStore.findManyPaged(isActive, ownerId, offset, count)
    }

    override fun findMany(isActive: Boolean, idsToSearch: List<Long>): List<DamageClaim> {
        return damageClaimStore.findMany(isActive, idsToSearch) as ArrayList
    }

    override fun deleteOne(ownerId: Long, damageClaimId: Long): Boolean {
        locationStore.deleteOne(damageClaimId)
        return true
    }
}