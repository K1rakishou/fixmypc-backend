package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileDao {
    suspend fun saveOne(specialistProfile: SpecialistProfile): Boolean
    suspend fun findOne(userId: Long): Fickle<SpecialistProfile>
    suspend fun findMany(userIdList: List<Long>): List<SpecialistProfile>
    suspend fun updateInfo(userId: Long, name: String, phone: String): Boolean
    suspend fun updatePhoto(userId: Long, photoName: String): Boolean
}