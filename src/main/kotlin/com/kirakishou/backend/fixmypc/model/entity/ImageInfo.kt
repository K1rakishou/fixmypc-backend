package com.kirakishou.backend.fixmypc.model.entity

import org.springframework.web.multipart.MultipartFile

data class ImageInfo(val serverId: Int,
                     val filePart: MultipartFile)