package com.kirakishou.backend.fixmypc.model.repository.redis

import com.kirakishou.backend.fixmypc.model.User
import java.util.*

/**
 * Created by kirakishou on 7/11/2017.
 */
interface UserCache {
    fun save(key: String, user: User)
    fun get(key: String): Optional<User>
    fun delete(key: String)
}