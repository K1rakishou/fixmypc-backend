package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

interface DamageClaimRepository {
    fun saveOne(damageClaim: DamageClaim): Boolean
    fun findOne(damageClaimId: Long): Fickle<DamageClaim>
    fun findMany(ownerId: Long, offset: Long, count: Long): List<DamageClaim>
    fun findMany(idsToSearch: List<Long>): List<DamageClaim>
    fun deleteOne(ownerId: Long, damageClaimId: Long): Boolean
}