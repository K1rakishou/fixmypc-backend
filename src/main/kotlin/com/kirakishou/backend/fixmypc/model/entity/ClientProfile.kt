package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ClientProfile(@SerializedName("user_id")
                         val userId: Long = -1L,

                         @SerializedName("name")
                         val name: String = "",

                         @SerializedName("phone")
                         val phone: String = "",

                         @SerializedName("photo_folder")
                         var photoFolder: String = "",

                         @SerializedName("photo_name")
                         var photoName: String = "",

                         @Transient
                         var registeredOn: Long = 0L) : Serializable {

    fun isProfileInfoFilledIn(): Boolean {
        return (this.name.isNotEmpty() && this.phone.isNotEmpty() && this.photoName.isNotEmpty())
    }
}