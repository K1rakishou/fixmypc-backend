package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.store.UserStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl : UserRepository {

    @Autowired
    private lateinit var store: UserStore

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(login: String, user: User): Long {
        return store.saveOne(login, user)
    }

    override fun findOne(login: String): Fickle<User> {
        return store.findOne(login)
    }

    override fun deleteOne(login: String) {
        store.deleteOne(login)
    }
}