package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdLocationDTO
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import java.sql.Connection

interface DamageClaimDao : Dao {
    suspend fun saveOne(damageClaim: DamageClaim, connection: Connection): Boolean
    suspend fun findOne(id: Long, connection: Connection): Fickle<DamageClaim>
    suspend fun findManyByIdList(isActive: Boolean, idsToSearch: List<Long>, connection: Connection): List<DamageClaim>
    suspend fun findManyByOwnerId(isActive: Boolean, ownerId: Long, connection: Connection): List<DamageClaim>
    suspend fun findAllIdsWithLocations(offset: Long, count: Long, connection: Connection): List<DamageClaimIdLocationDTO>
    suspend fun findPaged(ownerId: Long, isActive: Boolean, offset: Long, count: Int, connection: Connection): List<DamageClaim>
    suspend fun findAll(isActive: Boolean, connection: Connection): List<DamageClaim>
    suspend fun findAll(connection: Connection): List<DamageClaim>
    suspend fun deleteOne(id: Long, connection: Connection): Boolean
    suspend fun deleteOnePermanently(id: Long, connection: Connection): Boolean
}