package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionRepository {
    fun createMalfunction(malfunction: Malfunction): Boolean
    fun get(malfunctionId: Long): Fickle<Malfunction>
    fun getMany(ownerId: Long, offset: Long, count: Long): List<Malfunction>
    fun deleteMalfunction(ownerId: Long, malfunctionId: Long): Boolean
}