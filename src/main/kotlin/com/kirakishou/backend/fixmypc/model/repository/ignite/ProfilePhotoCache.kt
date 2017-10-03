package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto

interface ProfilePhotoCache {
    fun saveOne(profilePhoto: ProfilePhoto)
    fun findOne(userId: Long): Fickle<ProfilePhoto>
}