package com.kirakishou.backend.fixmypc.model.repository

interface UserMalfunctionsRepository {
    fun saveOne(ownerId: Long, malfunctionId: Long): Boolean
    fun findMany(ownerId: Long, offset: Long, count: Long): List<Long>
    fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean
}