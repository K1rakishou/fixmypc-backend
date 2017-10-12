package com.kirakishou.backend.fixmypc.model.cache

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

interface SessionCache {
    fun saveOne(sessionId: String, user: User)
    fun findOne(sessionId: String): Fickle<User>
}