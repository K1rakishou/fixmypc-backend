package com.kirakishou.backend.fixmypc.service.user

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
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var generator: Generator

    override fun doLogin(login: String, password: String): LoginService.Result {
        val newUserFickle = userRepository.findOne(login)
        if (!newUserFickle.isPresent()) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val newUser = newUserFickle.get()
        if (newUser.password != password) {
            return LoginService.Result.WrongLoginOrPassword(login)
        }

        val sessionId = generator.generateSessionId()

        newUser.sessionId = sessionId
        userRepository.saveOneToStore(sessionId, newUser)

        return LoginService.Result.Ok(sessionId)
    }
}