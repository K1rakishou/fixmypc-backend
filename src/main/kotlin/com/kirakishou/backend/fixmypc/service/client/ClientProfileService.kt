package com.kirakishou.backend.fixmypc.service.client

import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import com.kirakishou.backend.fixmypc.model.net.request.ClientProfileRequest
import io.reactivex.Single

interface ClientProfileService {

    interface Get {
        interface ResultProfile {
            class Ok(val clientProfile: ClientProfile) : ResultProfile
            class BadAccountType : ResultProfile
            class CouldNotFindProfile : ResultProfile
            class SessionIdExpired : ResultProfile
        }

        interface ResultFilledIn {
            class Ok(val isFilledIn: Boolean) : ResultFilledIn
            class BadAccountType : ResultFilledIn
            class CouldNotFindProfile : ResultFilledIn
            class SessionIdExpired : ResultFilledIn
        }
    }

    interface Post {
        interface Result {
            class Ok : Result
            class BadAccountType : Result
            class SessionIdExpired : Result
        }
    }

    fun isClientProfileFilledIn(sessionId: String): Single<Get.ResultFilledIn>
    fun getClientProfile(sessionId: String): Single<Get.ResultProfile>
    fun updateClientProfile(sessionId: String, profile: ClientProfileRequest): Single<Post.Result>
}