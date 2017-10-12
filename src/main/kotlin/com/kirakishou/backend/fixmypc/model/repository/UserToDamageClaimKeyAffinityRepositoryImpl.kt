package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserToDamageClaimKeyAffinityStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserToDamageClaimKeyAffinityRepositoryImpl : UserToDamageClaimKeyAffinityRepository {
    
    @Autowired
    private lateinit var store: UserToDamageClaimKeyAffinityStore
    
    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(ownerId: Long, malfunctionId: Long): Boolean {
        store.saveOne(ownerId, malfunctionId)
        return true
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        return store.findMany(ownerId, offset, count)
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean {
        store.deleteOne(ownerId, malfunctionId)
        return true
    }
}