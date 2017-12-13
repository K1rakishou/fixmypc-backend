package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import java.sql.Connection

/**
 * Created by kirakishou on 7/9/2017.
 */

interface UserDao : Dao {
    suspend fun saveOne(user: User, connection: Connection): Pair<Boolean, Long>
    suspend fun findOne(login: String, connection: Connection): Fickle<User>
    suspend fun deleteOne(login: String, connection: Connection): Boolean
}