package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

/**
 * Created by kirakishou on 7/9/2017.
 */

interface UserDao {
    fun findByLogin(login: String): Fickle<User>
    fun createNew(user: User)
    fun deleteByLogin(login: String)
}