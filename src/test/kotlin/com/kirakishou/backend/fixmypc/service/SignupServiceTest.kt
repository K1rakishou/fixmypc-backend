package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.store.UserStore
import com.kirakishou.backend.fixmypc.service.user.SignupService
import com.kirakishou.backend.fixmypc.service.user.SignupServiceImpl
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.sql.Timestamp
import java.util.*

/**
 * Created by kirakishou on 7/16/2017.
 */


class SignupServiceTest {

    @InjectMocks
    val service = SignupServiceImpl()

    @Mock
    lateinit var userStore: UserStore

    private val GOOD_LOGIN: String = "test@gmail.com"
    private val GOOD_PASSWORD: String = "1234567890"
    private val ALREADY_EXISTING_LOGIN: String = "alreadyexists@gmail.com"
    private val INCORRECT_LOGIN1: String = "testgmail.com"
    private val INCORRECT_LOGIN2: String = "test@gmailcom"
    private val GOOD_ACCOUNT_TYPE: AccountType = AccountType.Client
    private val INCORRECT_PASSWORD_SHORT: String = "123"
    private val INCORRECT_PASSWORD_LONG: String = "123643563463463465687679670-7698-6706783567563486479758075890-78"
    private val TEST_USER = User(0L, GOOD_LOGIN, GOOD_PASSWORD, AccountType.Guest, Timestamp(Date().time))

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(userStore.findOne(ALREADY_EXISTING_LOGIN)).thenReturn(Fickle.of(TEST_USER))
        Mockito.`when`(userStore.findOne(GOOD_LOGIN)).thenReturn(Fickle.empty())
    }

    @Test
    fun shouldRegisterIfLoginAndPasswordAreCorrect() {
        val status = service.doSignup(GOOD_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE)
        assert(status is SignupService.Result.Ok)
    }

    @Test
    fun shouldNotRegisterIfLoginIsIncorrect() {
        val status = service.doSignup(INCORRECT_LOGIN1, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE)
        assert(status is SignupService.Result.LoginIsIncorrect)
    }

    @Test
    fun shouldNotRegisterIfLoginIsIncorrect2() {
        val status = service.doSignup(INCORRECT_LOGIN2, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE)
        assert(status is SignupService.Result.LoginIsIncorrect)
    }

    @Test
    fun shouldNotRegisterIfPasswordIsTooShort() {
        val status = service.doSignup(GOOD_LOGIN, INCORRECT_PASSWORD_SHORT, GOOD_ACCOUNT_TYPE)
        assert(status is SignupService.Result.PasswordIsIncorrect)
    }

    @Test
    fun shouldNotRegisterIfPasswordIsTooLong() {
        val status = service.doSignup(GOOD_LOGIN, INCORRECT_PASSWORD_LONG, GOOD_ACCOUNT_TYPE)
        assert(status is SignupService.Result.PasswordIsIncorrect)
    }


    @Test
    fun shouldNotRegisterIfLoginAlreadyExists() {
        val status = service.doSignup(ALREADY_EXISTING_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE)
        assert(status is SignupService.Result.LoginAlreadyExists)
    }
}