package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.service.ImageService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
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
    lateinit var imageService: ImageService

    @Autowired
    lateinit var log: FileLog

    @RequestMapping(path = arrayOf("${Constant.Paths.IMAGE_CONTROLLER_PATH}/{user_id}/{image_type}/{size}/{image_name:.+}"),
            method = arrayOf(RequestMethod.GET))
    fun serveImage(@PathVariable("image_name") imageName: String,
                   @PathVariable("size") size: String,
                   @PathVariable("user_id") userId: Long,
                   @PathVariable("image_type") imageType: Int): Single<ResponseEntity<Resource>> {

        return imageService.serveImage(userId, imageType, imageName, size)
                .map { result ->
                    when (result) {
                        is ImageService.Get.Result.Ok -> {
                            ResponseEntity
                                    .status(HttpStatus.OK)
                                    .contentType(MediaType.IMAGE_PNG)
                                    .contentLength(result.inputStream.available().toLong())
                                    .body<Resource>(InputStreamResource(result.inputStream))
                        }

                        is ImageService.Get.Result.BadImageType -> {
                            return@map ResponseEntity<Resource>(null, HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is ImageService.Get.Result.BadFileName -> {
                            return@map ResponseEntity<Resource>(null, HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is ImageService.Get.Result.NotFound -> {
                            return@map ResponseEntity<Resource>(null, HttpStatus.NOT_FOUND)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}