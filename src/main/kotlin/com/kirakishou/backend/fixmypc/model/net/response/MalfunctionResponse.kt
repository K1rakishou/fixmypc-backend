package com.kirakishou.backend.fixmypc.model.net.response

import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode

class MalfunctionResponse(errorCode: ServerErrorCode) : StatusResponse(errorCode.value)