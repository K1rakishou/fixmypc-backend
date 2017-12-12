package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdLocationDTO
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

interface DamageClaimDao {
    suspend fun saveOne(damageClaim: DamageClaim): Boolean
    suspend fun findOne(id: Long): Fickle<DamageClaim>
    suspend fun findManyByIdList(isActive: Boolean, idsToSearch: List<Long>): List<DamageClaim>
    suspend fun findManyByOwnerId(isActive: Boolean, ownerId: Long): List<DamageClaim>
    suspend fun findAllIdsWithLocations(offset: Long, count: Long): List<DamageClaimIdLocationDTO>
    suspend fun findPaged(ownerId: Long, isActive: Boolean, offset: Long, count: Int): List<DamageClaim>
    suspend fun findAll(isActive: Boolean): List<DamageClaim>
    suspend fun findAll(): List<DamageClaim>
    suspend fun deleteOne(id: Long): Boolean
    suspend fun deleteOnePermanently(id: Long): Boolean
}