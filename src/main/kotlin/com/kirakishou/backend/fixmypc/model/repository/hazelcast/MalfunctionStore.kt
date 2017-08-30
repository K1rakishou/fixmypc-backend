package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionStore {
    fun save(malfunction: Malfunction)
    fun saveMany(malfunctionList: List<Malfunction>)
    fun get(malfunctionId: Long): Fickle<Malfunction>
    fun getMany(malfunctionIdList: List<Long>): List<Malfunction>
    fun delete(malfunctionId: Long)
    fun deleteMany(malfunctionIdList: List<Long>)
}