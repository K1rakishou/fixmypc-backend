package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

/**
 * Created by kirakishou on 7/9/2017.
 */

interface UserDao {
    fun saveOne(user: User): Either<Throwable, Pair<Boolean, Long>>
    fun findOne(login: String): Either<Throwable, Fickle<User>>
    fun deleteOne(login: String): Either<Throwable, Boolean>
}