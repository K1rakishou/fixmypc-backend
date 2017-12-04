package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName

data class MarkResponseViewedRequest(@SerializedName("damage_claim_id")
                                     val damageClaimId: Long,

                                     @SerializedName("user_id")
                                     val userId: Long)