package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist

interface RespondedSpecialistsDao {
    fun saveOne(respondedSpecialist: RespondedSpecialist): Either<Throwable, Boolean>
    fun findOne(userId: Long, damageClaimId: Long): Either<Throwable, Fickle<RespondedSpecialist>>
    fun findAllForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): Either<Throwable, List<RespondedSpecialist>>
    fun deleteAllForDamageClaim(damageClaimId: Long): Either<Throwable, Boolean>
}