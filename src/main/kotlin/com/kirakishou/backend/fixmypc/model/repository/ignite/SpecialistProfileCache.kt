package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileCache {
    fun saveOne(specialistProfile: SpecialistProfile)
    fun saveMany(specialistProfileList: List<SpecialistProfile>)
    fun findMany(userIdList: List<Long>): List<SpecialistProfile>
}