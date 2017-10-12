package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import com.kirakishou.backend.fixmypc.model.repository.ignite.SpecialistProfileStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SpecialistProfileRepositoryImpl : SpecialistProfileRepository {

    @Autowired
    private lateinit var store: SpecialistProfileStore

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(specialistProfile: SpecialistProfile): Boolean {
        store.saveOne(specialistProfile)
        return true
    }

    override fun findOne(userId: Long): Fickle<SpecialistProfile> {
        return store.findOne(userId)
    }

    override fun findMany(userIdList: List<Long>): List<SpecialistProfile> {
        return store.findMany(userIdList)
    }

    override fun updateInfo(userId: Long, name: String, phone: String): Boolean {
        store.updateInfo(userId, name, phone)
        return true
    }

    override fun updatePhoto(userId: Long, photoName: String): Boolean {
        store.updatePhoto(userId, photoName)
        return true
    }
}