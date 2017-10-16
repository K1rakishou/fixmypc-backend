package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.store.UserStore
import com.kirakishou.backend.fixmypc.service.user.LoginService
import com.kirakishou.backend.fixmypc.service.user.LoginServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Created by kirakishou on 7/11/2017.
 */

class LoginServiceTest {

    @InjectMocks
    val service = LoginServiceImpl()

    @Mock
    lateinit var userStore: UserStore

    @Mock
    lateinit var generator: Generator

    private val GOOD_LOGIN: String = "test@gmail.com"
    private val GOOD_PASSWORD: String = "1234567890"
    private val GOOD_SESSION_ID: String = "abcdef1234567890"
    private val TEST_USER = User(0L, GOOD_LOGIN, GOOD_PASSWORD, AccountType.Guest)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(userStore.findOne(GOOD_LOGIN)).thenReturn(Fickle.of(TEST_USER))
        Mockito.`when`(userStore.findOne(BAD_LOGIN)).thenReturn(Fickle.empty())
        Mockito.`when`(generator.generateSessionId()).thenReturn(GOOD_SESSION_ID)
    }

    @Test
    fun shouldLoginIfCredentialsAreGood() {
        val status = service.doLogin(GOOD_LOGIN, GOOD_PASSWORD)
        assertEquals(LoginService.Result.Ok(GOOD_SESSION_ID, AccountType.Guest), status)
    }

    private val BAD_LOGIN: String = "badlogin@gmail.com"
    private val BAD_PASSWORD: String = "badpassword"

    @Test
    fun shouldNotLoginIfCredentialsAreBad() {
        val status = service.doLogin(BAD_LOGIN, BAD_PASSWORD)
        assertEquals(LoginService.Result.WrongLoginOrPassword(BAD_LOGIN), status)
    }
}











