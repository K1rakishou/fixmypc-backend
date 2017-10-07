package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import io.reactivex.Single

interface SpecialistProfileService {

    interface Get {
        interface Result {
            class Ok(val profile: SpecialistProfile) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class NotFound : Result
        }
    }

    fun getProfile(sessionId: String): Single<Get.Result>
}