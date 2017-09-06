package com.kirakishou.backend.fixmypc.service.malfunction

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

    override fun getDamageClaimsWithinRadiusPaged(lat: Double, lon: Double, radius: Double, page: Long):
            Single<GetUserDamageClaimListService.Get.Result> {

        val checkResult = checkInputs(lat, lon, radius, page)
        if (checkResult !is GetUserDamageClaimListService.Get.Result.ParamsAreOk) {
            log.d("One of the input parameters is bad")
            return Single.just(checkResult)
        }

        val idsList = locationCache.findWithin(page, LatLon(lat, lon), radius, Constant.MAX_CLAIMS_PER_PAGE)
        val damageClaimsList = damageClaimRepository.findMany(idsList.map { it.id })

        return Single.just(GetUserDamageClaimListService.Get.Result.Ok(damageClaimsList))
    }

    private fun checkInputs(lat: Double, lon: Double, radius: Double, page: Long): GetUserDamageClaimListService.Get.Result {
        if (lat < -180.0) {
            return GetUserDamageClaimListService.Get.Result.BadLatitude()
        }

        if (lat > 180.0) {
            return GetUserDamageClaimListService.Get.Result.BadLatitude()
        }

        if (lon < -90.0) {
            return GetUserDamageClaimListService.Get.Result.BadLongitude()
        }

        if (lon > 90.0) {
            return GetUserDamageClaimListService.Get.Result.BadLongitude()
        }

        if (radius < 1.0) {
            return GetUserDamageClaimListService.Get.Result.RadiusIsTooSmall()
        }

        if (radius > 75.0) {
            return GetUserDamageClaimListService.Get.Result.RadiusIsTooLarge()
        }

        if (page < 0) {
            return GetUserDamageClaimListService.Get.Result.BadPage()
        }

        return GetUserDamageClaimListService.Get.Result.ParamsAreOk()
    }
}