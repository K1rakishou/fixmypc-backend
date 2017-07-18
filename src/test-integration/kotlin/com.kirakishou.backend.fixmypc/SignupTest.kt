package com.kirakishou.backend.fixmypc

import com.kirakishou.backend.fixmypc.controller.SignupController
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserRepository
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


@TestPropertySource(locations = arrayOf("classpath:repositories-test.properties"))
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class SignupTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userRepo: UserRepository

    @Before
    fun init() {
        userRepo.deleteByLogin(GOOD_LOGIN)
    }

    @After
    fun tearDown() {
        userRepo.deleteByLogin(GOOD_LOGIN)
    }

    private val GOOD_LOGIN: String = "test2@gmail.com"
    private val PASSWORD: String = "12345678990"

    /*
    *
    * Should successfully sign up and create new record in the database
    *
    * */
    @Test
    fun signup() {
        val responseEntity = restTemplate.postForEntity("/signup",
                SignupController.SignupRequest(GOOD_LOGIN, PASSWORD, AccountType.Client),
                SignupController.SignupResponse::class.java)

        val response = responseEntity.body as SignupController.SignupResponse
        assertEquals(HttpStatus.CREATED, responseEntity.statusCode)

        val userInDb = userRepo.findByLogin(GOOD_LOGIN)
        assertEquals(userInDb.isPresent, true)
        assertEquals(userInDb.get().login, GOOD_LOGIN)
        assertEquals(userInDb.get().password, PASSWORD)
    }
}

















