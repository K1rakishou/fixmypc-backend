package com.kirakishou.backend.fixmypc.model

enum class FileServerErrorCode(val value: Int) {
    OK(0),
    COULD_NOT_STORE_ONE_OR_MORE_IMAGES(1),
    UNKNOWN_ERROR(2),
    REQUEST_TIMEOUT(3),
    NO_WORKING_FILESERVERS(4);

    companion object {
        fun from(value: Int): FileServerErrorCode {
            for (code in FileServerErrorCode.values()) {
                if (code.value == value) {
                    return code
                }
            }

            throw IllegalArgumentException("Unknown value: $value")
        }
    }
}