package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import java.sql.SQLException

/**
 * Created by kirakishou on 7/9/2017.
 */

interface UserDao {
    fun saveOne(user: User): Either<SQLException, Boolean>
    fun findOne(login: String): Either<SQLException, Fickle<User>>
    fun deleteOne(login: String): Either<SQLException, Boolean>
}