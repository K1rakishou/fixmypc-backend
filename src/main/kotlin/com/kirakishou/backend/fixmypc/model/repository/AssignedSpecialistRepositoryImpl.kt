package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import com.kirakishou.backend.fixmypc.model.repository.ignite.AssignedSpecialistStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AssignedSpecialistRepositoryImpl : AssignedSpecialistRepository {

    @Autowired
    private lateinit var store: AssignedSpecialistStore

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(assignedSpecialist: AssignedSpecialist): Boolean {
        store.saveOne(assignedSpecialist)
        return true
    }

    override fun findOne(damageClaimId: Long, isWorkDone: Boolean): Fickle<AssignedSpecialist> {
        return store.findOne(damageClaimId, isWorkDone)
    }

    override fun findMany(damageClaimIdList: List<Long>, isWorkDone: Boolean): List<AssignedSpecialist> {
        return store.findMany(damageClaimIdList, isWorkDone)
    }
}



































