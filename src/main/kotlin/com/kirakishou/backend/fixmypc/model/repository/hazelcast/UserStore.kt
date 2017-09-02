package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

/**
 * Created by kirakishou on 7/11/2017.
 */
interface UserStore {
    fun saveOne(sessionId: String, user: User)
    fun findOne(sessionId: String): Fickle<User>
    fun deleteOne(sessionId: String)
}