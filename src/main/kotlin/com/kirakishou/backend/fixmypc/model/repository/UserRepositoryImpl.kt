package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserStore
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl : UserRepository {

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var userStore: UserStore

    @Autowired
    private lateinit var log: FileLog

    override fun findOne(login: String): Fickle<User> {
        val daoResult = userDao.findOne(login)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        }

        return (daoResult as Either.Value).value
    }

    override fun saveOneToDao(user: User): Boolean {
        val daoResult = userDao.saveOne(user)
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

    override fun saveOneToStore(sessionId: String, user: User) {
        userStore.saveOne(sessionId, user)
    }

    override fun deleteOneFromDao(login: String): Boolean {
        val daoResult = userDao.deleteOne(login)

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
        userStore.deleteOne(sessionId)
    }
}