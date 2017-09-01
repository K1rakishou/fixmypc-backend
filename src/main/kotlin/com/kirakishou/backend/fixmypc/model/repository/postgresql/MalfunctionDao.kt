package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionDao {

    interface Result {
        class FoundOne(val malfunction: Malfunction) : Result
        class FoundMany(val malfunctions: List<Malfunction>) : Result
        class NotFound : Result
        class DbError(val e: Throwable) : Result
        class Deleted : Result
        class Saved : Result
    }

    fun saveOne(malfunction: Malfunction): Result
    fun findOne(id: Long): Result
    fun findManyActive(ownerId: Long): Result
    fun findManyInactive(ownerId: Long): Result
    fun findPaged(ownerId: Long, isActive: Boolean, offset: Long, count: Int): Result
    fun deleteOne(id: Long): Result
    fun deleteOnePermanently(id: Long): Result
}