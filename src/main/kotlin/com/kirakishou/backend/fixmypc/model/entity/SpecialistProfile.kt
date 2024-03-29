package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName

data class SpecialistProfile(@SerializedName("user_id")
                             var userId: Long = -1L,

                             @SerializedName("name")
                             var name: String = "",

                             @SerializedName("rating")
                             val rating: Float = 0f,

                             @SerializedName("photo_name")
                             var photoName: String = "",

                             @SerializedName("phone")
                             var phone: String = "",

                             @SerializedName("registered_on")
                             val registeredOn: Long = 0L,

                             @SerializedName("success_repairs")
                             val successRepairs: Int = 0,

                             @SerializedName("fail_repairs")
                             val failRepairs: Int = 0) {

    fun isProfileInfoFilledIn(): Boolean {
        return (this.name.isNotEmpty() && this.phone.isNotEmpty() && this.photoName.isNotEmpty())
    }
}