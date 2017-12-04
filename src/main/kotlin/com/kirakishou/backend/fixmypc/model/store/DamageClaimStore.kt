package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

interface DamageClaimStore {
    fun saveOne(damageClaim: DamageClaim): Boolean
    fun saveMany(damageClaimList: List<DamageClaim>): Boolean
    fun findOne(damageClaimId: Long): Fickle<DamageClaim>
    fun findMany(isActive: Boolean, damageClaimIdList: List<Long>): List<DamageClaim>
    fun findAll(isActive: Boolean): List<DamageClaim>
    fun findAll(): List<DamageClaim>
    fun findManyPaged(isActive: Boolean, userId: Long, offset: Long, count: Long): List<DamageClaim>
    fun deleteOne(damageClaim: DamageClaim): Boolean
    fun clear()
}