package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName

data class RespondedSpecialist(@SerializedName("id")
                               var id: Long = -1L,

                               @SerializedName("user_id")
                               val userId: Long = -1L,

                               @SerializedName("damage_claim_id")
                               val damageClaimId: Long = -1L,

                               @SerializedName("was_viewed")
                               var wasViewed: Boolean = false)