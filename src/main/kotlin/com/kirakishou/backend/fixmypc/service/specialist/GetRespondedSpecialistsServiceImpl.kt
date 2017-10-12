package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.RespondedSpecialistsRepository
import com.kirakishou.backend.fixmypc.model.repository.SpecialistProfileRepository
import com.kirakishou.backend.fixmypc.model.repository.store.UserStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class GetRespondedSpecialistsServiceImpl : GetRespondedSpecialistsService {

    @Autowired
    private lateinit var repository: RespondedSpecialistsRepository

    @Autowired
    private lateinit var damageClaimRepository: DamageClaimRepository

    @Autowired
    private lateinit var userStore: UserStore

    @Autowired
    private lateinit var specialistProfilesRepository: SpecialistProfileRepository

    @Autowired
    private lateinit var log: FileLog

    override fun getRespondedSpecialistsPaged(sessionId: String, damageClaimId: Long, skip: Long, count: Long):
            Single<GetRespondedSpecialistsService.Get.Result> {

        val userFickle = userStore.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the specialistProfileStore")
            return Single.just(GetRespondedSpecialistsService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(GetRespondedSpecialistsService.Get.Result.BadAccountType())
        }

        val damageClaimFickle = damageClaimRepository.findOne(damageClaimId)
        if (!damageClaimFickle.isPresent()) {
            log.d("DamageClaim with id $damageClaimId does not exist")
            return Single.just(GetRespondedSpecialistsService.Get.Result.DamageClaimDoesNotExist())
        }

        val damageClaim = damageClaimFickle.get()
        if (!damageClaim.isActive) {
            log.d("DamageClaim with id $damageClaimId is not active")
            return Single.just(GetRespondedSpecialistsService.Get.Result.DamageClaimIsNotActive())
        }

        val respondedSpecialistsList = repository.findAllForDamageClaimPaged(damageClaimId, skip, count)
        val specialistIdsList = respondedSpecialistsList.stream()
                .map { it.userId }
                .collect(Collectors.toList())

        val specialistProfilesList = specialistProfilesRepository.findMany(specialistIdsList)
        return Single.just(GetRespondedSpecialistsService.Get.Result.Ok(specialistProfilesList))
    }
}












































