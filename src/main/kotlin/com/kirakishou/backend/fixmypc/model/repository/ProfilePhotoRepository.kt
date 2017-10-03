package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto

interface ProfilePhotoRepository {
    fun saveOne(profilePhoto: ProfilePhoto): Boolean
    fun findOne(userId: Long): Fickle<ProfilePhoto>
}