package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile

interface ClientProfileStore {
    fun saveOne(clientProfile: ClientProfile)
    fun findOne(userId: Long): Fickle<ClientProfile>
    //fun deleteOne(userId: Long)
}