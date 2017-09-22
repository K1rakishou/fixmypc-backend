package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist

interface RespondedSpecialistsCache {
    fun saveOne(respondedSpecialist: RespondedSpecialist)
    fun saveMany(damageClaimId: Long, respondedSpecialistList: List<RespondedSpecialist>)
    fun findManyForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist>
    fun deleteAllForDamageClaim(damageClaimId: Long)
}