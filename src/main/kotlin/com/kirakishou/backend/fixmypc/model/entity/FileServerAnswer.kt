package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

data class FileServerAnswer(@SerializedName(Constant.SerializedNames.ERROR_CODE) val errorCode: Int,
                            @SerializedName(Constant.SerializedNames.BAD_PHOTO_NAMES) val badPhotoNames: List<String>)