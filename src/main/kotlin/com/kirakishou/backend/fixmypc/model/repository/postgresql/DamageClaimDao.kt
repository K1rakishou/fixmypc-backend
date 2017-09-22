package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdLocationDTO
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

interface DamageClaimDao {
    fun saveOne(damageClaim: DamageClaim): Either<Throwable, Boolean>
    fun findOne(id: Long): Either<Throwable, Fickle<DamageClaim>>
    fun findManyActiveByIdList(idsToSearch: List<Long>): Either<Throwable, List<DamageClaim>>
    fun findManyActiveByOwnerId(ownerId: Long): Either<Throwable, List<DamageClaim>>
    fun findManyInactiveByOwnerId(ownerId: Long): Either<Throwable, List<DamageClaim>>
    fun findAllIdsWithLocations(offset: Long, count: Long): List<DamageClaimIdLocationDTO>
    fun findPaged(ownerId: Long, isActive: Boolean, offset: Long, count: Int): Either<Throwable, List<DamageClaim>>
    fun deleteOne(id: Long): Either<Throwable, Boolean>
    fun deleteOnePermanently(id: Long): Either<Throwable, Boolean>
}