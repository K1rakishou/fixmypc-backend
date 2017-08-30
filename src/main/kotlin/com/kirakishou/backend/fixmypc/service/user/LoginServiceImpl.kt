package com.kirakishou.backend.fixmypc.service.user

import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserDao
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.UserStore
import com.kirakishou.backend.fixmypc.service.Generator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by kirakishou on 7/9/2017.
 */

@Component
class LoginServiceImpl: LoginService {

    @Autowired
    lateinit var userRepo: UserDao

    @Autowired
    lateinit var generator: Generator

    @Autowired
    lateinit var userStore: UserStore

    override fun doLogin(login: String, password: String): LoginService.Result {
        val userFromCache = userStore.get(login)

        if (userFromCache.isPresent()) {
            if (userFromCache.get().password != password) {
                return LoginService.Result.WrongLoginOrPassword(login)
            }

            return LoginService.Result.Ok(userFromCache.get().sessionId!!)
        }

        val newUserFickle = userRepo.findByLogin(login)
        if (!newUserFickle.isPresent()) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val newUser = newUserFickle.get()
        if (newUser.password != password) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val sessionId = generator.generateSessionId()

        newUser.sessionId = sessionId
        userStore.save(sessionId, newUser)

        return LoginService.Result.Ok(sessionId)
    }
}