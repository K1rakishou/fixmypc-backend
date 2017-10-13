package com.kirakishou.backend.fixmypc.service.user

import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.store.UserStore
import com.kirakishou.backend.fixmypc.service.Generator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by kirakishou on 7/9/2017.
 */

@Component
class LoginServiceImpl: LoginService {

    @Autowired
    lateinit var sessionCache: SessionCache

    @Autowired
    lateinit var userStore: UserStore

    @Autowired
    lateinit var generator: Generator

    override fun doLogin(login: String, password: String): LoginService.Result {
        val userFickle = sessionCache.findOne(login)
        if (!userFickle.isPresent()) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val user = userFickle.get()
        if (user.password != password) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val sessionId = generator.generateSessionId()

        user.sessionId = sessionId
        userStore.saveOne(sessionId, user)

        return LoginService.Result.Ok(sessionId, user.accountType)
    }
}