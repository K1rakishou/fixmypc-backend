package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.dto.PhotoInfoDTO
import com.kirakishou.backend.fixmypc.model.repository.ignite.PhotoToUserAffinityCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.PhotoToUserAffinityDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PhotoToUserAffinityRepositoryImpl : PhotoToUserAffinityRepository {

    @Autowired
    lateinit var cache: PhotoToUserAffinityCache

    @Autowired
    lateinit var dao: PhotoToUserAffinityDao

    @Autowired
    private lateinit var log: FileLog

    override fun getOne(imageName: String): Fickle<PhotoInfoDTO> {
        val cacheResult = cache.getOne(imageName)
        if (cacheResult.isPresent()) {
            return cacheResult
        }

        val daoResult = dao.findOne(imageName)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        } else {
            if (!(daoResult as Either.Value).value.isPresent()) {
                return Fickle.empty()
            }
        }

        return daoResult.value
    }
}