package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import com.kirakishou.backend.fixmypc.model.store.DamageClaimStore
import com.kirakishou.backend.fixmypc.model.store.RespondedSpecialistsStore
import com.kirakishou.backend.fixmypc.model.store.SpecialistProfileStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DamageClaimResponseServiceImpl : DamageClaimResponseService {

    @Autowired
    private lateinit var respondedSpecialistsStore: RespondedSpecialistsStore

    @Autowired
    private lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var specialistProfileStore: SpecialistProfileStore

    @Autowired
    private lateinit var log: FileLog

    override fun respondToDamageClaim(sessionId: String, damageClaimId: Long): Single<DamageClaimResponseService.Post.Result> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(DamageClaimResponseService.Post.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Specialist) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(DamageClaimResponseService.Post.Result.BadAccountType())
        }

        val specialistProfileFickle = specialistProfileStore.findOne(user.id)
        if (!specialistProfileFickle.isPresent()) {
            log.d("Could not find specialist profile with id ${user.id}")
            return Single.just(DamageClaimResponseService.Post.Result.CouldNotFindSpecialistProfile())
        }

        val specialistProfile = specialistProfileFickle.get()
        if (!specialistProfile.isProfileInfoFilledIn()) {
            log.d("User with id ${user.id} tried to respond to damage claim with not filled in profile")
            return Single.just(DamageClaimResponseService.Post.Result.ProfileIsNotFilledIn())
        }

        val damageClaimFickle = damageClaimStore.findOne(damageClaimId)
        if (!damageClaimFickle.isPresent()) {
            log.d("DamageClaim with id $damageClaimId does not exist")
            return Single.just(DamageClaimResponseService.Post.Result.DamageClaimDoesNotExist())
        }

        val damageClaim = damageClaimFickle.get()
        if (!damageClaim.isActive) {
            log.d("DamageClaim with id $damageClaimId is not active")
            return Single.just(DamageClaimResponseService.Post.Result.DamageClaimIsNotActive())
        }

        check(user.id != -1L) { "userId should not be -1" }

        val storeResult = respondedSpecialistsStore.saveOne(RespondedSpecialist(damageClaimId = damageClaimId, userId = user.id))
        if (!storeResult) {
            log.d("Couldn't respond to damage claim")
            return Single.just(DamageClaimResponseService.Post.Result.CouldNotRespondToDamageClaim())
        }

        return Single.just(DamageClaimResponseService.Post.Result.Ok())
    }

    override fun hasAlreadyResponded(sessionId: String, damageClaimId: Long): Single<DamageClaimResponseService.Get.Result> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(DamageClaimResponseService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Specialist) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(DamageClaimResponseService.Get.Result.BadAccountType())
        }

        val hasAlreadyResponded = respondedSpecialistsStore.containsOne(damageClaimId, user.id)
        return Single.just(DamageClaimResponseService.Get.Result.Ok(hasAlreadyResponded))
    }
}


































