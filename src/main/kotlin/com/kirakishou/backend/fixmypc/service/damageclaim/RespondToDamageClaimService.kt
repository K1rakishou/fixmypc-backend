package com.kirakishou.backend.fixmypc.service.damageclaim

import io.reactivex.Single

interface RespondToDamageClaimService {

    interface Post {
        interface Result {
            class Ok : Result
            class SessionIdExpired : Result
            class CouldNotRespondToDamageClaim : Result
            class DamageClaimDoesNotExist : Result
            class BadAccountType: Result
            class DamageClaimIsNotActive : Result
        }
    }

    fun respondToDamageClaim(sessionId: String, damageClaimId: Long): Single<Post.Result>
}