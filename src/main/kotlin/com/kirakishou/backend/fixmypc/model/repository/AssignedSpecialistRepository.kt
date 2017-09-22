package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist

interface AssignedSpecialistRepository {
    fun saveOne(assignedSpecialist: AssignedSpecialist): Boolean
    fun findOne(damageClaimId: Long, isActive: Boolean): Fickle<AssignedSpecialist>
    fun findMany(damageClaimIdList: List<Long>, isActive: Boolean): List<AssignedSpecialist>
}