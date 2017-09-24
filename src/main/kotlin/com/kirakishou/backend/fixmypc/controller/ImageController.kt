package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.service.ServerImageService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod


@Controller
@RequestMapping
class ImageController {

    @Autowired
    lateinit var serverImageService: ServerImageService

    @Autowired
    lateinit var log: FileLog

    @RequestMapping(path = arrayOf("${Constant.Paths.IMAGE_CONTROLLER_PATH}/{image_name:.+}/{size}"),
            method = arrayOf(RequestMethod.GET),
            produces = arrayOf(MediaType.IMAGE_PNG_VALUE))
    fun serveImage(@PathVariable("image_name") imageName: String,
                   @PathVariable("size") size: String): Single<ResponseEntity<Resource>> {

        return serverImageService.serveImage(imageName, size)
                .map { result ->
                    when (result) {
                        is ServerImageService.Get.Result.Ok -> {
                            return@map ResponseEntity.ok(result.image.body)
                        }

                        is ServerImageService.Get.Result.ServerIsDead -> {
                            log.e("File servers are dead")
                            return@map ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is ServerImageService.Get.Result.BadName -> {
                            return@map ResponseEntity<Resource>(HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is ServerImageService.Get.Result.NotFound -> {
                            return@map ResponseEntity<Resource>(HttpStatus.NOT_FOUND)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}