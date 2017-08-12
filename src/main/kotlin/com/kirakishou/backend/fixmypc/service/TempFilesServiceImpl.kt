package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.util.ServerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream

@Component
class TempFilesServiceImpl : TempFilesService {

    @Value("\${fixmypc.backend.images.temp-dir}")
    private lateinit var tempImagesDir: String

    @Autowired
    lateinit var log: FileLog

    private val tempFiles = arrayListOf<String>()

    override fun fromMultipartFile(file: MultipartFile): String {
        val tempFilePath = "${tempImagesDir}${file.originalFilename}"

        val tempFile = File(tempFilePath)
        if (tempFile.exists()) {
            log.d("No need to create a new temp file it's already exists on the disk")
            return tempFilePath
        }

        log.d("Creating a new tempFile")

        tempFiles.add(tempFilePath)
        val fo = FileOutputStream(tempFilePath)

        fo.use {
            it.write(file.bytes)
        }

        return tempFilePath
    }

    override fun deleteAllTempFiles() {
        ServerUtils.purgeDirectory(File(tempImagesDir))
    }
}