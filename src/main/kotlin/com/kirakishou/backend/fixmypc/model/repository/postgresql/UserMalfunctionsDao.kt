package com.kirakishou.backend.fixmypc.model.repository.postgresql

interface UserMalfunctionsDao {

    interface Result {
        class Saved : Result
        class FoundMany(val idList: List<Long>) : Result
        class NotFound : Result
        class Deleted : Result
        class DbError(val e: Throwable) : Result
    }

    fun saveOne(ownerId: Long, malfunctionId: Long): Result
    fun findMany(ownerId: Long, offset: Long, count: Long): Result
    fun findAll(ownerId: Long): Result
    fun deleteOne(ownerId: Long, malfunctionId: Long): Result
    fun deleteOnePermanently(ownerId: Long, malfunctionId: Long): Result
}