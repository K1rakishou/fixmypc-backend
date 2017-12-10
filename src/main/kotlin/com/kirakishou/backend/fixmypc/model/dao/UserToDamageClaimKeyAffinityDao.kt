package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Either

interface UserToDamageClaimKeyAffinityDao {
    fun saveOne(ownerId: Long, malfunctionId: Long): Either<Throwable, Boolean>
    fun findMany(ownerId: Long, offset: Long, count: Long): Either<Throwable, List<Long>>
    fun findAll(ownerId: Long): Either<Throwable, List<Long>>
    fun deleteOne(ownerId: Long, malfunctionId: Long): Either<Throwable, Boolean>
    fun deleteOnePermanently(ownerId: Long, malfunctionId: Long): Either<Throwable, Boolean>
}