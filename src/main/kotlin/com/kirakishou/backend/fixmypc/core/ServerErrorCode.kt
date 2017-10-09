package com.kirakishou.backend.fixmypc.core

enum class ServerErrorCode(val value: Int) {
    SEC_OK(0),
    SEC_WRONG_LOGIN_OR_PASSWORD(1),
    SEC_LOGIN_ALREADY_EXISTS(2),
    SEC_LOGIN_IS_INCORRECT(3),
    SEC_PASSWORD_IS_INCORRECT(4),
    SEC_ACCOUNT_TYPE_IS_INCORRECT(5),
    SEC_NO_FILES_WERE_SELECTED_TO_UPLOAD(6),
    SEC_IMAGES_COUNT_EXCEEDED(7),
    SEC_FILE_SIZE_EXCEEDED(8),
    SEC_REQUEST_SIZE_EXCEEDED(9),
    SEC_ALL_FILE_SERVERS_ARE_NOT_WORKING(10),
    SEC_DATABASE_ERROR(11),
    SEC_SESSION_ID_EXPIRED(12),
    SEC_LOGIN_IS_TOO_LONG(13),
    SEC_BAD_ORIGINAL_FILE_NAME(22),
    SEC_COULD_NOT_RESPOND_TO_DAMAGE_CLAIM(24),
    SEC_DAMAGE_CLAIM_DOES_NOT_EXIST(25),
    SEC_BAD_ACCOUNT_TYPE(26),
    SEC_DAMAGE_CLAIM_IS_NOT_ACTIVE(27),
    SEC_COULD_NOT_REMOVE_RESPONDED_SPECIALISTS(28),
    SEC_DAMAGE_CLAIM_DOES_NOT_BELONG_TO_USER(29),
    SEC_COULD_NOT_FIND_PROFILE_WITH_USER_ID(30),
    SEC_COULD_NOT_UPLOAD_IMAGE(31),
    SEC_REPOSITORY_ERROR(32),
    SEC_COULD_NOT_DELETE_OLD_IMAGE(33),

    SEC_UNKNOWN_SERVER_ERROR(-1);

    companion object {
        fun from(value: Int): ServerErrorCode {
            for (code in ServerErrorCode.values()) {
                if (code.value == value) {
                    return code
                }
            }

            throw IllegalArgumentException("Unknown value: $value")
        }
    }
}