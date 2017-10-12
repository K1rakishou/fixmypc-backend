package com.kirakishou.backend.fixmypc.model.repository.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist

interface AssignedSpecialistStore {
    fun saveOne(assignedSpecialist: AssignedSpecialist)
    fun saveMany(assignedSpecialistList: List<AssignedSpecialist>)
    fun findOne(damageClaimId: Long, isWorkDone: Boolean): Fickle<AssignedSpecialist>
    fun findMany(damageClaimIdList: List<Long>, isWorkDone: Boolean): List<AssignedSpecialist>
}