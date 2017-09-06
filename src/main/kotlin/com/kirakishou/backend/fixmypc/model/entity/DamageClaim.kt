package com.kirakishou.backend.fixmypc.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp

data class DamageClaim(@JsonProperty("id") var id: Long = 0L,
                       @JsonIgnore var ownerId: Long = 0L,
                       @JsonProperty("is_active") var isActive: Boolean = false,
                       @JsonIgnore var damageClaimRequestId: String = "",
                       @JsonProperty("category") var category: Int = 0,
                       @JsonProperty("description") var description: String = "",
                       @JsonProperty("lat") var lat: Double = 0.0,
                       @JsonProperty("lon") var lon: Double = 0.0,
                       @JsonProperty("created_on") var createdOn: Timestamp? = null,
                       @JsonProperty("photos") var imageNamesList: MutableList<String> = mutableListOf())