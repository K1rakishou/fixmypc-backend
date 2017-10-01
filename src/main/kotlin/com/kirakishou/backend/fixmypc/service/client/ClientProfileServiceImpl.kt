package com.kirakishou.backend.fixmypc.service.client

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.ClientProfileRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientProfileServiceImpl : ClientProfileService {

    @Autowired
    private lateinit var clientProfileRepository: ClientProfileRepository

    @Autowired
    private lateinit var userCache: UserCache

    @Autowired
    private lateinit var log: FileLog

    override fun getClientProfile(sessionId: String, userId: Long): Single<ClientProfileService.Get.Result> {
        val userFickle = userCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the cache")
            return Single.just(ClientProfileService.Get.Result.SessionIdExpired())
        }

        val repoResult = clientProfileRepository.findOne(userId)
        if (!repoResult.isPresent()) {
            return Single.just(ClientProfileService.Get.Result.CouldNotFindProfile())
        }

        return Single.just(ClientProfileService.Get.Result.Ok(repoResult.get()))
    }
}