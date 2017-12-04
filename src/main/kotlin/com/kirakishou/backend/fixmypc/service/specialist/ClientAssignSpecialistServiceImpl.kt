package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import com.kirakishou.backend.fixmypc.model.store.AssignedSpecialistStore
import com.kirakishou.backend.fixmypc.model.store.ClientProfileStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/*
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
    private lateinit var clientProfileStore: ClientProfileStore

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    private lateinit var log: FileLog

    override fun assignSpecialist(sessionId: String, specialistUserId: Long, damageClaimId: Long): Single<ClientAssignSpecialistService.Get.Result> {
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

        val clientProfileFickle = clientProfileStore.findOne(user.id)
        if (!clientProfileFickle.isPresent()) {
            log.d("Could not find client profile with id ${user.id}")
            return Single.just(ClientAssignSpecialistService.Get.Result.CouldNotFindClientProfile())
        }

        val clientProfile = clientProfileFickle.get()
        if (!clientProfile.isProfileInfoFilledIn()) {
            log.d("User with id ${user.id} tried to respond to damage claim with not filled in profile")
            return Single.just(ClientAssignSpecialistService.Get.Result.ProfileIsNotFilledIn())
        }

        val assignedSpecialistFickle = assignedSpecialistsStore.findOne(damageClaimId)
        if (assignedSpecialistFickle.isPresent()) {
            log.d("Specialist already assigned")
            return Single.just(ClientAssignSpecialistService.Get.Result.SpecialistAlreadyAssigned())
        }

        val damageClaimFickle = damageClaimStore.findOne(damageClaimId)
        if (!damageClaimFickle.isPresent()) {
            log.d("DamageClaim with id $damageClaimId does not exist")
            return Single.just(ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotExist())
        }

        val damageClaim = damageClaimFickle.get()
        if (user.id != damageClaim.userId) {
            log.d("DamageClaim with id ${damageClaim.id} does not belong to user with ud ${user.id}")
            return Single.just(ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotBelongToUser())
        }

        ignite.transactions().txStart().use { transaction ->
            try {
                if (!respondedSpecialistsStore.deleteAllForDamageClaim(damageClaimId)) {
                    log.d("Something went wrong while trying to remove specialists responded to damageClaim with id $damageClaimId")
                    transaction.rollback()

                    return Single.just(ClientAssignSpecialistService.Get.Result.CouldNotRemoveRespondedSpecialists())
                }

                if (!assignedSpecialistsStore.saveOne(AssignedSpecialist(damageClaimId, specialistUserId, user.id, false))) {
                    log.d("Something went wrong while trying to save assigned specialist to store")
                    transaction.rollback()

                    return Single.just(ClientAssignSpecialistService.Get.Result.CouldNotSaveAssignedSpecialist())
                }

                transaction.commit()
            } catch (e: Throwable) {
                transaction.rollback()
            }
        }

        return Single.just(ClientAssignSpecialistService.Get.Result.Ok())
    }
}

*/

































