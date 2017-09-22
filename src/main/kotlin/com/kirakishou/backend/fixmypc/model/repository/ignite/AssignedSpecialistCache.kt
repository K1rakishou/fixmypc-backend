package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist

interface AssignedSpecialistCache {
    fun saveOne(assignedSpecialist: AssignedSpecialist)
    fun saveMany(assignedSpecialistList: List<AssignedSpecialist>)
    fun findOne(damageClaimId: Long, isActive: Boolean): Fickle<AssignedSpecialist>
    fun findMany(damageClaimIdList: List<Long>, isActive: Boolean): List<AssignedSpecialist>
}