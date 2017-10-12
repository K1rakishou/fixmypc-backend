package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import com.kirakishou.backend.fixmypc.model.repository.store.ClientProfileStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientProfileRepositoryImpl : ClientProfileRepository {

    @Autowired
    private lateinit var store: ClientProfileStore

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(clientProfile: ClientProfile): Boolean {
        store.saveOne(clientProfile)
        return true
    }

    override fun findOne(userId: Long): Fickle<ClientProfile> {
        return store.findOne(userId)
    }
}





































