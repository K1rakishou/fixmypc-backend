package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.PhotoInfoDTO
import com.kirakishou.backend.fixmypc.model.repository.ignite.PhotoToUserAffinityCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.PhotoToUserAffinityDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PhotoToUserAffinityRepositoryImpl : PhotoToUserAffinityRepository {

    @Autowired
    lateinit var photoInfoCache: PhotoToUserAffinityCache

    @Autowired
    lateinit var photoInfoDao: PhotoToUserAffinityDao

    override fun getOne(imageName: String): Fickle<PhotoInfoDTO> {
        val cacheResult = photoInfoCache.getOne(imageName)
        if (cacheResult.isPresent()) {
            return cacheResult
        }

        val daoResult = photoInfoDao.getOne(imageName)
        if (!daoResult.isPresent()) {
            return Fickle.empty()
        }

        photoInfoCache.saveOne(imageName, daoResult.get())
        return daoResult
    }
}