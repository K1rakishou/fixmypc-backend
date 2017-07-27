package com.kirakishou.backend.fixmypc.model.net.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.AccountType

data class LoginResponse(@JsonProperty("session_id") val sessionId: String,
                         @JsonProperty("account_type") val accountType: Int = AccountType.Guest.ordinal,
                         @JsonProperty("status_code") val statusCode: Int = 0) //: StatusResponse(statusCode.ordinal)