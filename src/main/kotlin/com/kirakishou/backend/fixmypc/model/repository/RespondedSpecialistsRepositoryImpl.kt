package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import com.kirakishou.backend.fixmypc.model.store.RespondedSpecialistsStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RespondedSpecialistsRepositoryImpl : RespondedSpecialistsRepository {

    @Autowired
    lateinit var store: RespondedSpecialistsStore

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean {
        store.saveOne(respondedSpecialist)
        return true
    }

    override fun findAllForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist> {
        return store.findManyForDamageClaimPaged(damageClaimId, skip, count)
    }

    override fun containsOne(userId: Long, damageClaimId: Long): Boolean {
        val cacheResult = store.findOne(userId, damageClaimId)
        return cacheResult.isPresent()
    }

    override fun deleteAllForDamageClaim(damageClaimId: Long): Boolean {
        store.deleteAllForDamageClaim(damageClaimId)
        return true
    }
}

























