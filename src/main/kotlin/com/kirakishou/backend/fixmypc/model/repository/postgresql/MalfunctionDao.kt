package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import java.sql.SQLException

interface MalfunctionDao {
    fun saveOne(damageClaim: DamageClaim): Either<SQLException, Boolean>
    fun findOne(id: Long): Either<SQLException, Fickle<DamageClaim>>
    fun findManyActive(ownerId: Long): Either<Exception, List<DamageClaim>>
    fun findManyInactive(ownerId: Long): Either<Exception, List<DamageClaim>>
    fun findPaged(ownerId: Long, isActive: Boolean, offset: Long, count: Int): Either<Exception, List<DamageClaim>>
    fun deleteOne(id: Long): Either<SQLException, Boolean>
    fun deleteOnePermanently(id: Long): Either<SQLException, Boolean>
}