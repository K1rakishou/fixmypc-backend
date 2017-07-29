package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.Constant

data class LoginRequest(@JsonProperty(Constant.SerializedNames.LOGIN_SERIALIZED_NAME) val login: String,
                        @JsonProperty(Constant.SerializedNames.PASSWORD_SERIALIZED_NAME) val password: String)