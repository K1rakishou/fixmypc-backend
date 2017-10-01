package com.kirakishou.backend.fixmypc.service.specialist

import io.reactivex.Single

interface ClientChooseSpecialistService {

    interface Get {
        interface Result {
            class Ok : Result
        }
    }

    fun chooseSpecialist(userId: Long): Single<Get.Result>
}