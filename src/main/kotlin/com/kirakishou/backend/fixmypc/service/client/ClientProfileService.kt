package com.kirakishou.backend.fixmypc.service.client

import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import io.reactivex.Single

interface ClientProfileService {

    interface Get {
        interface Result {
            class Ok(val clientProfile: ClientProfile) : Result
            class CouldNotFindProfile : Result
            class SessionIdExpired : Result
        }
    }

    fun getClientProfile(sessionId: String, userId: Long): Single<Get.Result>
}