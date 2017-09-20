package com.kirakishou.backend.fixmypc.service.profile

import com.kirakishou.backend.fixmypc.model.repository.ClientProfileRepository
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClientProfileServiceImpl : ClientProfileService {

    @Autowired
    private lateinit var clientProfileRepository: ClientProfileRepository

    override fun getClientProfile(userId: Long): Single<ClientProfileService.Get.Result> {
        val repoResult = clientProfileRepository.findOne(userId)
        if (!repoResult.isPresent()) {
            return Single.just(ClientProfileService.Get.Result.CouldNotFindProfile())
        }

        return Single.just(ClientProfileService.Get.Result.Ok(repoResult.get()))
    }
}