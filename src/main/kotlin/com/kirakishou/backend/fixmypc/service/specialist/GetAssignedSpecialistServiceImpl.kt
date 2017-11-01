package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.store.AssignedSpecialistStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired

class GetAssignedSpecialistServiceImpl : GetAssignedSpecialistService {

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var assignedSpecialistsStore: AssignedSpecialistStore

    override fun getAssignedSpecialist(sessionId: String, damageClaimId: Long): Single<GetAssignedSpecialistService.Get.Result> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(GetAssignedSpecialistService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(GetAssignedSpecialistService.Get.Result.BadAccountType())
        }

        val assignedSpecialistFickle = assignedSpecialistsStore.findOne(damageClaimId)
        if (!assignedSpecialistFickle.isPresent()) {
            log.d("Could not file assigned specialist with damageClaimId: $damageClaimId")
            return Single.just(GetAssignedSpecialistService.Get.Result.CouldNotFindAssignedSpecialist())
        }

        val assignedSpecialist = assignedSpecialistFickle.get()
        if (assignedSpecialist.whoAssignedUserId != user.id) {
            log.d("whoAssignedUserId (${assignedSpecialist.whoAssignedUserId}) != current userId (${user.id})")
            return Single.just(GetAssignedSpecialistService.Get.Result.SpecialistWasNotAssignedByCurrentUser())
        }

        return Single.just(GetAssignedSpecialistService.Get.Result.Ok(assignedSpecialist.specialistUserId))
    }
}

































