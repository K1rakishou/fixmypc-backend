package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto

interface ProfilePhotoStore {
    fun saveOne(profilePhoto: ProfilePhoto)
    fun findOne(userId: Long): Fickle<ProfilePhoto>
}