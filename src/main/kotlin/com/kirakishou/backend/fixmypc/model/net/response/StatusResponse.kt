package com.kirakishou.backend.fixmypc.model.net.response

import com.fasterxml.jackson.annotation.JsonProperty

abstract class StatusResponse(@JsonProperty("status_code") val statusCode: Int)