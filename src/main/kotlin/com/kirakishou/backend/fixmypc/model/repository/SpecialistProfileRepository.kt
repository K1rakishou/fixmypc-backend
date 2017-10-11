package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileRepository {
    fun saveOne(specialistProfile: SpecialistProfile): Boolean
    fun findOne(userId: Long): Fickle<SpecialistProfile>
    fun findMany(userIdList: List<Long>): List<SpecialistProfile>
    fun updateInfo(userId: Long, name: String, phone: String): Boolean
    fun updatePhoto(userId: Long, photoName: String): Boolean
}