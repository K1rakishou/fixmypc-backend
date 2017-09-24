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
        const val IMAGE_CONTROLLER_PATH = "/v1/api/image"
        const val CLIENT_PROFILE_CONTROLLER_PATH = "/v1/api/profile"
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

        const val DAMAGE_CLAIM_ID = "id"
    }

    object IgniteNames {
        val USER_MALFUNCTION_CACHE_NAME = "user_malfunction_cache"
        val MALFUNCTION_CACHE_NAME = "malfunction_cache"
        val USER_CACHE_NAME = "user_cache"
        val PHOTO_TO_USER_AFFINITY_CACHE = "photo_to_user_affinity_cache"
        val CLIENT_PROFILE_CACHE_NAME = "client_profile_cache"
        val DAMAGE_CLAIM_ASSIGNED_SPECIALIST = "damage_claim_assigned_specialist"
        val RESPONDED_SPECIALISTS = "responded_specialists"
    }

    object RedisNames {
        val LOCATION_CACHE_NAME = "malfunction_location_cache"
    }

    object ImageTypes {
        val IMAGE_TYPE_MALFUNCTION_PHOTO = 0
    }

    object Url {
        //"http://$host/v1/api/upload_image"
        val SAVE_DAMAGE_CLAIM_IMAGE_URL = "http://%s${Paths.DAMAGE_CLAIM_PHOTO_CONTROLLER_PATH}"

        //"http://$host/v1/api/malfunction_image/${ownerId}/${folderName}"
        val DELETE_DAMAGE_CLAIM_IMAGES_URL = "http://%s${Paths.DAMAGE_CLAIM_PHOTO_CONTROLLER_PATH}/%d/%s"

        //"/v1/api/damage_claim_photo/{image_type}/{owner_id}/{folder_name}/{image_name:.+}"
        val GET_DAMAGE_CLAIM_IMAGE_URL = "http://%s${Paths.DAMAGE_CLAIM_PHOTO_CONTROLLER_PATH}/%d/%d/%s/%s/%s"
    }

    object TextLength {
        val MAX_LOGIN_LENGTH = 32
        val MIN_PASSWORD_LENGTH = 10
        val MAX_PASSWORD_LENGTH = 20
        val MAX_DESCRIPTION_LENGTH = 500
    }

    val DAMAGE_CLAIM_MAX_IMAGES_PER_REQUEST = 4
    val MAX_CLAIMS_PER_PAGE = 50L
    val FILE_SERVER_REQUEST_TIMEOUT: Long = 7L
}