package com.kirakishou.backend.fixmypc.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kirakishou.backend.fixmypc.FixmypcApplication
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.User
import com.kirakishou.backend.fixmypc.model.net.request.SignupRequest
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserRepository
import com.kirakishou.backend.fixmypc.service.SignupService
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
 * Created by kirakishou on 7/15/2017.
 */

@TestPropertySource(locations = arrayOf("classpath:repositories-test.properties"))
@SpringBootTest(classes = arrayOf(FixmypcApplication::class))
@RunWith(SpringJUnit4ClassRunner::class)
@WebAppConfiguration
class SignupControllerTest {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @Mock
    lateinit var service: SignupService

    @Mock
    lateinit var userRepo: UserRepository

    @Autowired
    @InjectMocks
    lateinit var controller: SignupController

    private val GOOD_LOGIN: String = "test@gmail.com"
    private val GOOD_PASSWORD: String = "1234567890"
    private val ALREADY_EXISTING_LOGIN: String = "alreadyexists@gmail.com"
    private val INCORRECT_LOGIN: String = "testgmail.com"
    private val GOOD_ACCOUNT_TYPE: AccountType = AccountType.Client
    private val TEST_USER = User(GOOD_LOGIN, GOOD_PASSWORD, AccountType.Guest, Timestamp(Date().time))

    private val INCORRECT_PASSWORD_SHORT: String = "123"
    private val INCORRECT_PASSWORD_LONG: String = "123643563463463465687679670-7698-6706783567563486479758075890-78"

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build()

        Mockito.`when`(service.doSignup(GOOD_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE)).thenReturn(SignupService.Result.Ok())
        Mockito.`when`(service.doSignup(ALREADY_EXISTING_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE)).thenReturn(SignupService.Result.LoginAlreadyExists())
        Mockito.`when`(service.doSignup(INCORRECT_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE)).thenReturn(SignupService.Result.LoginIsIncorrect())
        Mockito.`when`(service.doSignup(GOOD_LOGIN, INCORRECT_PASSWORD_SHORT, GOOD_ACCOUNT_TYPE)).thenReturn(SignupService.Result.PasswordIsIncorrect())
        Mockito.`when`(service.doSignup(GOOD_LOGIN, INCORRECT_PASSWORD_LONG, GOOD_ACCOUNT_TYPE)).thenReturn(SignupService.Result.PasswordIsIncorrect())
        Mockito.`when`(userRepo.findByLogin(GOOD_LOGIN)).thenReturn(Optional.of(TEST_USER))
    }

    /*
    *
    * Test case when everything should work fine
    *
    * */
    @Test
    fun shouldSuccessfullyRegisterAndReturn201() {
        val result = mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.SIGNUP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonObjectMapper().writeValueAsString(SignupRequest(GOOD_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE))))
                .andExpect(MockMvcResultMatchers.request().asyncStarted())
                .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(MockMvcResultMatchers.status().isCreated)
    }

    /*
    *
    * Test case when login is already registered
    *
    * */
    @Test
    fun shouldNotRegisterAndReturn409() {
        val result = mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.SIGNUP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonObjectMapper().writeValueAsString(SignupRequest(ALREADY_EXISTING_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE))))
                .andExpect(MockMvcResultMatchers.request().asyncStarted())
                .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(MockMvcResultMatchers.status().isConflict)
    }

    /*
    *
    * Test case when login is incorrect
    *
    * */
    @Test
    fun shouldNotRegisterAndReturn422_loginIsIncorrect() {
        val result = mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.SIGNUP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonObjectMapper().writeValueAsString(SignupRequest(INCORRECT_LOGIN, GOOD_PASSWORD, GOOD_ACCOUNT_TYPE))))
                .andExpect(MockMvcResultMatchers.request().asyncStarted())
                .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    /*
    *
    * Test case when password is too short
    *
    * */
    @Test
    fun shouldNotRegisterAndReturn422_passwordIsTooShort() {
        val result = mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.SIGNUP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonObjectMapper().writeValueAsString(SignupRequest(GOOD_LOGIN, INCORRECT_PASSWORD_SHORT, GOOD_ACCOUNT_TYPE))))
                .andExpect(MockMvcResultMatchers.request().asyncStarted())
                .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    /*
    *
    * Test case when password is too long
    *
    * */
    @Test
    fun shouldNotRegisterAndReturn422_passwordIsTooLong() {
        val result = mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.SIGNUP_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jacksonObjectMapper().writeValueAsString(SignupRequest(GOOD_LOGIN, INCORRECT_PASSWORD_LONG, GOOD_ACCOUNT_TYPE))))
                .andExpect(MockMvcResultMatchers.request().asyncStarted())
                .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }
}