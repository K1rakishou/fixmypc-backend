package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist

interface RespondedSpecialistsStore {
    fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean
    fun containsOne(damageClaimId: Long, userId: Long): Boolean
    fun findOne(damageClaimId: Long): Fickle<RespondedSpecialist>
    fun findManyForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist>
    fun findAllAndCount(damageClaimIdList: List<Long>): MutableMap<Long, Int>
    fun deleteAllForDamageClaim(damageClaimId: Long): Boolean
}