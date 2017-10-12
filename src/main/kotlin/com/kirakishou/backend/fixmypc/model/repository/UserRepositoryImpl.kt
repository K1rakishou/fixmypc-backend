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

    override fun findOne(login: String): Fickle<User> {
        return Fickle.empty()
    }

    override fun saveOneToDao(user: User): Pair<Boolean, Long> {
        return Pair(false, 0L)
    }

    override fun saveOneToStore(sessionId: String, user: User) {
        store.saveOne(sessionId, user)
    }

    override fun deleteOneFromDao(login: String): Boolean {
        return false
    }

    override fun deleteOneFromStore(sessionId: String) {
        store.deleteOne(sessionId)
    }
}