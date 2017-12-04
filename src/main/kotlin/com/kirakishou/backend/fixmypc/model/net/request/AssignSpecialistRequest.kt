package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName

data class AssignSpecialistRequest(@SerializedName("specialist_user_id")
                                   val specialistUserId: Long,

                                   @SerializedName("damage_claim_id")
                                   val damageClaimId: Long)