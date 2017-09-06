package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdLocationDTO
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import java.sql.SQLException

interface DamageClaimDao {
    fun saveOne(damageClaim: DamageClaim): Either<SQLException, Boolean>
    fun findOne(id: Long): Either<SQLException, Fickle<DamageClaim>>
    fun findManyActiveByIdList(idsToSearch: List<Long>): Either<SQLException, List<DamageClaim>>
    fun findManyActiveByOwnerId(ownerId: Long): Either<Exception, List<DamageClaim>>
    fun findManyInactiveByOwnerId(ownerId: Long): Either<Exception, List<DamageClaim>>
    fun findAllIdsWithLocations(offset: Long, count: Long): List<DamageClaimIdLocationDTO>
    fun findPaged(ownerId: Long, isActive: Boolean, offset: Long, count: Int): Either<Exception, List<DamageClaim>>
    fun deleteOne(id: Long): Either<SQLException, Boolean>
    fun deleteOnePermanently(id: Long): Either<SQLException, Boolean>
}