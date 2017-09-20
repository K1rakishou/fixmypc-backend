package com.kirakishou.backend.fixmypc.service.profile

import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import io.reactivex.Single

interface ClientProfileService {

    interface Get {
        interface Result {
            class Ok(val clientProfile: ClientProfile) : Result
            class CouldNotFindProfile : Result
        }
    }

    fun getClientProfile(userId: Long): Single<Get.Result>
}