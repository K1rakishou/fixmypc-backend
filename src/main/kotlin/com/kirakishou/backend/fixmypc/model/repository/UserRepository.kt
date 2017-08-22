package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

interface UserRepository {
    fun findUserByLogin(login: String): Fickle<User>
    fun createUser(user: User)
    fun deleteUser(login: String)
}