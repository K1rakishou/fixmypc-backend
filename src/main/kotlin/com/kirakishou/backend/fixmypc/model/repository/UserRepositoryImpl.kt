package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.UserStore
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
        val userDaoResult = userDao.findOne(login)
        if (userDaoResult !is UserDao.Result.Found) {
            if (userDaoResult is UserDao.Result.NotFound) {
                return Fickle.empty()
            }

            val error = (userDaoResult as UserDao.Result.DbError)
            log.e(error.e)

            return Fickle.empty()
        }

        return Fickle.of(userDaoResult.user)
    }

    override fun saveOneToDao(user: User): Boolean {
        val daoResult = userDao.saveOne(user)

        if (daoResult is UserDao.Result.Saved) {
            return true
        }

        if (daoResult is UserDao.Result.DbError) {
            log.e(daoResult.e)
        }

        return false
    }

    override fun saveOneToStore(sessionId: String, user: User) {
        userStore.saveOne(sessionId, user)
    }

    override fun deleteOneFromDao(login: String): Boolean {
        val daoResult = userDao.deleteOne(login)

        if (daoResult is UserDao.Result.Deleted) {
            return true
        }

        if (daoResult is UserDao.Result.DbError) {
            log.e(daoResult.e)
        }

        return false
    }

    override fun deleteOneFromStore(sessionId: String) {
        userStore.deleteOne(sessionId)
    }
}