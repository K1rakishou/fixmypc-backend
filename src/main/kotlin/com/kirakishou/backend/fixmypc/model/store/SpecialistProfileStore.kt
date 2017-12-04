package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileStore {
    fun saveOne(specialistProfile: SpecialistProfile): Boolean
    fun saveMany(specialistProfileList: List<SpecialistProfile>): Boolean
    fun findOne(userId: Long): Fickle<SpecialistProfile>
    fun findMany(userIdList: List<Long>): List<SpecialistProfile>
    fun updateInfo(userId: Long, name: String, phone: String): Boolean
    fun updatePhoto(userId: Long, photoName: String): Boolean
    fun deleteOne(userId: Long): Boolean
}