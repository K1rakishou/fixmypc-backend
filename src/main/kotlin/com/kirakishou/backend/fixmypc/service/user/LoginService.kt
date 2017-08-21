package com.kirakishou.backend.fixmypc.service.user

/**
 * Created by kirakishou on 7/9/2017.
 */
interface LoginService {

    interface Result {
        data class Ok(val sessionId: String): Result
        data class WrongLoginOrPassword(val login: String): Result
    }

    fun doLogin(login: String, password: String): Result
}