package com.kirakishou.backend.fixmypc.model.dto

data class PhotoInfoDTO(var ownerId: Long = -1L,
                        var photoFolder: String = "",
                        var imageType: Int = -1)