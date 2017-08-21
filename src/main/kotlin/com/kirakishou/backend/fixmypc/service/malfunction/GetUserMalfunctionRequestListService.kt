package com.kirakishou.backend.fixmypc.service.malfunction

import io.reactivex.Single

interface GetUserMalfunctionRequestListService {

    interface Get {
        interface Result {
            class Ok : Result
            class SessionIdExpired : Result
        }
    }

    fun getUserMalfunctionRequestList(sessionId: String): Single<Get.Result>
}