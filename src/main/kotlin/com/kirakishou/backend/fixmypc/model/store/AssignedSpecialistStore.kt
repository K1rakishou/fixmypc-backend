package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist

interface AssignedSpecialistStore {
    fun saveOne(assignedSpecialist: AssignedSpecialist): Boolean
    fun saveMany(assignedSpecialistList: List<AssignedSpecialist>): Boolean
    fun findOne(damageClaimId: Long): Fickle<AssignedSpecialist>
    fun findOne(damageClaimId: Long, isWorkDone: Boolean): Fickle<AssignedSpecialist>
    fun findMany(damageClaimIdList: List<Long>, isWorkDone: Boolean): List<AssignedSpecialist>
}