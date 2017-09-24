package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile

interface ClientProfileDao {
    fun saveOne(clientProfile: ClientProfile): Either<Throwable, Boolean>
    fun findOne(userId: Long): Either<Throwable, Fickle<ClientProfile>>
    //fun deleteOne(userId: Long): Either<Throwable, Boolean>
}