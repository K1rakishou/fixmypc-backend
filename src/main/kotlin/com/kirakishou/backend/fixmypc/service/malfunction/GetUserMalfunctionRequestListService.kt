package com.kirakishou.backend.fixmypc.service.malfunction

import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import io.reactivex.Single

interface GetUserMalfunctionRequestListService {

    interface Get {
        interface Result {
            class Ok(val malfunctionList: List<Malfunction>) : Result
            class SessionIdExpired : Result
        }
    }

    fun getUserMalfunctionRequestList(sessionId: String, offset: Long): Single<Get.Result>
}