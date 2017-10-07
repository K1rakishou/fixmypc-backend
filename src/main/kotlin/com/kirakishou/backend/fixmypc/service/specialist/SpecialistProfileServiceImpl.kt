package com.kirakishou.backend.fixmypc.service.specialist

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.SpecialistProfileRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired

class SpecialistProfileServiceImpl : SpecialistProfileService {

    @Autowired
    private lateinit var mUserCache: UserCache

    @Autowired
    private lateinit var mSpecialistProfileRepository: SpecialistProfileRepository

    @Autowired
    private lateinit var log: FileLog

    override fun getProfile(sessionId: String): Single<SpecialistProfileService.Get.Result> {
        val userFickle = mUserCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the cache")
            return Single.just(SpecialistProfileService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(SpecialistProfileService.Get.Result.BadAccountType())
        }

        val profileFickle = mSpecialistProfileRepository.findOne(user.id)
        if (!profileFickle.isPresent()) {
            return Single.just(SpecialistProfileService.Get.Result.NotFound())
        }

        return Single.just(SpecialistProfileService.Get.Result.Ok(profileFickle.get()))
    }
}