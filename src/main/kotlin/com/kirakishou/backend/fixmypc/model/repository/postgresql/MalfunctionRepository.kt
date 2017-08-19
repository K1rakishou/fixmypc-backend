package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionRepository {
    fun createNewMalfunctionRequest(malfunction: Malfunction)
    fun findMalfunctionRequestById(id: Long): Fickle<Malfunction>
    fun deleteMalfunctionRequest(id: Long)
}