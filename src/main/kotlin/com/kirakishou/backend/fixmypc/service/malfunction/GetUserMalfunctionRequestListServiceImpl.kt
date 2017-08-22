package com.kirakishou.backend.fixmypc.service.malfunction

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.UserCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.MalfunctionRepository
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GetUserMalfunctionRequestListServiceImpl : GetUserMalfunctionRequestListService {

    @Autowired
    private lateinit var userCache: UserCache

    @Autowired
    private lateinit var malfunctionRepository: MalfunctionRepository

    @Autowired
    private lateinit var log: FileLog

    override fun getUserMalfunctionRequestList(sessionId: String): Single<GetUserMalfunctionRequestListService.Get.Result> {

        //user must re login if sessionId was removed from the cache
        val userFickle = userCache.get(sessionId)
        if (!userFickle.isPresent()) {
            log.d("sessionId $sessionId was not found in the cache")
            return Single.just(GetUserMalfunctionRequestListService.Get.Result.SessionIdExpired())
        }



        return Single.just(GetUserMalfunctionRequestListService.Get.Result.Ok())
    }
}