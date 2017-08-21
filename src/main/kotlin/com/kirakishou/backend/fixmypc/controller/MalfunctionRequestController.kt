package com.kirakishou.backend.fixmypc.controller

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.model.net.response.MalfunctionResponse
import com.kirakishou.backend.fixmypc.service.malfunction.CreateMalfunctionRequestService
import com.kirakishou.backend.fixmypc.service.malfunction.GetUserMalfunctionRequestListService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping
class MalfunctionRequestController {

    @Autowired
    lateinit var mCreateMalfunctionRequestService: CreateMalfunctionRequestService

    @Autowired
    lateinit var mGetUserMalfunctionRequestListService: GetUserMalfunctionRequestListService

    @Autowired
    lateinit var log: FileLog

    @RequestMapping(path = arrayOf(Constant.Paths.MALFUNCTION_REQUEST_CONTROLLER_PATH),
            method = arrayOf(RequestMethod.POST))
    fun createMalfunctionRequest(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                           @RequestPart("photos") uploadingFiles: Array<MultipartFile>,
                           @RequestPart("request") request: MalfunctionRequest,
                           @RequestPart("images_type") imagesType: Int): Single<ResponseEntity<MalfunctionResponse>> {

        return mCreateMalfunctionRequestService.createMalfunctionRequest(uploadingFiles, imagesType, request, sessionId)
                .map { result ->
                    when (result) {
                        is CreateMalfunctionRequestService.Post.Result.Ok -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is CreateMalfunctionRequestService.Post.Result.SessionIdExpired -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        is CreateMalfunctionRequestService.Post.Result.NoFilesToUpload -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_NO_FILES_WERE_SELECTED_TO_UPLOAD.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.ImagesCountExceeded -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_IMAGES_COUNT_EXCEEDED.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.FileSizeExceeded -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_FILE_SIZE_EXCEEDED.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.RequestSizeExceeded -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_REQUEST_SIZE_EXCEEDED.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.AllFileServersAreNotWorking -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_ALL_FILE_SERVERS_ARE_NOT_WORKING.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is CreateMalfunctionRequestService.Post.Result.DatabaseError -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_DATABASE_ERROR.ordinal),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is CreateMalfunctionRequestService.Post.Result.UnknownError -> {
                            return@map ResponseEntity(MalfunctionResponse(
                                    ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
    }

    @RequestMapping(path = arrayOf(Constant.Paths.MALFUNCTION_REQUEST_CONTROLLER_PATH),
            method = arrayOf(RequestMethod.GET))
    fun getUserMalfunctionRequestList(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String):
            Single<ResponseEntity<UserMalfunctionRequestList>> {

        return mGetUserMalfunctionRequestListService.getUserMalfunctionRequestList(sessionId)
                .map { result ->
                    when (result) {
                        is GetUserMalfunctionRequestListService.Get.Result.Ok -> {
                            return@map ResponseEntity(UserMalfunctionRequestList(listOf("123")), HttpStatus.OK)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
    }

    data class UserMalfunctionRequestList(@JsonProperty("request_list") val requestList: List<String>)
}