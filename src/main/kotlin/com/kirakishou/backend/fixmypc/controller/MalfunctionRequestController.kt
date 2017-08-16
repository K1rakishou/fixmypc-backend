package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.model.net.response.MalfunctionResponse
import com.kirakishou.backend.fixmypc.service.MalfunctionRequestService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
                            return@map ResponseEntity(MalfunctionResponse(ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is MalfunctionRequestService.Result.NoFilesToUpload -> {
                            return@map ResponseEntity(MalfunctionResponse(ServerErrorCode.SEC_NO_FILES_WERE_SELECTED_TO_UPLOAD.value), HttpStatus.BAD_REQUEST)
                        }

                        is MalfunctionRequestService.Result.ImagesCountExceeded -> {
                            return@map ResponseEntity(MalfunctionResponse(ServerErrorCode.SEC_IMAGES_COUNT_EXCEEDED.value), HttpStatus.BAD_REQUEST)
                        }

                        is MalfunctionRequestService.Result.FileSizeExceeded -> {
                            return@map ResponseEntity(MalfunctionResponse(ServerErrorCode.SEC_FILE_SIZE_EXCEEDED.value), HttpStatus.BAD_REQUEST)
                        }

                        is MalfunctionRequestService.Result.RequestSizeExceeded -> {
                            return@map ResponseEntity(MalfunctionResponse(ServerErrorCode.SEC_REQUEST_SIZE_EXCEEDED.value), HttpStatus.BAD_REQUEST)
                        }

                        is MalfunctionRequestService.Result.AllFileServersAreNotWorking -> {
                            return@map ResponseEntity(MalfunctionResponse(ServerErrorCode.SEC_ALL_FILE_SERVERS_ARE_NOT_WORKING.value), HttpStatus.SERVICE_UNAVAILABLE)
                        }

                        is MalfunctionRequestService.Result.UnknownError -> {
                            return@map ResponseEntity(MalfunctionResponse(ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
    }
}