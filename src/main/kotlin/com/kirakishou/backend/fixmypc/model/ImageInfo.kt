package com.kirakishou.backend.fixmypc.model

import org.springframework.web.multipart.MultipartFile

data class ImageInfo(val serverId: Int,
                     val filePart: MultipartFile)