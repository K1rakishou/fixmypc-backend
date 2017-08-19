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

        const val ERROR_CODE = "error_code"
        const val BAD_PHOTO_NAMES = "bad_photo_names"

        const val IMAGE_ORIGINAL_NAME = "image_orig_name"
        const val IMAGE_TYPE = "image_type"
        const val IMAGE_NAME = "image_name"
        const val OWNER_ID = "owner_id"
        const val MALFUNCTION_REQUEST_ID = "malfunction_request_id"
    }

    object FileServers {
        val SERVERS_IPS = arrayListOf("127.0.0.1:9119")
    }

    object HazelcastNames {
        val USER_CACHE_KEY = "user_cache"
    }

    object ImageTypes {
        val IMAGE_TYPE_MALFUNCTION_PHOTO = 0
    }

    val MIN_PASSWORD_LENGTH = 10
    val MAX_PASSWORD_LENGTH = 32
    val MALFUNCTION_MAX_IMAGES_PER_REQUEST = 4
    val USER_CACHE_LIFE_TIME_SECONDS = 60L
}