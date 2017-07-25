package com.kirakishou.backend.fixmypc.model.net.response

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResponse(@JsonProperty("session_id") val sessionId: String)