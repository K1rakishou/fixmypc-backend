package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

data class LoginRequest(@SerializedName(Constant.SerializedNames.LOGIN)
                        val login: String,

                        @SerializedName(Constant.SerializedNames.PASSWORD)
                        val password: String)