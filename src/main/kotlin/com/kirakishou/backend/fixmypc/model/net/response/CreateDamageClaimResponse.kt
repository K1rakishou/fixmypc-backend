package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode

class CreateDamageClaimResponse(
    @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
    val errorCode: Int
) {
    companion object {
        fun success(): CreateDamageClaimResponse {
            return CreateDamageClaimResponse(ServerErrorCode.SEC_OK.value)
        }

        fun fail(errorCode: ServerErrorCode): CreateDamageClaimResponse {
            return CreateDamageClaimResponse(errorCode.value)
        }
    }
}