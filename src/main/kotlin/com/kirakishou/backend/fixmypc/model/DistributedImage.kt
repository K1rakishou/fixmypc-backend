package com.kirakishou.backend.fixmypc.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DistributedImage(@JsonProperty("image_orig_name") val imageOrigName: String,
                            @JsonProperty("image_type") val imageType: Int,
                            @JsonProperty("image_name") val imageNewName: String,
                            @JsonProperty("owner_id") val ownerId: Long)