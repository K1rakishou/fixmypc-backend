package com.kirakishou.backend.fixmypc

import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.User
import com.kirakishou.backend.fixmypc.model.net.request.LoginRequest
import com.kirakishou.backend.fixmypc.model.net.response.LoginResponse
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserRepository
import com.kirakishou.backend.fixmypc.model.repository.redis.UserCache
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.sql.Timestamp
import java.util.*

@TestPropertySource(locations = arrayOf("classpath:repositories-test.properties"))
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginTest {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userRepo: UserRepository

    @Autowired
    lateinit var userCache: UserCache

    @Before
    fun init() {
        userRepo.deleteByLogin(GOOD_LOGIN)
        userCache.delete(GOOD_LOGIN)
    }

    @After
    fun tearDown() {
        userRepo.deleteByLogin(GOOD_LOGIN)
        userCache.delete(GOOD_LOGIN)
    }

    private val GOOD_LOGIN: String = "test2@gmail.com"
    private val PASSWORD: String = "12345678990"
    private val TEST_USER = User(GOOD_LOGIN, PASSWORD, AccountType.Guest, Timestamp(Date().time))

    @Test
    fun login() {
        userRepo.createNew(TEST_USER)

        val responseEntity = restTemplate.postForEntity(Constant.Paths.LOGIN_CONTROLLER_PATH,
                LoginRequest(GOOD_LOGIN, PASSWORD),
                LoginResponse::class.java)

        val response = responseEntity.body as LoginResponse
        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertEquals(response.sessionId.isEmpty(), false)

        val userInCache = userCache.get(GOOD_LOGIN)
        assertEquals(userInCache.isPresent, true)
        assertEquals(userInCache.get().login, GOOD_LOGIN)
        assertEquals(userInCache.get().password, PASSWORD)
    }
}
























