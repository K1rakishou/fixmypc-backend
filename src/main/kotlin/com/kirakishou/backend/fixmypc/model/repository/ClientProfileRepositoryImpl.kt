package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import com.kirakishou.backend.fixmypc.model.repository.ignite.ClientProfileCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.ClientProfileDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientProfileRepositoryImpl : ClientProfileRepository {

    @Autowired
    private lateinit var dao: ClientProfileDao

    @Autowired
    private lateinit var cache: ClientProfileCache

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(clientProfile: ClientProfile): Boolean {
        val daoResult = dao.saveOne(clientProfile)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        cache.saveOne(clientProfile)
        return true
    }

    override fun findOne(userId: Long): Fickle<ClientProfile> {
        val cacheResult = cache.findOne(userId)
        if (cacheResult.isPresent()) {
            return cacheResult
        }

        val daoResult = dao.findOne(userId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        } else {
            if (!(daoResult as Either.Value).value.isPresent()) {
                return Fickle.empty()
            }
        }

        cache.saveOne(daoResult.value.get())
        return daoResult.value
    }
}





































