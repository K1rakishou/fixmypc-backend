package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import java.sql.SQLException

interface ClientProfileDao {
    fun saveOne(clientProfile: ClientProfile): Either<SQLException, Boolean>
    fun findOne(userId: Long): Either<SQLException, Fickle<ClientProfile>>
    fun deleteOne(userId: Long): Either<SQLException, Boolean>
}