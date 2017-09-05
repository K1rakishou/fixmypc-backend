package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

/**
 * Created by kirakishou on 7/11/2017.
 */
interface UserCache {
    fun saveOne(sessionId: String, user: User)
    fun findOne(sessionId: String): Fickle<User>
    fun deleteOne(sessionId: String)
}