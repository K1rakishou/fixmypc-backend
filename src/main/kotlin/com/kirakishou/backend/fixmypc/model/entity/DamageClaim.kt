package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.extension.toList
import com.kirakishou.backend.fixmypc.util.ServerUtils
import java.sql.ResultSet


data class DamageClaim(@SerializedName("id")
                       var id: Long = -1L,

                       @SerializedName("owner_id")
                       var userId: Long = -1L,

                       @SerializedName("is_active")
                       var isActive: Boolean = false,

                       @SerializedName("category")
                       var category: Int = -1,

                       @SerializedName("description")
                       var description: String = "",

                       @SerializedName("lat")
                       var lat: Double = 0.0,

                       @SerializedName("lon")
                       var lon: Double = 0.0,

                       @SerializedName("created_on")
                       var createdOn: Long = -1L,

                       @SerializedName("photos")
                       var imageNamesList: List<String> = listOf()) {

    companion object {
        fun create(userId: Long, category: Int, description: String, lat: Double, lon: Double, fileNames: List<String>): DamageClaim {
            return DamageClaim(-1L, userId, true, category, description, lat, lon, ServerUtils.getTimeFast(), fileNames)
        }

        fun fromResultSet(rs: ResultSet): DamageClaim {
            val sqlArray = rs.getArray("photos_array")

            return DamageClaim(
                    rs.getLong("id"),
                    rs.getLong("owner_id"),
                    rs.getBoolean("is_active"),
                    rs.getInt("category"),
                    rs.getString("description"),
                    rs.getDouble("lat"),
                    rs.getDouble("lon"),
                    rs.getTimestamp("created_on").time,
                    sqlArray.toList())
        }
    }
}