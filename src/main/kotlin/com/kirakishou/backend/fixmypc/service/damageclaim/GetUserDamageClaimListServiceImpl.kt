package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.LocationCache
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GetUserDamageClaimListServiceImpl : GetUserDamageClaimListService {

    @Autowired
    private lateinit var locationCache: LocationCache

    @Autowired
    private lateinit var damageClaimRepository: DamageClaimRepository

    @Autowired
    private lateinit var log: FileLog

    override fun getDamageClaimsWithinRadiusPaged(latParam: Double, lonParam: Double, radiusParam: Double, skipParam: Long, countParam: Long):
            Single<GetUserDamageClaimListService.Get.Result> {

        val lat = when {
            latParam < -180.0 -> -180.0
            latParam > 180.0 -> 180.0
            else -> latParam
        }

        val lon = when {
            lonParam < -90.0 -> -90.0
            lonParam > 90.0 -> 90.0
            else -> lonParam
        }

        val radius = when {
            radiusParam < 1.0 -> 1.0
            radiusParam > 75.0 -> 75.0
            else -> radiusParam
        }

        val skip = when {
            skipParam < 0 -> 0
            else -> skipParam
        }

        val count = when {
            countParam < 0 -> 0
            countParam > Constant.MAX_CLAIMS_PER_PAGE -> Constant.MAX_CLAIMS_PER_PAGE
            else -> countParam
        }

        val idsList = locationCache.findWithin(skip, LatLon(lat, lon), radius, count)
        val damageClaimsList = damageClaimRepository.findMany(idsList)

        return Single.just(GetUserDamageClaimListService.Get.Result.Ok(damageClaimsList))
    }
}











































