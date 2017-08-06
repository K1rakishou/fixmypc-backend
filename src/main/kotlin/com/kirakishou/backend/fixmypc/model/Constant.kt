package com.kirakishou.backend.fixmypc.model

/**
 * Created by kirakishou on 7/16/2017.
 */
object Constant {

    object Paths {
        const val LOGIN_CONTROLLER_PATH = "/v1/api/login"
        const val SIGNUP_CONTROLLER_PATH = "/v1/api/signup"
        const val MALFUNCTION_REQUEST_CONTROLLER_PATH = "/v1/api/m_request"
    }

    object SerializedNames {
        const val LOGIN_SERIALIZED_NAME = "login"
        const val PASSWORD_SERIALIZED_NAME = "password"
        const val ACCOUNT_TYPE_SERIALIZED_NAME = "account_type"
        const val SESSION_ID_SERIALIZED_NAME = "session_id"
        const val SERVER_ERROR_CODE_SERIALIZED_NAME = "server_error_code"
        const val MALFUNCTION_CATEGORY = "m_category"
        const val MALFUNCTION_DESCRIPTION = "m_description"
    }

    object FileServers {
        val SERVERS_IPS = arrayListOf("127.0.0.1:9119")
    }

    val MIN_PASSWORD_LENGTH = 10
    val MAX_PASSWORD_LENGTH = 32
    val USER_CACHE_LIFE_TIME_SECONDS = 60L
}