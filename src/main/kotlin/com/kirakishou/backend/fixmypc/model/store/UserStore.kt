package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User

/**
 * Created by kirakishou on 7/11/2017.
 */
interface UserStore {
    fun saveOne(login: String, user: User): Long
    fun findOne(login: String): Fickle<User>
    fun deleteOne(login: String)
}