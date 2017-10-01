package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import io.reactivex.Single

interface GetRespondedSpecialistsService {

    interface Get {
        interface Result {
            class Ok(val responded: List<SpecialistProfile>) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class DamageClaimDoesNotExist : Result
            class DamageClaimIsNotActive : Result
        }
    }

    fun getRespondedSpecialistsPaged(sessionId: String, damageClaimId: Long, skip: Long, count: Long): Single<Get.Result>
}