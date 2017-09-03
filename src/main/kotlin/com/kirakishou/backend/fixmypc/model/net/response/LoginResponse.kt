package com.kirakishou.backend.fixmypc.model.net.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant

class LoginResponse(@JsonProperty(Constant.SerializedNames.SESSION_ID)
                    val sessionId: String,

                    @JsonProperty(Constant.SerializedNames.ACCOUNT_TYPE)
                    val accountType: Int = AccountType.Guest.value,

                    @JsonProperty(Constant.SerializedNames.SERVER_ERROR_CODE)
                    val errorCode: Int)