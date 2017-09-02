package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.Constant

data class MalfunctionCreateRequest(@JsonProperty(Constant.SerializedNames.MALFUNCTION_CATEGORY) val category: Int,
                                    @JsonProperty(Constant.SerializedNames.MALFUNCTION_DESCRIPTION) val description: String,
                                    @JsonProperty(Constant.SerializedNames.LOCATION_LAT) val lat: Double,
                                    @JsonProperty(Constant.SerializedNames.LOCATION_LON) val lon: Double)