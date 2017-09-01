package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.model.entity.User

/**
 * Created by kirakishou on 7/9/2017.
 */

interface UserDao {

    interface Result {
        class Found(val user: User) : Result
        class NotFound : Result
        class Saved : Result
        class Deleted : Result
        class DbError(val e: Throwable) : Result
    }

    fun saveOne(user: User): Result
    fun findOne(login: String): Result
    fun deleteOne(login: String): Result
}