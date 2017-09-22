package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist

interface RespondedSpecialistsRepository {
    fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean
    fun findAllForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist>
    fun deleteAllForDamageClaim(damageClaimId: Long): Boolean
}