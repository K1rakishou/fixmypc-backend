package com.kirakishou.backend.fixmypc.model.net.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode

class LoginResponse(@JsonProperty(Constant.SerializedNames.SESSION_ID_SERIALIZED_NAME)
                         val sessionId: String,

                         @JsonProperty(Constant.SerializedNames.ACCOUNT_TYPE_SERIALIZED_NAME)
                         val accountType: Int = AccountType.Guest.ordinal,

                         errorCode: ServerErrorCode) : StatusResponse(errorCode.value)