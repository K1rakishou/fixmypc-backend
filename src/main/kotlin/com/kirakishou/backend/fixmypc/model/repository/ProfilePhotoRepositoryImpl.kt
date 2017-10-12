package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto
import com.kirakishou.backend.fixmypc.model.store.ProfilePhotoStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ProfilePhotoRepositoryImpl : ProfilePhotoRepository {

    @Autowired
    private lateinit var store: ProfilePhotoStore

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(profilePhoto: ProfilePhoto): Boolean {
        store.saveOne(profilePhoto)
        return true
    }

    override fun findOne(userId: Long): Fickle<ProfilePhoto> {
        return store.findOne(userId)
    }
}