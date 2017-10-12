package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.entity.User
import org.springframework.beans.factory.annotation.Autowired

class SessionRepositoryImpl : SessionRepository {

    @Autowired
    lateinit var sessionCache: SessionCache

    override fun saveOne(sessionId: String, user: User) {
        sessionCache.saveOne(sessionId, user)
    }

    override fun findOne(sessionId: String): Fickle<User> {
        return sessionCache.findOne(sessionId)
    }
}