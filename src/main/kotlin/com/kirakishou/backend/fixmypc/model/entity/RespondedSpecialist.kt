package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName
import org.apache.ignite.cache.query.annotations.QuerySqlField

data class RespondedSpecialist(@QuerySqlField(index = true, name = "id")
                               @SerializedName("id")
                               var id: Long = -1L,

                               @QuerySqlField(index = true, name = "user_id")
                               @SerializedName("user_id")
                               val userId: Long = -1L,

                               @QuerySqlField(index = true, name = "damage_claim_id")
                               @SerializedName("damage_claim_id")
                               val damageClaimId: Long = -1L,

                               @QuerySqlField(name = "was_viewed")
                               @SerializedName("was_viewed")
                               var wasViewed: Boolean = false)