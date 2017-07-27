package com.kirakishou.backend.fixmypc.model

/**
 * Created by kirakishou on 7/16/2017.
 */
object Constant {
    const val LOGIN_CONTROLLER_PATH = "/v1/api/login"
    const val SIGNUP_CONTROLLER_PATH = "/v1/api/signup"

    val MIN_PASSWORD_LENGTH = 10
    val MAX_PASSWORD_LENGTH = 32
    val USER_CACHE_LIFE_TIME_SECONDS = 60L
}