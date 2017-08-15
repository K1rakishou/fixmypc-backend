package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.model.net.response.MalfunctionResponse
import com.kirakishou.backend.fixmypc.service.MalfunctionRequestService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping
class MalfunctionRequestController {

    @Autowired
    lateinit var malfunctionRequestService: MalfunctionRequestService

    @Autowired
    lateinit var log: FileLog

    @RequestMapping(
            path = arrayOf(Constant.Paths.MALFUNCTION_REQUEST_CONTROLLER_PATH),
            method = arrayOf(RequestMethod.POST))
    fun malfunctionRequest(@RequestPart("photos") uploadingFiles: Array<MultipartFile>,
                           @RequestPart("request") request: MalfunctionRequest,
                           @RequestPart("images_type") imagesType: Int): Single<ResponseEntity<MalfunctionResponse>> {

        return malfunctionRequestService.handleNewMalfunctionRequest(uploadingFiles, imagesType, request)
                .map { result ->
                    when (result) {
                        is MalfunctionRequestService.Result.Ok -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        is MalfunctionRequestService.Result.NoFilesToUpload -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        is MalfunctionRequestService.Result.ImagesCountExceeded -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        is MalfunctionRequestService.Result.FileSizeExceeded -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        is MalfunctionRequestService.Result.RequestSizeExceeded -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        is MalfunctionRequestService.Result.CouldNotStoreOneOreMoreImages -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        is MalfunctionRequestService.Result.AllFileServersAreNotWorking -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        is MalfunctionRequestService.Result.UnknownError -> {
                            return@map ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK.value))
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
    }
}