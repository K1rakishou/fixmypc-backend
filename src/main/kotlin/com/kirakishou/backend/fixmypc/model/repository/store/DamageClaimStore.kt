package com.kirakishou.backend.fixmypc.model.repository.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

interface DamageClaimStore {
    fun saveOne(damageClaim: DamageClaim)
    fun saveMany(damageClaimList: List<DamageClaim>)
    fun findOne(damageClaimId: Long): Fickle<DamageClaim>
    fun findMany(isActive: Boolean, damageClaimIdList: List<Long>): List<DamageClaim>
    fun findAll(isActive: Boolean): List<DamageClaim>
    fun findManyPaged(isActive: Boolean, userId: Long, offset: Long, count: Long): List<DamageClaim>
    fun deleteOne(damageClaim: DamageClaim)
    fun clear()
}