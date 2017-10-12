package com.kirakishou.backend.fixmypc.service.user

import com.kirakishou.backend.fixmypc.model.repository.SessionRepository
import com.kirakishou.backend.fixmypc.model.repository.UserRepository
import com.kirakishou.backend.fixmypc.service.Generator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by kirakishou on 7/9/2017.
 */

@Component
class LoginServiceImpl: LoginService {

    @Autowired
    lateinit var sessionRepository: SessionRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var generator: Generator

    override fun doLogin(login: String, password: String): LoginService.Result {
        val userFickle = userRepository.findOne(login)
        if (!userFickle.isPresent()) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val user = userFickle.get()
        if (user.password != password) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val sessionId = generator.generateSessionId()

        user.sessionId = sessionId
        sessionRepository.saveOne(sessionId, user)

        return LoginService.Result.Ok(sessionId, user.accountType)
    }
}