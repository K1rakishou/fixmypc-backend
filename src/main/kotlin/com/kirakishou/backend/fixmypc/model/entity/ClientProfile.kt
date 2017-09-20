package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName


data class ClientProfile(@SerializedName("user_id")
                         val userId: Long = 0L,

                         @SerializedName("name")
                         val name: String = "",

                         @SerializedName("phone")
                         val phone: String = "",

                         @Transient
                         val isFilledOut: Boolean = false)