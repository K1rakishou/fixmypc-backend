package com.kirakishou.backend.fixmypc.model.net.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.core.Constant

data class CreateDamageClaimRequest(@JsonProperty(Constant.SerializedNames.DAMAGE_CATEGORY) val category: Int,
                                    @JsonProperty(Constant.SerializedNames.DAMAGE_DESCRIPTION) val description: String,
                                    @JsonProperty(Constant.SerializedNames.LOCATION_LAT) val lat: Double,
                                    @JsonProperty(Constant.SerializedNames.LOCATION_LON) val lon: Double)