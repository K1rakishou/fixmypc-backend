package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import io.reactivex.Single

interface GetAssignedSpecialistService {

    interface Get {
        interface Result {
            class Ok(val specialistProfile: SpecialistProfile) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class CouldNotFindAssignedSpecialist : Result
            class SpecialistWasNotAssignedByCurrentUser : Result
            class CouldNotFindSpecialistProfile : Result
        }
    }

    fun getRespondedSpecialist(sessionId: String, damageClaimId: Long): Single<Get.Result>
}