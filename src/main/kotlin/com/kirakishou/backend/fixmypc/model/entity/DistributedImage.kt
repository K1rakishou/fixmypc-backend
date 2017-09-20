package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

data class DistributedImage(@SerializedName(Constant.SerializedNames.IMAGE_ORIGINAL_NAME) val imageOrigName: String,
                            @SerializedName(Constant.SerializedNames.IMAGE_TYPE) val imageType: Int,
                            @SerializedName(Constant.SerializedNames.IMAGE_NAME) val imageNewName: String,
                            @SerializedName(Constant.SerializedNames.OWNER_ID) val ownerId: Long,
                            @SerializedName(Constant.SerializedNames.MALFUNCTION_REQUEST_ID) val malfunctionRequestId: String)