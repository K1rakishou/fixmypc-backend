package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.PhotoInfoDTO

interface PhotoToUserAffinityRepository {
    fun getOne(imageName: String): Fickle<PhotoInfoDTO>
}