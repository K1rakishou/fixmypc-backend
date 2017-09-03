package com.kirakishou.backend.fixmypc.service.user

import com.kirakishou.backend.fixmypc.core.AccountType

/**
 * Created by kirakishou on 7/9/2017.
 */
interface LoginService {

    interface Result {
        data class Ok(val sessionId: String,
                      val accountType: AccountType): Result
        data class WrongLoginOrPassword(val login: String): Result
    }

    fun doLogin(login: String, password: String): Result
}