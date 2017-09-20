package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant

class LoginResponse(@SerializedName(Constant.SerializedNames.SESSION_ID)
                    val sessionId: String,

                    @SerializedName(Constant.SerializedNames.ACCOUNT_TYPE)
                    val accountType: Int = AccountType.Guest.value,

                    @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                    val errorCode: Int)