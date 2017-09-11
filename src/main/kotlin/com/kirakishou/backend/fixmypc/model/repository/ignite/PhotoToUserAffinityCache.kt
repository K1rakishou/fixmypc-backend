package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.PhotoInfoDTO

interface PhotoToUserAffinityCache {
    fun getOne(imageName: String): Fickle<PhotoInfoDTO>
    fun saveOne(imageName: String, info: PhotoInfoDTO)
}