package com.kirakishou.backend.fixmypc.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.FileOutputStream

@Component
class TempFilesServiceImpl : TempFilesService {

    @Value("\${fixmypc.backend.images.temp-path}")
    private lateinit var tempImgsPath: String

    private val tempFiles = arrayListOf<String>()

    override fun fromMultipartFile(file: MultipartFile): String {
        val tempFilePath = "${tempImgsPath}${file.originalFilename}"
        tempFiles.add(tempFilePath)

        val fo = FileOutputStream(tempFilePath)

        fo.use {
            it.write(file.bytes)
        }

        return tempFilePath
    }
}