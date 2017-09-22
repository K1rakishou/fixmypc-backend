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
    private lateinit var clientProfileDao: ClientProfileDao

    @Autowired
    private lateinit var clientProfileCache: ClientProfileCache

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(clientProfile: ClientProfile): Boolean {
        val daoResult = clientProfileDao.saveOne(clientProfile)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        clientProfileCache.saveOne(clientProfile)
        return true
    }

    override fun findOne(userId: Long): Fickle<ClientProfile> {
        val cacheResult = clientProfileCache.findOne(userId)
        if (cacheResult.isPresent()) {
            return cacheResult
        }

        val daoResult = clientProfileDao.findOne(userId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        } else {
            if (!(daoResult as Either.Value).value.isPresent()) {
                return Fickle.empty()
            }
        }

        clientProfileCache.saveOne(daoResult.value.get())
        return daoResult.value
    }
}





































