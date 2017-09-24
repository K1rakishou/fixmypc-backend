package com.kirakishou.backend.fixmypc.service.damageclaim

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import com.kirakishou.backend.fixmypc.model.repository.DamageClaimRepository
import com.kirakishou.backend.fixmypc.model.repository.RespondedSpecialistsRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RespondToDamageClaimServiceImpl : RespondToDamageClaimService {

    @Autowired
    private lateinit var repository: RespondedSpecialistsRepository

    @Autowired
    private lateinit var damageClaimRepository: DamageClaimRepository

    @Autowired
    private lateinit var userCache: UserCache

    @Autowired
    private lateinit var log: FileLog

    override fun respondToDamageClaim(sessionId: String, damageClaimId: Long): Single<RespondToDamageClaimService.Post.Result> {
        val userFickle = userCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("sessionId $sessionId was not found in the cache")
            return Single.just(RespondToDamageClaimService.Post.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        val damageClaim = damageClaimRepository.findOne(damageClaimId)
        if (!damageClaim.isPresent()) {
            log.d("DamageClaim with id $damageClaimId does not exist")
            return Single.just(RespondToDamageClaimService.Post.Result.DamageClaimDoesNotExists())
        }

        val repoResult = repository.saveOne(RespondedSpecialist(damageClaimId = damageClaimId, userId = user.id))

        if (!repoResult) {
            log.d("Couldn't respond to damage claim")
            return Single.just(RespondToDamageClaimService.Post.Result.CouldNotRespondToDamageClaim())
        }

        return Single.just(RespondToDamageClaimService.Post.Result.Ok())
    }
}