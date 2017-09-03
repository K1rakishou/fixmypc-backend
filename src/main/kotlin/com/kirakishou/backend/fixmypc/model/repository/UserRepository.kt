package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

interface UserRepository {
    fun findOne(login: String): Fickle<User>
    fun saveOneToDao(user: User): Boolean
    fun saveOneToStore(sessionId: String, user: User)
    fun deleteOneFromDao(login: String): Boolean
    fun deleteOneFromStore(sessionId: String)
}