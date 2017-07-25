package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginRequest(@JsonProperty("login") val login: String,
                        @JsonProperty("password") val password: String)