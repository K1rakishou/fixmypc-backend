package com.kirakishou.backend.fixmypc.service

import org.springframework.web.multipart.MultipartFile

interface TempFilesService {
    fun fromMultipartFile(file: MultipartFile): String
}