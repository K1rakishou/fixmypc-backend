package com.kirakishou.backend.fixmypc.model.net

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