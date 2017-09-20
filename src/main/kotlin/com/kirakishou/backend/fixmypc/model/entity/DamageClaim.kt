package com.kirakishou.backend.fixmypc.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class DamageClaim(@JsonProperty("id") var id: Long = 0L,
                       @JsonProperty("owner_id") var ownerId: Long = 0L,
                       @JsonProperty("is_active") var isActive: Boolean = false,
                       @JsonIgnore var folderName: String = "",
                       @JsonProperty("category") var category: Int = 0,
                       @JsonProperty("description") var description: String = "",
                       @JsonProperty("lat") var lat: Double = 0.0,
                       @JsonProperty("lon") var lon: Double = 0.0,
                       @JsonProperty("created_on") var createdOn: Long = 0L,
                       @JsonProperty("photos") var imageNamesList: MutableList<String> = mutableListOf())