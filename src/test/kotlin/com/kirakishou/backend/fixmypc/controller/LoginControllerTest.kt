package com.kirakishou.backend.fixmypc.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kirakishou.backend.fixmypc.FixmypcApplication
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.net.request.LoginRequest
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserRepository
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.UserCache
import com.kirakishou.backend.fixmypc.service.Generator
import com.kirakishou.backend.fixmypc.service.user.LoginService
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.sql.Timestamp
import java.util.*


/**
 * Created by kirakishou on 7/9/2017.
 */

@TestPropertySource(locations = arrayOf("classpath:repositories-test.properties"))
@SpringBootTest(classes = arrayOf(FixmypcApplication::class))
@RunWith(SpringJUnit4ClassRunner::class)
@WebAppConfiguration
class LoginControllerTest {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @Mock
    lateinit var service: LoginService

    @Mock
    lateinit var usersCache: UserCache

    @Mock
    lateinit var generator: Generator

    @Mock
    lateinit var userRepo: UserRepository

    @Autowired
    @InjectMocks
    lateinit var controller: LoginController

    private val GOOD_LOGIN: String = "test@gmail.com"
    private val GOOD_PASSWORD: String = "1234567890"
    private val BAD_LOGIN: String = "badlogin@gmail.com"
    private val BAD_PASSWORD: String = "badpassword"
    private val SESSION_ID: String = "123"
    private val TEST_USER = User(0L, GOOD_LOGIN, GOOD_PASSWORD, AccountType.Guest, Timestamp(Date().time))

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build()

        Mockito.`when`(service.doLogin(GOOD_LOGIN, GOOD_PASSWORD)).thenReturn(LoginService.Result.Ok(SESSION_ID))
        Mockito.`when`(service.doLogin(BAD_LOGIN, BAD_PASSWORD)).thenReturn(LoginService.Result.WrongLoginOrPassword(BAD_LOGIN))
        Mockito.`when`(usersCache.get(Mockito.anyString())).thenReturn(Fickle.empty())
        Mockito.`when`(generator.generateSessionId()).thenReturn(SESSION_ID)
        Mockito.`when`(userRepo.findByLogin(GOOD_LOGIN)).thenReturn(Fickle.of(TEST_USER))
    }

    /*
    *
    * Test case when everything should work fine
    *
    * */
    @Test
    fun controllerMustReturn200IfGoodLoginOrPassword() {
        val result = mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.Paths.LOGIN_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonObjectMapper().writeValueAsString(LoginRequest(GOOD_LOGIN, GOOD_PASSWORD))))
                .andExpect(MockMvcResultMatchers.request().asyncStarted())
                .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(SESSION_ID)))
    }

    /*
    *
    * Test case when received bad login or password
    *
    * */
    @Test
    fun controllerMustReturn422IfBadLoginOrPassword() {
        val result = mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.Paths.LOGIN_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonObjectMapper().writeValueAsString(LoginRequest(BAD_LOGIN, BAD_PASSWORD))))
                .andExpect(MockMvcResultMatchers.request().asyncStarted())
                .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }
}





































