package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.io.Serializable


data class DamageClaim(@SerializedName("id")
                       @QuerySqlField(index = true, name = "id")
                       var id: Long = 0L,

                       @QuerySqlField(index = true, name = "user_id")
                       @SerializedName("owner_id")
                       var userId: Long = 0L,

                       @QuerySqlField(name = "is_active")
                       @SerializedName("is_active")
                       var isActive: Boolean = false,

                       @SerializedName("category")
                       var category: Int = 0,

                       @SerializedName("description")
                       var description: String = "",

                       @SerializedName("lat")
                       var lat: Double = 0.0,

                       @SerializedName("lon")
                       var lon: Double = 0.0,

                       @SerializedName("created_on")
                       var createdOn: Long = 0L,

                       @SerializedName("photos")
                       var imageNamesList: MutableList<String> = mutableListOf()) : Serializable