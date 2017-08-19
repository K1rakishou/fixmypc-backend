package com.kirakishou.backend.fixmypc.model

import com.fasterxml.jackson.annotation.JsonProperty

data class FileServerAnswer(@JsonProperty(Constant.SerializedNames.ERROR_CODE) val errorCode: Int,
                            @JsonProperty(Constant.SerializedNames.BAD_PHOTO_NAMES) val badPhotoNames: List<String>)