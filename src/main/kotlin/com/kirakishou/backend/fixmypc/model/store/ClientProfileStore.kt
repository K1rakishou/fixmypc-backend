package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile

interface ClientProfileStore {
    fun saveOne(clientProfile: ClientProfile): Boolean
    fun findOne(userId: Long): Fickle<ClientProfile>
    fun update(userId: Long, name: String, phone: String): Boolean
    fun deleteOne(userId: Long): Boolean
}