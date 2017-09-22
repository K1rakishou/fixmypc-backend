package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.dto.PhotoInfoDTO

interface PhotoToUserAffinityDao {
    fun getOne(imageName: String): Either<Throwable, Fickle<PhotoInfoDTO>>
}