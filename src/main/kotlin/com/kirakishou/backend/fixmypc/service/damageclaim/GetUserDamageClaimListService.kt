package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import io.reactivex.Single

interface GetUserDamageClaimListService {

    interface Get {
        interface Result {
            class Ok(val damageClaimList: List<DamageClaim>) : Result
            class SessionIdExpired : Result
            class BadAccountType : Result
        }
    }

    fun getDamageClaimsWithinRadiusPaged(sessionId: String, latParam: Double, lonParam: Double,
                                         radiusParam: Double, skipParam: Long, countParam: Long): Single<Get.Result>

    fun getClientDamageClaimsPaged(sessionId: String, isActive: Boolean, skip: Long, count: Long): Single<Get.Result>
}