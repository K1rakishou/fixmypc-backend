package com.kirakishou.backend.fixmypc.service.client

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.cache.SessionCache
import com.kirakishou.backend.fixmypc.model.store.ClientProfileStore
import com.kirakishou.backend.fixmypc.model.store.ProfilePhotoStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientProfileServiceImpl : ClientProfileService {

    @Autowired
    private lateinit var clientProfileStore: ClientProfileStore

    @Autowired
    private lateinit var profilePhotoStore: ProfilePhotoStore

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var log: FileLog

    override fun getClientProfile(sessionId: String, userId: Long): Single<ClientProfileService.Get.Result> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionRepository")
            return Single.just(ClientProfileService.Get.Result.SessionIdExpired())
        }

        val clientProfileFickle = clientProfileStore.findOne(userId)
        if (!clientProfileFickle.isPresent()) {
            return Single.just(ClientProfileService.Get.Result.CouldNotFindProfile())
        }

        val clientProfile = clientProfileFickle.get()

        val profilePhotoFickle = profilePhotoStore.findOne(userId)
        if (profilePhotoFickle.isPresent()) {
            val profilePhoto = profilePhotoFickle.get()

            clientProfile.photoFolder = profilePhoto.photoFolder
            clientProfile.photoName = profilePhoto.photoName
        }

        return Single.just(ClientProfileService.Get.Result.Ok(clientProfile))
    }
}