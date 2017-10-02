package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import com.kirakishou.backend.fixmypc.model.repository.AssignedSpecialistRepository
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.RespondedSpecialistsRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientAssignSpecialistServiceImpl : ClientAssignSpecialistService {

    @Autowired
    private lateinit var assignedSpecialistsRepo: AssignedSpecialistRepository

    @Autowired
    private lateinit var respondedSpecialistsRepo: RespondedSpecialistsRepository

    @Autowired
    private lateinit var damageClaimRepo: DamageClaimRepository

    @Autowired
    private lateinit var userCache: UserCache

    @Autowired
    private lateinit var log: FileLog

    override fun assignSpecialist(sessionId: String, userId: Long, damageClaimId: Long): Single<ClientAssignSpecialistService.Get.Result> {
        val userFickle = userCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the cache")
            return Single.just(ClientAssignSpecialistService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(ClientAssignSpecialistService.Get.Result.BadAccountType())
        }

        val damageClaimFickle = damageClaimRepo.findOne(damageClaimId)
        if (!damageClaimFickle.isPresent()) {
            log.d("DamageClaim with id $damageClaimId does not exist")
            return Single.just(ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotExist())
        }

        val damageClaim = damageClaimFickle.get()
        if (userId != damageClaim.ownerId) {
            log.d("DamageClaim with id ${damageClaim.id} does not belong to user with ud $userId")
            return Single.just(ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotBelongToUser())
        }

        if (!respondedSpecialistsRepo.deleteAllForDamageClaim(damageClaimId)) {
            log.d("Something went wrong while trying to remove specialists responded to damageClaim with id $damageClaimId")
            return Single.just(ClientAssignSpecialistService.Get.Result.CouldNotRemoveRespondedSpecialists())
        }

        if (!assignedSpecialistsRepo.saveOne(AssignedSpecialist(damageClaimId, userId, false))) {
            log.d("Something went wrong while trying to remove specialists responded to damageClaim with id $damageClaimId")
            return Single.just(ClientAssignSpecialistService.Get.Result.CouldNotRemoveRespondedSpecialists())
        }

        return Single.just(ClientAssignSpecialistService.Get.Result.Ok())
    }
}



































