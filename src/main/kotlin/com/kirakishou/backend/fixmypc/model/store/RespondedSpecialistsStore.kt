package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist

interface RespondedSpecialistsStore {
    fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean
    fun containsOne(damageClaimId: Long): Boolean
    fun findOne(damageClaimId: Long): Fickle<RespondedSpecialist>
    fun findManyForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist>
    fun deleteAllForDamageClaim(damageClaimId: Long): Boolean
}