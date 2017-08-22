package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionCache {
    fun save(ownerId: Long, malfunction: Malfunction)
    fun saveMany(ownerId: Long, malfunctionList: List<Malfunction>)
    fun get(ownerId: Long, malfunctionId: Long): Fickle<Malfunction>
    fun getSome(ownerId: Long, offset: Long, count: Long): List<Malfunction>
    fun getAll(ownerId: Long): List<Malfunction>
    fun delete(ownerId: Long, malfunctionId: Long)
    fun deleteAll(ownerId: Long)
}