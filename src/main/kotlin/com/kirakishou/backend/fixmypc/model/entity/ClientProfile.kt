package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ClientProfile(@SerializedName("user_id")
                         val userId: Long = -1L,

                         @SerializedName("name")
                         var name: String = "",

                         @SerializedName("phone")
                         var phone: String = "",

                         @Transient
                         var registeredOn: Long = 0L) : Serializable {

    fun isProfileInfoFilledIn(): Boolean {
        return (this.name.isNotEmpty() && this.phone.isNotEmpty())
    }


}