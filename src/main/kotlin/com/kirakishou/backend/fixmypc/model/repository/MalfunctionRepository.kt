package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionRepository {
    fun saveOne(malfunction: Malfunction): Boolean
    fun findOne(malfunctionId: Long): Fickle<Malfunction>
    fun findMany(ownerId: Long, offset: Long, count: Long): List<Malfunction>
    fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean
}