package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileCache {
    fun saveOne(specialistProfile: SpecialistProfile)
    fun saveMany(specialistProfileList: List<SpecialistProfile>)
    fun findOne(userId: Long): Fickle<SpecialistProfile>
    fun findMany(userIdList: List<Long>): List<SpecialistProfile>
    fun updateInfo(userId: Long, name: String, phone: String)
    fun updatePhoto(userId: Long, photoName: String)
}