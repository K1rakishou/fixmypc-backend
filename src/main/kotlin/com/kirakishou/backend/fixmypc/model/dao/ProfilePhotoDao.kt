package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto

interface ProfilePhotoDao {
    fun saveOne(profilePhoto: ProfilePhoto): Either<Throwable, Boolean>
    fun findOne(userId: Long): Either<Throwable, Fickle<ProfilePhoto>>
}