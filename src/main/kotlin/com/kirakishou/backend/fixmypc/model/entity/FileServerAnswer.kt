package com.kirakishou.backend.fixmypc.model.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.core.Constant

data class FileServerAnswer(@JsonProperty(Constant.SerializedNames.ERROR_CODE) val errorCode: Int,
                            @JsonProperty(Constant.SerializedNames.BAD_PHOTO_NAMES) val badPhotoNames: List<String>)