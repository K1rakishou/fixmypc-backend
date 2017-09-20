package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant

data class SignupRequest(@SerializedName(Constant.SerializedNames.LOGIN) val login: String,
                         @SerializedName(Constant.SerializedNames.PASSWORD) val password: String,
                         @SerializedName(Constant.SerializedNames.ACCOUNT_TYPE) val accountType: AccountType)