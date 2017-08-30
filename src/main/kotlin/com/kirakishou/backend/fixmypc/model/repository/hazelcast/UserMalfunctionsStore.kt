package com.kirakishou.backend.fixmypc.model.repository.hazelcast

interface UserMalfunctionsStore {
    fun addMalfunction(ownerId: Long, malfunctionId: Long)
    fun addMany(ownerId: Long, malfunctionIdList: List<Long>)
    fun getMany(ownerId: Long, offset: Long, count: Long): List<Long>
    fun removeMalfunction(ownerId: Long, malfunctionId: Long)
}