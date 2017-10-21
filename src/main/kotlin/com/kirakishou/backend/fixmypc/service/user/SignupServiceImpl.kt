package com.kirakishou.backend.fixmypc.service.user

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.store.ClientProfileStore
import com.kirakishou.backend.fixmypc.model.store.SpecialistProfileStore
import com.kirakishou.backend.fixmypc.model.store.UserStore
import com.kirakishou.backend.fixmypc.util.ServerUtils
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * Created by kirakishou on 7/15/2017.
 */

@Component
class SignupServiceImpl : SignupService {

    @Autowired
    lateinit var userStore: UserStore

    @Autowired
    lateinit var clientProfileStore: ClientProfileStore

    @Autowired
    lateinit var specialistProfileStore: SpecialistProfileStore

    @PostConstruct
    fun initForTest() {
        /*doSignup("client1@gmail.com", "1234567890", AccountType.Client)
        doSignup("client2@gmail.com", "1234567890", AccountType.Client)
        doSignup("specialist1@gmail.com", "1234567890", AccountType.Specialist)
        doSignup("specialist2@gmail.com", "1234567890", AccountType.Specialist)
        doSignup("specialist3@gmail.com", "1234567890", AccountType.Specialist)
        doSignup("specialist4@gmail.com", "1234567890", AccountType.Specialist)

        val allUsers = userStore.findAll()
        for (user in allUsers) {
            println(user)
        }*/
    }

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

        val user = userStore.findOne(login)
        if (user.isPresent()) {
            return SignupService.Result.LoginAlreadyExists()
        }

        val currentTime =  ServerUtils.getTimeFast()
        val newUser = User(0L, login, password, accountType)

        val userId = userStore.saveOne(login, newUser)
        if (userId == -1L) {
            return SignupService.Result.StoreError()
        }

        if (accountType == AccountType.Client) {
            if (!clientProfileStore.saveOne(ClientProfile(userId = userId, registeredOn = currentTime))) {
                return SignupService.Result.StoreError()
            }
        } else if (accountType == AccountType.Specialist) {
            if (!specialistProfileStore.saveOne(SpecialistProfile(userId = userId, registeredOn =currentTime))) {
                return SignupService.Result.StoreError()
            }
        }

        return SignupService.Result.Ok()
    }
}

































