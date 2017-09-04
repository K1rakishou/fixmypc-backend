package com.kirakishou.backend.fixmypc.service.malfunction

import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import io.reactivex.Single

interface GetUserMalfunctionRequestListService {

    interface Get {
        interface Result {
            class Ok(val damageClaimList: List<DamageClaim>) : Result
            class SessionIdExpired : Result
        }
    }

    fun getUserMalfunctionRequestList(sessionId: String, offset: Long): Single<Get.Result>
}