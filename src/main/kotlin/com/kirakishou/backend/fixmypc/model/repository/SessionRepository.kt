package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

interface SessionRepository {
    fun saveOne(sessionId: String, user: User)
    fun findOne(sessionId: String): Fickle<User>
}