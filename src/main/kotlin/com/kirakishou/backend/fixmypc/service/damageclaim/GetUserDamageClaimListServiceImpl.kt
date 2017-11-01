package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.store.DamageClaimStore
import com.kirakishou.backend.fixmypc.model.store.LocationStore
import com.kirakishou.backend.fixmypc.model.store.RespondedSpecialistsStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GetUserDamageClaimListServiceImpl : GetUserDamageClaimListService {

    @Autowired
    private lateinit var locationStore: LocationStore

    @Autowired
    private lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var respondedSpecialistsStore: RespondedSpecialistsStore

    @Autowired
    private lateinit var log: FileLog

    override fun getDamageClaimsWithinRadiusPaged(sessionId: String, latParam: Double, lonParam: Double,
                                                  radiusParam: Double, skipParam: Long, countParam: Long):
            Single<GetUserDamageClaimListService.Get.PlainResult> {

        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(GetUserDamageClaimListService.Get.PlainResult.SessionIdExpired())
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
        val damageClaimsList = damageClaimStore.findMany(true, idsList)

        return Single.just(GetUserDamageClaimListService.Get.PlainResult.Ok(damageClaimsList))
    }

    override fun getClientDamageClaimsPaged(sessionId: String, isActive: Boolean, skip: Long, count: Long):
            Single<GetUserDamageClaimListService.Get.ResultAndCount> {

        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(GetUserDamageClaimListService.Get.ResultAndCount.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(GetUserDamageClaimListService.Get.ResultAndCount.BadAccountType())
        }

        check(user.id != -1L) { "userId should not be -1" }

        val repoResult = damageClaimStore.findManyPaged(isActive, user.id, skip, count)
        val respondedSpecialists = respondedSpecialistsStore.findMany(repoResult.map { it.id })

        for (specialist in respondedSpecialists) {
            specialist.id = -1L
        }

        return Single.just(GetUserDamageClaimListService.Get.ResultAndCount.Ok(repoResult, respondedSpecialists))
    }
}












































