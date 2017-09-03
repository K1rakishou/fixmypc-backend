package com.kirakishou.backend.fixmypc.service.user

import com.kirakishou.backend.fixmypc.core.AccountType

/**
 * Created by kirakishou on 7/15/2017.
 */
interface SignupService {

    interface Result {
        class Ok: Result
        class LoginIsIncorrect: Result
        class PasswordIsIncorrect: Result
        class AccountTypeIsIncorrect: Result
        class LoginAlreadyExists: Result
        class LoginIsTooLong : Result
    }

    fun doSignup(login: String, password: String, accountType: AccountType): Result
}