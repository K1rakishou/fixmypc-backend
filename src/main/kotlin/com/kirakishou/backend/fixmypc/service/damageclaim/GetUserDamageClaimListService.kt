package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import io.reactivex.Single

interface GetUserDamageClaimListService {

    interface Get {
        interface PlainResult {
            class Ok(val damageClaimList: List<DamageClaim>) : PlainResult
            class SessionIdExpired : PlainResult
            class BadAccountType : PlainResult
        }

        interface ResultAndCount {
            class Ok(val damageClaimList: List<DamageClaim>,
                     val responsesCountList: List<RespondedSpecialist>) : ResultAndCount
            class SessionIdExpired : ResultAndCount
            class BadAccountType : ResultAndCount
        }
    }

    fun getDamageClaimsWithinRadiusPaged(sessionId: String, latParam: Double, lonParam: Double,
                                         radiusParam: Double, skipParam: Long, countParam: Long): Single<Get.PlainResult>

    fun getClientDamageClaimsPaged(sessionId: String, isActive: Boolean, skip: Long, count: Long): Single<Get.ResultAndCount>
}