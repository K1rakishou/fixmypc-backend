package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionCreateRequest
import com.kirakishou.backend.fixmypc.model.net.response.DamageClaimsResponse
import com.kirakishou.backend.fixmypc.model.net.response.MalfunctionCreateResponse
import com.kirakishou.backend.fixmypc.service.malfunction.CreateMalfunctionRequestService
import com.kirakishou.backend.fixmypc.service.malfunction.GetUserDamageClaimListService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping
class MalfunctionRequestController {

    @Autowired
    lateinit var mCreateMalfunctionRequestService: CreateMalfunctionRequestService

    @Autowired
    lateinit var mGetUserDamageClaimListService: GetUserDamageClaimListService

    @Autowired
    lateinit var log: FileLog

    @RequestMapping(path = arrayOf(Constant.Paths.MALFUNCTION_REQUEST_CONTROLLER_PATH),
            method = arrayOf(RequestMethod.POST))
    fun createMalfunctionRequest(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                                 @RequestPart("photos") uploadingFiles: Array<MultipartFile>,
                                 @RequestPart("request") request: MalfunctionCreateRequest,
                                 @RequestPart("images_type") imagesType: Int): Single<ResponseEntity<MalfunctionCreateResponse>> {

        return mCreateMalfunctionRequestService.createMalfunctionRequest(uploadingFiles, imagesType, request, sessionId)
                .map { result ->
                    when (result) {
                        is CreateMalfunctionRequestService.Post.Result.Ok -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is CreateMalfunctionRequestService.Post.Result.SessionIdExpired -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        is CreateMalfunctionRequestService.Post.Result.NoFilesToUpload -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_NO_FILES_WERE_SELECTED_TO_UPLOAD.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.ImagesCountExceeded -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_IMAGES_COUNT_EXCEEDED.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.FileSizeExceeded -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_FILE_SIZE_EXCEEDED.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.RequestSizeExceeded -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_REQUEST_SIZE_EXCEEDED.value),
                                    HttpStatus.BAD_REQUEST)
                        }

                        is CreateMalfunctionRequestService.Post.Result.AllFileServersAreNotWorking -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_ALL_FILE_SERVERS_ARE_NOT_WORKING.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is CreateMalfunctionRequestService.Post.Result.DatabaseError -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_DATABASE_ERROR.ordinal),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is CreateMalfunctionRequestService.Post.Result.UnknownError -> {
                            return@map ResponseEntity(MalfunctionCreateResponse(
                                    ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.MALFUNCTION_REQUEST_CONTROLLER_PATH}/{offset}"),
            method = arrayOf(RequestMethod.GET))
    fun getUserMalfunctionRequestList(@PathVariable("lat") lat: Double,
                                      @PathVariable("lon") lon: Double,
                                      @PathVariable("radius") radius: Double,
                                      @PathVariable("page") page: Long): Single<ResponseEntity<DamageClaimsResponse>> {

        return mGetUserDamageClaimListService.getDamageClaimsWithinRadiusPaged(lat, lon, radius, Math.abs(page))
                .map { result ->
                    when (result) {
                        is GetUserDamageClaimListService.Get.Result.Ok -> {
                            return@map ResponseEntity(DamageClaimsResponse(result.damageClaimList, ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is GetUserDamageClaimListService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(DamageClaimsResponse(emptyList(), ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
    }
}