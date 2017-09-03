package com.kirakishou.backend.fixmypc.model.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.core.Constant

data class DistributedImage(@JsonProperty(Constant.SerializedNames.IMAGE_ORIGINAL_NAME) val imageOrigName: String,
                            @JsonProperty(Constant.SerializedNames.IMAGE_TYPE) val imageType: Int,
                            @JsonProperty(Constant.SerializedNames.IMAGE_NAME) val imageNewName: String,
                            @JsonProperty(Constant.SerializedNames.OWNER_ID) val ownerId: Long,
                            @JsonProperty(Constant.SerializedNames.MALFUNCTION_REQUEST_ID) val malfunctionRequestId: String)