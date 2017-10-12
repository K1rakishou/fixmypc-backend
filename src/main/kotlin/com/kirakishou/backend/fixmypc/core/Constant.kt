package com.kirakishou.backend.fixmypc.core

/**
 * Created by kirakishou on 7/16/2017.
 */
object Constant {

    object Paths {
        const val LOGIN_CONTROLLER_PATH = "/v1/api/login"
        const val SIGNUP_CONTROLLER_PATH = "/v1/api/signup"
        const val DAMAGE_CLAIM_CONTROLLER_PATH = "/v1/api/damage_claim"
        const val DAMAGE_CLAIM_PHOTO_CONTROLLER_PATH = "/v1/api/damage_claim_photo"
        const val IMAGE_CONTROLLER_PATH = "/v1/api/image"
        const val CLIENT_CONTROLLER_PATH = "/v1/api/client"
        const val SPECIALIST_CONTROLLER_PATH = "/v1/api/specialist"
    }

    object SerializedNames {
        const val LOGIN = "login"
        const val PASSWORD = "password"
        const val ACCOUNT_TYPE = "account_type"
        const val SESSION_ID = "session_id"
        const val SERVER_ERROR_CODE = "server_error_code"
        const val DAMAGE_CATEGORY = "damage_category"
        const val DAMAGE_DESCRIPTION = "damage_description"

        const val LOCATION_LAT = "lat"
        const val LOCATION_LON = "lon"

        const val ERROR_CODE = "error_code"
        const val BAD_PHOTO_NAMES = "bad_photo_names"

        const val IMAGE_ORIGINAL_NAME = "image_orig_name"
        const val IMAGE_TYPE = "image_type"
        const val IMAGE_NAME = "image_name"
        const val OWNER_ID = "owner_id"
        const val MALFUNCTION_REQUEST_ID = "m_request_id"

        const val DAMAGE_CLAIM_LIST = "damage_claim_list"
        const val CLIENT_PROFILE = "client_profile"

        const val DAMAGE_CLAIM_ID = "damage_claim_id"
        const val SPECIALIST_PROFILES_LIST = "specialist_profiles_list"
        const val SPECIALIST_PROFILE = "specialist_profile"

        const val PROFILE_NAME = "profile_name"
        const val PROFILE_PHONE = "profile_phone"

        const val NEW_SPECIALIST_PROFILE_PHOTO_NAME = "nsp_photo_name"

        const val HAS_SPECIALIST_ALREADY_RESPONDED = "responded"
    }

    object IgniteNames {
        val USER_DAMAGE_CLAIM_CACHE = "user_damage_claim_cache"
        val DAMAGE_CLAIM_CACHE = "damage_claim_cache"
        val DAMAGE_CLAIM_KEYS_CACHE = "damage_claim_keys_cache"
        val USER_CACHE = "user_cache"
        val PHOTO_TO_USER_AFFINITY_CACHE = "photo_to_user_affinity_cache"
        val CLIENT_PROFILE_CACHE = "client_profile_cache"
        val SPECIALIST_PROFILE_CACHE = "specialist_profile_cache"
        val DAMAGE_CLAIM_ASSIGNED_SPECIALIST_CACHE = "damage_claim_assigned_specialist_cache"
        val RESPONDED_SPECIALISTS_CACHE = "responded_specialists_cache"
        val PROFILE_PHOTO_CACHE = "profile_photo_cache"
    }

    object RedisNames {
        val LOCATION_CACHE_NAME = "malfunction_location_cache"
    }

    object TextLength {
        val MAX_LOGIN_LENGTH = 32
        val MIN_PASSWORD_LENGTH = 10
        val MAX_PASSWORD_LENGTH = 20
        val MAX_DESCRIPTION_LENGTH = 500
        val MAX_PROFILE_NAME_LENGTH = 50
        val MAX_PHONE_LENGTH = 15
    }

    val DAMAGE_CLAIM_MAX_IMAGES_PER_REQUEST = 4
    val MAX_CLAIMS_PER_PAGE = 50L
    val HADOOP_TIMEOUT: Long = 10L
}