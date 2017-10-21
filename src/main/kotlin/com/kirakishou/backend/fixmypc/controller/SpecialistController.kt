package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.PickSpecialistRequest
import com.kirakishou.backend.fixmypc.model.net.request.SpecialistProfileRequest
import com.kirakishou.backend.fixmypc.model.net.response.*
import com.kirakishou.backend.fixmypc.service.specialist.ClientAssignSpecialistService
import com.kirakishou.backend.fixmypc.service.specialist.GetRespondedSpecialistsService
import com.kirakishou.backend.fixmypc.service.specialist.SpecialistProfileService
import io.reactivex.Single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping
class SpecialistController {

    @Autowired
    lateinit var mGetRespondedSpecialistsService: GetRespondedSpecialistsService

    @Autowired
    lateinit var mClientAssignSpecialistService: ClientAssignSpecialistService

    @Autowired
    lateinit var mSpecialistProfileService: SpecialistProfileService

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/profile/{damage_claim_id}/{skip}/{count}"),
            method = arrayOf(RequestMethod.GET))
    fun getAllRespondedSpecialistsPaged(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                                        @PathVariable("damage_claim_id") damageClaimId: Long,
                                        @PathVariable("skip") skip: Long,
                                        @PathVariable("count") count: Long): Single<ResponseEntity<SpecialistsListResponse>> {

        return mGetRespondedSpecialistsService.getRespondedSpecialistsPaged(sessionId, damageClaimId, skip, count)
                .map { result ->
                    when (result) {
                        is GetRespondedSpecialistsService.Get.Result.Ok -> {
                            return@map ResponseEntity(SpecialistsListResponse(result.responded,
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is GetRespondedSpecialistsService.Get.Result.BadAccountType -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.FORBIDDEN)
                        }

                        is GetRespondedSpecialistsService.Get.Result.DamageClaimDoesNotExist -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_DAMAGE_CLAIM_DOES_NOT_EXIST.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is GetRespondedSpecialistsService.Get.Result.DamageClaimIsNotActive -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_DAMAGE_CLAIM_IS_NOT_ACTIVE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is GetRespondedSpecialistsService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(SpecialistsListResponse(emptyList(),
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(SpecialistsListResponse(emptyList(),
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/assign"),
            method = arrayOf(RequestMethod.POST))
    fun clientAssignSpecialist(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                               @RequestBody request: PickSpecialistRequest): Single<ResponseEntity<StatusResponse>> {

        return mClientAssignSpecialistService.assignSpecialist(sessionId, request.userId, request.damageClaimId)
                .map { result ->
                    when (result) {
                        is ClientAssignSpecialistService.Get.Result.Ok -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is ClientAssignSpecialistService.Get.Result.CouldNotFindClientProfile -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is ClientAssignSpecialistService.Get.Result.ProfileIsNotFilledIn -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_PROFILE_IS_NOT_FILLED_IN.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is ClientAssignSpecialistService.Get.Result.CouldNotRemoveRespondedSpecialists -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_COULD_NOT_REMOVE_RESPONDED_SPECIALISTS.value),
                                    HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is ClientAssignSpecialistService.Get.Result.BadAccountType -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.FORBIDDEN)
                        }

                        is ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotBelongToUser -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_DAMAGE_CLAIM_DOES_NOT_BELONG_TO_USER.value),
                                    HttpStatus.FORBIDDEN)
                        }

                        is ClientAssignSpecialistService.Get.Result.DamageClaimDoesNotExist -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_DAMAGE_CLAIM_DOES_NOT_EXIST.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        is ClientAssignSpecialistService.Get.Result.SessionIdExpired -> {
                            return@map ResponseEntity(StatusResponse(ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(StatusResponse(
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/profile"),
            method = arrayOf(RequestMethod.GET))
    fun getSpecialistProfile(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String): Single<ResponseEntity<SpecialistProfileResponse>> {

        return mSpecialistProfileService.getSpecialistProfile(sessionId)
                .map { result ->
                    when (result) {
                        is SpecialistProfileService.Get.ResultProfile.Ok -> {
                            return@map ResponseEntity(SpecialistProfileResponse(result.profile, result.profile.isProfileInfoFilledIn(),
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is SpecialistProfileService.Get.ResultProfile.SessionIdExpired -> {
                            return@map ResponseEntity(SpecialistProfileResponse(null, false,
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        is SpecialistProfileService.Get.ResultProfile.BadAccountType -> {
                            return@map ResponseEntity(SpecialistProfileResponse(null, false,
                                    ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value), HttpStatus.FORBIDDEN)
                        }

                        is SpecialistProfileService.Get.ResultProfile.NotFound -> {
                            return@map ResponseEntity(SpecialistProfileResponse(null, false,
                                    ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE.value), HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(SpecialistProfileResponse(null, false,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/profile"),
            method = arrayOf(RequestMethod.POST))
    fun updateSpecialistProfile(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                                @RequestBody request: SpecialistProfileRequest): Single<ResponseEntity<StatusResponse>> {

        return mSpecialistProfileService.updateSpecialistProfile(sessionId, request)
                .map { result ->
                    when (result) {
                        is SpecialistProfileService.Post.ResultInfo.Ok -> {
                            return@map ResponseEntity(StatusResponse(
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is SpecialistProfileService.Post.ResultInfo.SessionIdExpired -> {
                            return@map ResponseEntity(StatusResponse(
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        is SpecialistProfileService.Post.ResultInfo.BadAccountType -> {
                            return@map ResponseEntity(StatusResponse(
                                    ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value), HttpStatus.FORBIDDEN)
                        }

                        is SpecialistProfileService.Post.ResultInfo.NotFound -> {
                            return@map ResponseEntity(StatusResponse(
                                    ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE.value), HttpStatus.NOT_FOUND)
                        }

                        is SpecialistProfileService.Post.ResultInfo.RepositoryError -> {
                            return@map ResponseEntity(StatusResponse(
                                    ServerErrorCode.SEC_STORE_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is SpecialistProfileService.Post.ResultInfo.UnknownError -> {
                            return@map ResponseEntity(StatusResponse(
                                    ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(StatusResponse(ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/profile/photo"),
            method = arrayOf(RequestMethod.POST))
    fun updateSpecialistProfilePhoto(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String,
                                     @RequestPart("photo") profilePhoto: MultipartFile): Single<ResponseEntity<UpdateSpecialistProfileResponse>> {

        return mSpecialistProfileService.updateProfilePhoto(sessionId, profilePhoto)
                .map { result ->
                    when (result) {
                        is SpecialistProfileService.Post.ResultPhoto.Ok -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse(result.newPhotoName,
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is SpecialistProfileService.Post.ResultPhoto.SessionIdExpired -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse("",
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value), HttpStatus.UNAUTHORIZED)
                        }

                        is SpecialistProfileService.Post.ResultPhoto.BadAccountType -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse("",
                                    ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value), HttpStatus.FORBIDDEN)
                        }

                        is SpecialistProfileService.Post.ResultPhoto.NotFound -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse("",
                                    ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE.value), HttpStatus.NOT_FOUND)
                        }

                        is SpecialistProfileService.Post.ResultPhoto.CouldNotUploadImage -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse("",
                                    ServerErrorCode.SEC_COULD_NOT_UPLOAD_IMAGE.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is SpecialistProfileService.Post.ResultPhoto.CouldNotDeleteOldImage -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse("",
                                    ServerErrorCode.SEC_COULD_NOT_DELETE_OLD_IMAGE.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is SpecialistProfileService.Post.ResultPhoto.StoreError -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse("",
                                    ServerErrorCode.SEC_STORE_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        is SpecialistProfileService.Post.ResultPhoto.UnknownError -> {
                            return@map ResponseEntity(UpdateSpecialistProfileResponse("",
                                    ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value), HttpStatus.INTERNAL_SERVER_ERROR)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(UpdateSpecialistProfileResponse("",
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @RequestMapping(path = arrayOf("${Constant.Paths.SPECIALIST_CONTROLLER_PATH}/profile/is_filled_in"),
            method = arrayOf(RequestMethod.GET))
    fun isProfileFilledIn(@RequestHeader(value = "session_id", defaultValue = "") sessionId: String): Single<ResponseEntity<IsProfileFilledInResponse>> {
        return mSpecialistProfileService.isSpecialistProfileFilledIn(sessionId)
                .map { result ->
                    when (result) {
                        is SpecialistProfileService.Get.ResultIsFilledIn.Ok -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(result.isFilledIn,
                                    ServerErrorCode.SEC_OK.value),
                                    HttpStatus.OK)
                        }

                        is SpecialistProfileService.Get.ResultIsFilledIn.SessionIdExpired -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(false,
                                    ServerErrorCode.SEC_SESSION_ID_EXPIRED.value),
                                    HttpStatus.UNAUTHORIZED)
                        }

                        is SpecialistProfileService.Get.ResultIsFilledIn.BadAccountType -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(false,
                                    ServerErrorCode.SEC_BAD_ACCOUNT_TYPE.value),
                                    HttpStatus.FORBIDDEN)
                        }

                        is SpecialistProfileService.Get.ResultIsFilledIn.CouldNotFindClientProfile -> {
                            return@map ResponseEntity(IsProfileFilledInResponse(false,
                                    ServerErrorCode.SEC_COULD_NOT_FIND_PROFILE.value),
                                    HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(IsProfileFilledInResponse(false,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }


}


































