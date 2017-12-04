package com.kirakishou.backend.fixmypc.service.client

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.net.request.ClientProfileRequest
import com.kirakishou.backend.fixmypc.model.store.ClientProfileStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientProfileServiceImpl : ClientProfileService {

    @Autowired
    private lateinit var clientProfileStore: ClientProfileStore

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var log: FileLog

    override fun getClientProfile(sessionId: String): Single<ClientProfileService.Get.ResultProfile> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionRepository")
            return Single.just(ClientProfileService.Get.ResultProfile.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(ClientProfileService.Get.ResultProfile.BadAccountType())
        }

        val clientProfileFickle = clientProfileStore.findOne(user.id)
        if (!clientProfileFickle.isPresent()) {
            return Single.just(ClientProfileService.Get.ResultProfile.CouldNotFindProfile())
        }

        val clientProfile = clientProfileFickle.get()
        return Single.just(ClientProfileService.Get.ResultProfile.Ok(clientProfile))
    }

    override fun isClientProfileFilledIn(sessionId: String): Single<ClientProfileService.Get.ResultFilledIn> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionRepository")
            return Single.just(ClientProfileService.Get.ResultFilledIn.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(ClientProfileService.Get.ResultFilledIn.BadAccountType())
        }

        val clientProfileFickle = clientProfileStore.findOne(user.id)
        if (!clientProfileFickle.isPresent()) {
            return Single.just(ClientProfileService.Get.ResultFilledIn.CouldNotFindProfile())
        }

        val clientProfile = clientProfileFickle.get()
        return Single.just(ClientProfileService.Get.ResultFilledIn.Ok(clientProfile.isProfileInfoFilledIn()))
    }

    override fun updateClientProfile(sessionId: String, profile: ClientProfileRequest): Single<ClientProfileService.Post.Result> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionRepository")
            return Single.just(ClientProfileService.Post.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(ClientProfileService.Post.Result.BadAccountType())
        }

        if (!clientProfileStore.update(user.id, profile.profileName, profile.profilePhone)) {
            Single.just(ClientProfileService.Post.Result.StoreError())
        }

        return Single.just(ClientProfileService.Post.Result.Ok())
    }
}











































