package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl : UserRepository {

    @Autowired
    private lateinit var dao: UserDao

    @Autowired
    private lateinit var cache: UserCache

    @Autowired
    private lateinit var log: FileLog

    override fun findOne(login: String): Fickle<User> {
        val daoResult = dao.findOne(login)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        }

        return (daoResult as Either.Value).value
    }

    override fun saveOneToDao(user: User): Pair<Boolean, Long> {
        val daoResult = dao.saveOne(user)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false to 0L
        } else {
            if (!(daoResult as Either.Value).value.first) {
                return false to 0L
            }
        }

        return daoResult.value
    }

    override fun saveOneToStore(sessionId: String, user: User) {
        cache.saveOne(sessionId, user)
    }

    override fun deleteOneFromDao(login: String): Boolean {
        val daoResult = dao.deleteOne(login)

        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        return daoResult.value
    }

    override fun deleteOneFromStore(sessionId: String) {
        cache.deleteOne(sessionId)
    }
}