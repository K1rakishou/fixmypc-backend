package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant

data class SignupRequest(@JsonProperty(Constant.SerializedNames.LOGIN) val login: String,
                         @JsonProperty(Constant.SerializedNames.PASSWORD) val password: String,
                         @JsonProperty(Constant.SerializedNames.ACCOUNT_TYPE) val accountType: AccountType)