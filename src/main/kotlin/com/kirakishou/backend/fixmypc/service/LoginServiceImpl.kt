package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserRepository
import com.kirakishou.backend.fixmypc.model.repository.redis.UserCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by kirakishou on 7/9/2017.
 */

@Component
class LoginServiceImpl: LoginService {

    @Autowired
    lateinit var userRepo: UserRepository

    @Autowired
    lateinit var generator: Generator

    @Autowired
    lateinit var userCache: UserCache

    override fun doLogin(login: String, password: String): LoginService.Result {
        val userFromCache = userCache.get(login)

        if (userFromCache.isPresent()) {
            if (userFromCache.get().password != password) {
                return LoginService.Result.WrongLoginOrPassword(login)
            }

            return LoginService.Result.Ok(userFromCache.get().sessionId!!)
        }

        val newUserOpt = userRepo.findByLogin(login)
        if (!newUserOpt.isPresent()) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val newUser = newUserOpt.get()
        if (newUser.password != password) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val sessionId = generator.generateSessionId()

        newUser.sessionId = sessionId
        userCache.save(login, newUser)

        return LoginService.Result.Ok(sessionId)
    }
}