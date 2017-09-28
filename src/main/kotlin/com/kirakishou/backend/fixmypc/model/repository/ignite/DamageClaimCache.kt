package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

interface DamageClaimCache {
    fun saveOne(damageClaim: DamageClaim)
    fun saveMany(damageClaimList: List<DamageClaim>)
    fun findOne(malfunctionId: Long): Fickle<DamageClaim>
    fun findMany(isActive: Boolean, malfunctionIdList: List<Long>): List<DamageClaim>
    fun deleteOne(malfunctionId: Long)
    fun deleteMany(malfunctionIdList: List<Long>)
    fun clear()
}