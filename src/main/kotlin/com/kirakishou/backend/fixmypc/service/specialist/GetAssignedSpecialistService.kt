package com.kirakishou.backend.fixmypc.service.specialist

import io.reactivex.Single

interface GetAssignedSpecialistService {

    interface Get {
        interface Result {
            class Ok(val specialistUserId: Long) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class CouldNotFindAssignedSpecialist : Result
            class SpecialistWasNotAssignedByCurrentUser : Result
        }
    }

    fun getAssignedSpecialist(sessionId: String, damageClaimId: Long): Single<Get.Result>
}