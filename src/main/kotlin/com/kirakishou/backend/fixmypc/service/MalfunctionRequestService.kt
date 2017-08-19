package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import io.reactivex.Single
import org.springframework.web.multipart.MultipartFile

interface MalfunctionRequestService {

    interface Result {
        class Ok: Result
        class NoFilesToUpload: Result
        class ImagesCountExceeded: Result
        class FileSizeExceeded: Result
        class RequestSizeExceeded: Result
        class AllFileServersAreNotWorking: Result
        class AllImagesUploaded(val names: List<String>): Result
        class DatabaseError: Result
        class UnknownError: Result
    }

    fun handleNewMalfunctionRequest(uploadingFiles: Array<MultipartFile>, imageType: Int, request: MalfunctionRequest): Single<Result>
}