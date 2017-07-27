package com.kirakishou.backend.fixmypc.model.net.response

import com.kirakishou.backend.fixmypc.model.net.StatusCode

class SignupResponse(statusCode: StatusCode) : StatusResponse(statusCode.ordinal)