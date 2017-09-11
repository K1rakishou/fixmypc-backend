package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.model.repository.PhotoToUserAffinityRepository
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.ConnectException
import java.util.concurrent.TimeoutException

@Component
class ServerImageServiceImpl : ServerImageService {

    @Autowired
    lateinit var photoInfoRepository: PhotoToUserAffinityRepository

    @Autowired
    lateinit var fileServerService: FileServerService

    @Autowired
    lateinit var fileServerManager: FileServersManager

    override fun serveImage(imageName: String, size: String): Single<ServerImageService.Get.Result> {
        val photoInfoFickle = photoInfoRepository.getOne(imageName)
        if (!photoInfoFickle.isPresent()) {
            return Single.just(ServerImageService.Get.Result.NotFound())
        }

        val photoInfo = photoInfoFickle.get()
        val imageNameInfo = TextUtils.parseImageName(imageName)
        val host = fileServerManager.getHostById(imageNameInfo.serverId)

        return fileServerService.serveDamageClaimImage(photoInfo.ownerId, host, photoInfo.folderName, imageName, photoInfo.imageType, size)
                .map { ServerImageService.Get.Result.Ok(it) as ServerImageService.Get.Result }
                .onErrorResumeNext { error ->
                    if (error is TimeoutException || error.cause is ConnectException) {
                        return@onErrorResumeNext Single.just(ServerImageService.Get.Result.ServerIsDead())
                    }

                    throw error
                }
    }
}