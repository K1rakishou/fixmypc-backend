package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

data class RespondToDamageClaimRequest(@SerializedName(Constant.SerializedNames.DAMAGE_CLAIM_ID)
                                       val damageClaimId: Long)