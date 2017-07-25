package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.AccountType

data class SignupRequest(@JsonProperty("login") val login: String,
                         @JsonProperty("password") val password: String,
                         @JsonProperty("account_type") val accountType: AccountType)