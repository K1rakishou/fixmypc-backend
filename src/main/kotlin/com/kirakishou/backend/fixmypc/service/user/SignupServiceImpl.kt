package com.kirakishou.backend.fixmypc.service.user

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.repository.ClientProfileRepository
import com.kirakishou.backend.fixmypc.model.repository.UserRepository
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by kirakishou on 7/15/2017.
 */

@Component
class SignupServiceImpl : SignupService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var clientProfileRepository: ClientProfileRepository

    override fun doSignup(login: String, password: String, accountType: AccountType): SignupService.Result {
        if (!TextUtils.checkLoginCorrect(login)) {
            return SignupService.Result.LoginIsIncorrect()
        }

        if (!TextUtils.checkLoginLenCorrect(login)) {
            return SignupService.Result.LoginIsTooLong()
        }

        if (!TextUtils.checkPasswordLenCorrect(password)) {
            return SignupService.Result.PasswordIsIncorrect()
        }

        if (!AccountType.values().contains(accountType)) {
            return SignupService.Result.AccountTypeIsIncorrect()
        }

        val user = userRepository.findOne(login)
        if (user.isPresent()) {
            return SignupService.Result.LoginAlreadyExists()
        }

        val newUser = User(0L, login, password, accountType)
        val daoResult = userRepository.saveOneToDao(newUser)

        if (!daoResult.first) {
            return SignupService.Result.UnknownError()
        }

        if (accountType == AccountType.Client) {
            val newClientProfile = ClientProfile(daoResult.second)
            val repoResult = clientProfileRepository.saveOne(newClientProfile)
            if (!repoResult) {
                return SignupService.Result.UnknownError()
            }
        }

        return SignupService.Result.Ok()
    }
}