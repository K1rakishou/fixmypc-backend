package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile

interface ClientProfileDao {
    suspend fun saveOne(clientProfile: ClientProfile): Boolean
    suspend fun findOne(userId: Long): Fickle<ClientProfile>
    //fun deleteOne(userId: Long): Either<Throwable, Boolean>
}