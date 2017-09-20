package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

data class DamageClaimsResponse(@SerializedName("test")
                                val damageClaims: List<DamageClaim>,

                                @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                                val errorCode: Int)