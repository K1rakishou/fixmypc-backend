package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName

data class SpecialistProfile(@SerializedName("user_id")
                             val userId: Long,

                             @SerializedName("name")
                             var name: String,

                             @SerializedName("rating")
                             val rating: Float,

                             @SerializedName("photo_name")
                             var photoName: String,

                             @SerializedName("phone")
                             var phone: String,

                             @SerializedName("registered_on")
                             val registeredOn: Long,

                             @SerializedName("success_repairs")
                             val successRepairs: Int,

                             @SerializedName("fail_repairs")
                             val failRepairs: Int,

                             @SerializedName("is_filled_in")
                             var isFilledIn: Boolean = false)