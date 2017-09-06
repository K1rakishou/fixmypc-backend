package com.kirakishou.backend.fixmypc.service.malfunction

import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import io.reactivex.Single

interface GetUserDamageClaimListService {

    interface Get {
        interface Result {
            class Ok(val damageClaimList: List<DamageClaim>) : Result
            class SessionIdExpired : Result
            class BadLatitude : Result
            class BadLongitude : Result
            class RadiusIsTooSmall : Result
            class RadiusIsTooLarge : Result
            class BadPage : Result
            class ParamsAreOk : Result
        }
    }

    fun getDamageClaimsWithinRadiusPaged(lat: Double, lon: Double, radius: Double, page: Long): Single<Get.Result>
}