package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode

class LoginResponse(
        @SerializedName(Constant.SerializedNames.SESSION_ID)
        val sessionId: String,

        @SerializedName(Constant.SerializedNames.ACCOUNT_TYPE)
        val accountType: Int,

        @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
        val errorCode: Int
) {
    companion object {
        fun success(sessionId: String, accountType: AccountType): LoginResponse {
            return LoginResponse(sessionId, accountType.value, ServerErrorCode.SEC_OK.value)
        }

        fun fail(errorCode: ServerErrorCode): LoginResponse {
            return LoginResponse("", AccountType.Guest.value, errorCode.value)
        }

    }
}