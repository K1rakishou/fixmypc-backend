package com.kirakishou.backend.fixmypc.service.malfunction

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.repository.MalfunctionRepository
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.MalfunctionStore
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserMalfunctionsStore
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.UserStore
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GetUserMalfunctionRequestListServiceImpl : GetUserMalfunctionRequestListService {

    @Autowired
    private lateinit var userStore: UserStore

    @Autowired
    private lateinit var malfunctionRepository: MalfunctionRepository

    @Autowired
    lateinit var malfunctionStore: MalfunctionStore

    @Autowired
    lateinit var userMalfunctionsStore: UserMalfunctionsStore

    @Autowired
    private lateinit var log: FileLog

    override fun getUserMalfunctionRequestList(sessionId: String, offset: Long): Single<GetUserMalfunctionRequestListService.Get.Result> {

        //user must re login if sessionId was removed from the cache
        val userFickle = userStore.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("sessionId $sessionId was not found in the cache")
            return Single.just(GetUserMalfunctionRequestListService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        val malfunctionList = malfunctionRepository.findMany(user.id, offset, Constant.MAX_MALFUNCTIONS_PER_PAGE)

        return Single.just(GetUserMalfunctionRequestListService.Get.Result.Ok(malfunctionList))
    }
}