package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import java.sql.SQLException

interface UserToDamageClaimKeyAffinityDao {
    fun saveOne(ownerId: Long, malfunctionId: Long): Either<SQLException, Boolean>
    fun findMany(ownerId: Long, offset: Long, count: Long): Either<SQLException, List<Long>>
    fun findAll(ownerId: Long): Either<SQLException, List<Long>>
    fun deleteOne(ownerId: Long, malfunctionId: Long): Either<SQLException, Boolean>
    fun deleteOnePermanently(ownerId: Long, malfunctionId: Long): Either<SQLException, Boolean>
}