package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.model.Fickle
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

    override fun findUserByLogin(login: String): Fickle<User> {
        val cachedUserFickle = userStore.get(login)
        if (cachedUserFickle.isPresent()) {
            return cachedUserFickle
        }

        return userDao.findByLogin(login)
    }

    override fun createUser(user: User) {
        userDao.createNew(user)
    }

    override fun deleteUser(login: String) {
        userStore.delete(login)
        userDao.deleteByLogin(login)
    }
}