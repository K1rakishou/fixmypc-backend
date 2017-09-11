package com.kirakishou.backend.fixmypc.model.dto

data class PhotoInfoDTO(var ownerId: Long = -1L,
                        var folderName: String = "",
                        var imageType: Int = -1)