package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode

class SignupResponse(
        @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
        val errorCode: Int
) {
    companion object {
        fun success(): SignupResponse {
            return SignupResponse(ServerErrorCode.SEC_OK.value)
        }

        fun fail(errorCode: ServerErrorCode): SignupResponse {
            return SignupResponse(errorCode.value)
        }
    }
}