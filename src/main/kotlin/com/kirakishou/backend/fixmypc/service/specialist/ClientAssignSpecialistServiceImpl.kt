package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import com.kirakishou.backend.fixmypc.model.store.AssignedSpecialistStore
import com.kirakishou.backend.fixmypc.model.store.DamageClaimStore
import com.kirakishou.backend.fixmypc.model.store.RespondedSpecialistsStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientAssignSpecialistServiceImpl : ClientAssignSpecialistService {

    @Autowired
    private lateinit var assignedSpecialistsStore: AssignedSpecialistStore

    @Autowired
    private lateinit var respondedSpecialistsStore: RespondedSpecialistsStore

    @Autowired
    private lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var log: FileLog

    override fun assignSpecialist(sessionId: String, userId: Long, damageClaimId: Long): Single<ClientAssignSpecialistService.Get.Result> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(ClientAssignSpecialistService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(ClientAssignSpecialistService.Get.Result.BadAccountType())
        }

        val damageClaimFickle = damageClaimStore.findOne(damageClaimId)
        if (!damageClaimFickle.isPresent()) {
            log.d("DamageClaim with id $damageClaimId does not exist")
            return Single.just(ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotExist())
        }

        val damageClaim = damageClaimFickle.get()
        if (userId != damageClaim.userId) {
            log.d("DamageClaim with id ${damageClaim.id} does not belong to user with ud $userId")
            return Single.just(ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotBelongToUser())
        }

        if (!respondedSpecialistsStore.deleteAllForDamageClaim(damageClaimId)) {
            log.d("Something went wrong while trying to remove specialists responded to damageClaim with id $damageClaimId")
            return Single.just(ClientAssignSpecialistService.Get.Result.CouldNotRemoveRespondedSpecialists())
        }

        if (!assignedSpecialistsStore.saveOne(AssignedSpecialist(damageClaimId, userId, false))) {
            log.d("Something went wrong while trying to remove specialists responded to damageClaim with id $damageClaimId")
            return Single.just(ClientAssignSpecialistService.Get.Result.CouldNotRemoveRespondedSpecialists())
        }

        return Single.just(ClientAssignSpecialistService.Get.Result.Ok())
    }
}



































