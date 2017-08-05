package com.kirakishou.backend.fixmypc.controller

import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.net.ServerErrorCode
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import com.kirakishou.backend.fixmypc.model.net.response.MalfunctionResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping
class MalfunctionRequestController {

    @Autowired
    lateinit var restTemplate: RestTemplate

    @RequestMapping(
            path = arrayOf(Constant.Paths.MALFUNCTION_REQUEST_CONTROLLER_PATH),
            method = arrayOf(RequestMethod.POST))
    fun malfunctionRequest(@RequestPart("photos") uploadingFiles: Array<MultipartFile>,
                           @RequestPart("request") request: MalfunctionRequest): ResponseEntity<MalfunctionResponse> {

        System.err.println("requestCategory: ${request.category}, requestDescription: ${request.description}")

        for (file in uploadingFiles) {
            System.err.println("fileName: ${file.originalFilename}")
        }

        return ResponseEntity.ok(MalfunctionResponse(ServerErrorCode.SEC_OK))
    }
}