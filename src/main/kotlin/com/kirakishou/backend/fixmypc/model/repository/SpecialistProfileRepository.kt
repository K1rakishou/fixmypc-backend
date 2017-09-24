package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileRepository {
    fun saveOne(specialistProfile: SpecialistProfile): Boolean
    fun findMany(userIdList: List<Long>): List<SpecialistProfile>
}