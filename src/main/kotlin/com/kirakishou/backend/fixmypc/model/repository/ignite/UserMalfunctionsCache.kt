package com.kirakishou.backend.fixmypc.model.repository.ignite

interface UserMalfunctionsCache {
    fun saveOne(ownerId: Long, malfunctionId: Long)
    fun saveMany(ownerId: Long, malfunctionIdList: List<Long>)
    fun findMany(ownerId: Long, offset: Long, count: Long): List<Long>
    fun findAll(ownerId: Long): List<Long>
    fun deleteOne(ownerId: Long, malfunctionId: Long)
    fun deleteAll(ownerId: Long)
    fun clear()
}