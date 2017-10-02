package com.kirakishou.backend.fixmypc.service.specialist

import io.reactivex.Single

interface ClientAssignSpecialistService {

    interface Get {
        interface Result {
            class Ok : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class DamageClaimDoesNotExist : Result
            class DamageClaimDoesNotBelongToUser : Result
            class CouldNotRemoveRespondedSpecialists : Result
        }
    }

    fun assignSpecialist(sessionId: String, userId: Long, damageClaimId: Long): Single<Get.Result>
}