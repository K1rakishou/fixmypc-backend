package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import io.reactivex.Single

interface RespondedSpecialistsService {

    interface Get {
        interface Result {
            class Ok(val specialistProfiles: List<SpecialistProfile>) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class DamageClaimDoesNotExist : Result
            class DamageClaimIsNotActive : Result
            class DamageClaimAlreadyHasAssignedSpecialist : Result
        }
    }

    interface Put {
        interface Result {
            class Ok : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
            class CouldNotUpdateRespondedSpecialist : Result
            class CouldNotFindDamageClaim : Result
            class EntityDoesNotBelongToUser : Result
        }
    }

    fun getRespondedSpecialistsPaged(sessionId: String, damageClaimId: Long, skip: Long, count: Long): Single<Get.Result>
    fun markResponseViewed(sessionId: String, damageClaimId: Long, userId: Long): Single<Put.Result>
}