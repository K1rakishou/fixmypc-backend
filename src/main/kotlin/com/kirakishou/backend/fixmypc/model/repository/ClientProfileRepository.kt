package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile

interface ClientProfileRepository {
    fun saveOne(clientProfile: ClientProfile): Boolean
    fun findOne(userId: Long): Fickle<ClientProfile>
}