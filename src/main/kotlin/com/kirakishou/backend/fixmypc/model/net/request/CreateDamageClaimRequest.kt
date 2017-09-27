package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

data class CreateDamageClaimRequest(@SerializedName(Constant.SerializedNames.DAMAGE_CATEGORY)
                                    val category: Int,

                                    @SerializedName(Constant.SerializedNames.DAMAGE_DESCRIPTION)
                                    val description: String,

                                    @SerializedName(Constant.SerializedNames.LOCATION_LAT)
                                    val lat: Double,

                                    @SerializedName(Constant.SerializedNames.LOCATION_LON)
                                    val lon: Double)