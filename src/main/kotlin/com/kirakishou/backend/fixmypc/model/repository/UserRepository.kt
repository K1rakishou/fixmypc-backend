package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

interface UserRepository {
    fun saveOne(login: String, user: User): Long
    fun findOne(login: String): Fickle<User>
    fun deleteOne(login: String)
}