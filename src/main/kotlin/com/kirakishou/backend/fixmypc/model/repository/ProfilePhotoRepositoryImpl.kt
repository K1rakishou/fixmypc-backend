package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto
import com.kirakishou.backend.fixmypc.model.repository.ignite.ProfilePhotoCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.ProfilePhotoDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ProfilePhotoRepositoryImpl : ProfilePhotoRepository {

    @Autowired
    private lateinit var dao: ProfilePhotoDao

    @Autowired
    private lateinit var cache: ProfilePhotoCache

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(profilePhoto: ProfilePhoto): Boolean {
        val daoResult = dao.saveOne(profilePhoto)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        return true
    }

    override fun findOne(userId: Long): Fickle<ProfilePhoto> {
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