package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class DamageClaim(@SerializedName("id") var id: Long = 0L,
                       @SerializedName("owner_id") var ownerId: Long = 0L,
                       @SerializedName("is_active") var isActive: Boolean = false,
                       @SerializedName("category") var category: Int = 0,
                       @SerializedName("description") var description: String = "",
                       @SerializedName("lat") var lat: Double = 0.0,
                       @SerializedName("lon") var lon: Double = 0.0,
                       @SerializedName("created_on") var createdOn: Long = 0L,
                       @SerializedName("photos") var imageNamesList: MutableList<String> = mutableListOf()) : Serializable