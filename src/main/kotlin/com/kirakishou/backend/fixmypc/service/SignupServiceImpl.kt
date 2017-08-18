package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserRepository
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.util.*

/**
 * Created by kirakishou on 7/15/2017.
 */

@Component
class SignupServiceImpl : SignupService {

    @Autowired
    lateinit var userRepo: UserRepository

    override fun doSignup(login: String, password: String, accountType: AccountType): SignupService.Result {
        if (!TextUtils.checkLoginCorrect(login)) {
            return SignupService.Result.LoginIsIncorrect()
        }

        if (!TextUtils.checkPasswordLenCorrect(password)) {
            return SignupService.Result.PasswordIsIncorrect()
        }

        if (!AccountType.values().contains(accountType)) {
            return SignupService.Result.AccountTypeIsIncorrect()
        }

        val user = userRepo.findByLogin(login)
        if (user.isPresent()) {
            return SignupService.Result.LoginAlreadyExists()
        }

        val newUser = User(login, password, accountType, Timestamp(Date().time))
        userRepo.createNew(newUser)

        return SignupService.Result.Ok()
    }
}