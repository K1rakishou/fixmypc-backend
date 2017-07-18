package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.model.User
import java.util.*

/**
 * Created by kirakishou on 7/9/2017.
 */

interface UserRepository {
    fun findByLogin(login: String): Optional<User>
    fun createNew(user: User)
    fun deleteByLogin(login: String)
}