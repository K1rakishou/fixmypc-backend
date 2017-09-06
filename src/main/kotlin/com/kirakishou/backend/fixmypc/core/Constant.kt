package com.kirakishou.backend.fixmypc.core

/**
 * Created by kirakishou on 7/16/2017.
 */
object Constant {

    object Paths {
        const val LOGIN_CONTROLLER_PATH = "/v1/api/login"
        const val SIGNUP_CONTROLLER_PATH = "/v1/api/signup"
        const val DAMAGE_CLAIM_CONTROLLER_PATH = "/v1/api/damage_claim_request"
        const val DAMAGE_CLAIM_PHOTO_CONTROLLER_PATH = "/v1/api/damage_claim_photo"
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
    }

    object IgniteNames {
        val USER_MALFUNCTION_CACHE_NAME = "user_malfunction_cache"
        val MALFUNCTION_CACHE_NAME = "malfunction_cache"
        val USER_CACHE_NAME = "user_cache"
    }

    object RedisNames {
        val LOCATION_CACHE_NAME = "malfunction_location_cache"
    }

    object ImageTypes {
        val IMAGE_TYPE_MALFUNCTION_PHOTO = 0
    }

    object Url {
        //"http://$host/v1/api/upload_image"
        val SAVE_MALFUNCTION_REQUEST_IMAGE_URL = "http://%s${Paths.DAMAGE_CLAIM_PHOTO_CONTROLLER_PATH}"

        //"http://$host/v1/api/malfunction_image/${ownerId}/${damageClaimRequestId}"
        val DELETE_MALFUNCTION_REQUEST_IMAGES_URL = "http://%s${Paths.DAMAGE_CLAIM_PHOTO_CONTROLLER_PATH}/%d/%s"
    }

    object TextLength {
        val MAX_LOGIN_LENGTH = 32
        val MIN_PASSWORD_LENGTH = 10
        val MAX_PASSWORD_LENGTH = 20
        val MAX_DESCRIPTION_LENGTH = 500
    }

    val DAMAGE_CLAIM_MAX_IMAGES_PER_REQUEST = 4
    val MAX_CLAIMS_PER_PAGE = 5L
}