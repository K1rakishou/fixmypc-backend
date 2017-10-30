package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.DamageClaimResponseCount

class DamageClaimsWithCountResponse(@SerializedName(Constant.SerializedNames.DAMAGE_CLAIM_LIST)
                                    val damageClaims: List<DamageClaim>,

                                    @SerializedName(Constant.SerializedNames.RESPONSES_COUNT_LIST)
                                    val responsesCountList: List<DamageClaimResponseCount>,

                                    @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                                    val errorCode: Int)