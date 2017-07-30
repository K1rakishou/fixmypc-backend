package com.kirakishou.backend.fixmypc.model.net.response

import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode

class SignupResponse(serverErrorCode: ServerErrorCode) : StatusResponse(serverErrorCode.ordinal)