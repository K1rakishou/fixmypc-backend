package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

interface MalfunctionRepository {
    fun saveOne(damageClaim: DamageClaim): Boolean
    fun findOne(malfunctionId: Long): Fickle<DamageClaim>
    fun findMany(ownerId: Long, offset: Long, count: Long): List<DamageClaim>
    fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean
}