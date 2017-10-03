package com.kirakishou.backend.fixmypc.service.damageclaim

import io.reactivex.Single

interface DamageClaimResponseService {

    interface Post {
        interface Result {
            class Ok : Result
            class SessionIdExpired : Result
            class CouldNotRespondToDamageClaim : Result
            class DamageClaimDoesNotExist : Result
            class BadAccountType : Result
            class DamageClaimIsNotActive : Result
        }
    }

    interface Get {
        interface Result {
            class Ok(val responded: Boolean) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
        }
    }

    fun respondToDamageClaim(sessionId: String, damageClaimId: Long): Single<Post.Result>
    fun hasAlreadyResponded(sessionId: String, damageClaimId: Long): Single<Get.Result>
}