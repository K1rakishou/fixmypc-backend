package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionStore {
    fun saveOne(malfunction: Malfunction)
    fun saveMany(malfunctionList: List<Malfunction>)
    fun findOne(malfunctionId: Long): Fickle<Malfunction>
    fun findMany(malfunctionIdList: List<Long>): List<Malfunction>
    fun deleteOne(malfunctionId: Long)
    fun deleteMany(malfunctionIdList: List<Long>)
    fun clear()
}