package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Constant

data class SignupRequest(@JsonProperty(Constant.SerializedNames.LOGIN_SERIALIZED_NAME) val login: String,
                         @JsonProperty(Constant.SerializedNames.PASSWORD_SERIALIZED_NAME) val password: String,
                         @JsonProperty(Constant.SerializedNames.ACCOUNT_TYPE_SERIALIZED_NAME) val accountType: AccountType)