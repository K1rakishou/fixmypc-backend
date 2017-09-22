package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist

interface AssignedSpecialistDao {
    fun saveOne(assignedSpecialist: AssignedSpecialist): Either<Throwable, Boolean>
    fun findOne(damageClaimId: Long, isActive: Boolean): Either<Throwable, Fickle<AssignedSpecialist>>
    fun findMany(damageClaimIdList: List<Long>, isActive: Boolean): Either<Throwable, List<AssignedSpecialist>>
}