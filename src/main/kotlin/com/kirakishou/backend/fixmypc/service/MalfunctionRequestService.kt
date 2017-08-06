package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import org.springframework.web.multipart.MultipartFile

interface MalfunctionRequestService {

    interface Result {
        class Ok: Result
        class FileSizeExceeded: Result
        class RequestSizeExceeded: Result
    }

    fun handleNewMalfunctionRequest(uploadingFiles: Array<MultipartFile>, request: MalfunctionRequest): Result
}