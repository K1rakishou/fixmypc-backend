package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

/**
 * Created by kirakishou on 7/9/2017.
 */

interface UserDao {
    suspend fun saveOne(user: User): Pair<Boolean, Long>
    suspend fun findOne(login: String): Fickle<User>
    suspend fun deleteOne(login: String): Boolean
}