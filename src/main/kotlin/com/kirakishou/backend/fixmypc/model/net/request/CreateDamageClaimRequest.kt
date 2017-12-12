package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.DamageClaimCategory

data class CreateDamageClaimRequest(
        @SerializedName(Constant.SerializedNames.DAMAGE_CATEGORY)
        val category: Int,

        @SerializedName(Constant.SerializedNames.DAMAGE_DESCRIPTION)
        val description: String,

        @SerializedName(Constant.SerializedNames.LOCATION_LAT)
        var lat: Double,

        @SerializedName(Constant.SerializedNames.LOCATION_LON)
        var lon: Double
) {
    private fun checkLocationBounds(): Boolean {
        if (lat < -180.0) {
            return false
        }

        if (lat > 180.0) {
            return false
        }

        if (lon < -90.0) {
            return false
        }

        if (lon > 90.0) {
            return false
        }

        return true
    }

    fun isOk(): Boolean {
        if (!checkLocationBounds()) {
            return false
        }

        if (!DamageClaimCategory.contains(category)) {
            return false
        }

        if (description.isBlank()) {
            return false
        }

        return true
    }
}