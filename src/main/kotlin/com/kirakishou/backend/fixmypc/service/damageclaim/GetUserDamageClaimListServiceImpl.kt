package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.store.LocationStore
import com.kirakishou.backend.fixmypc.model.repository.store.UserStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GetUserDamageClaimListServiceImpl : GetUserDamageClaimListService {

    @Autowired
    private lateinit var locationStore: LocationStore

    @Autowired
    private lateinit var damageClaimRepository: DamageClaimRepository

    @Autowired
    private lateinit var userStore: UserStore

    @Autowired
    private lateinit var log: FileLog

    override fun getDamageClaimsWithinRadiusPaged(sessionId: String, latParam: Double, lonParam: Double,
                                                  radiusParam: Double, skipParam: Long, countParam: Long):
            Single<GetUserDamageClaimListService.Get.Result> {

        val userFickle = userStore.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the specialistProfileStore")
            return Single.just(GetUserDamageClaimListService.Get.Result.SessionIdExpired())
        }

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

        val idsList = locationStore.findWithin(skip, LatLon(lat, lon), radius, count)
        val damageClaimsList = damageClaimRepository.findMany(true, idsList)

        return Single.just(GetUserDamageClaimListService.Get.Result.Ok(damageClaimsList))
    }

    override fun getClientDamageClaimsPaged(sessionId: String, isActive: Boolean, skip: Long, count: Long):
            Single<GetUserDamageClaimListService.Get.Result> {

        val userFickle = userStore.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the specialistProfileStore")
            return Single.just(GetUserDamageClaimListService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(GetUserDamageClaimListService.Get.Result.BadAccountType())
        }

        val repoResult = damageClaimRepository.findMany(isActive, user.id, skip, count)
        return Single.just(GetUserDamageClaimListService.Get.Result.Ok(repoResult))
    }
}












































