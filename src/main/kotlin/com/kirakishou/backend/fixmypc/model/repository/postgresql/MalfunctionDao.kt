package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import java.sql.SQLException

interface MalfunctionDao {
    fun saveOne(malfunction: Malfunction): Either<SQLException, Boolean>
    fun findOne(id: Long): Either<SQLException, Fickle<Malfunction>>
    fun findManyActive(ownerId: Long): Either<Exception, List<Malfunction>>
    fun findManyInactive(ownerId: Long): Either<Exception, List<Malfunction>>
    fun findPaged(ownerId: Long, isActive: Boolean, offset: Long, count: Int): Either<Exception, List<Malfunction>>
    fun deleteOne(id: Long): Either<SQLException, Boolean>
    fun deleteOnePermanently(id: Long): Either<SQLException, Boolean>
}