package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.Constant

data class MalfunctionRequest(@JsonProperty(Constant.SerializedNames.MALFUNCTION_CATEGORY) val category: Int,
                              @JsonProperty(Constant.SerializedNames.MALFUNCTION_DESCRIPTION) val description: String)