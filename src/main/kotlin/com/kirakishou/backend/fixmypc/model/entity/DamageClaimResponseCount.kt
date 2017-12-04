package com.kirakishou.backend.fixmypc.model.entity

import com.google.gson.annotations.SerializedName

data class DamageClaimResponseCount(@SerializedName("damage_claim_id")
                                    val damageClaimId: Long,

                                    @SerializedName("response_count")
                                    val responseCount: Int)