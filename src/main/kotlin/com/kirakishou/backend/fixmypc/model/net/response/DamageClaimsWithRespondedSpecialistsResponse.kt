package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist

class DamageClaimsWithRespondedSpecialistsResponse(@SerializedName(Constant.SerializedNames.DAMAGE_CLAIM_LIST)
                                                   val damageClaims: List<DamageClaim>,

                                                   @SerializedName(Constant.SerializedNames.RESPONDED_SPECIALISTS)
                                                   val respondedSpecialists: List<RespondedSpecialist>,

                                                   @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                                                   val errorCode: Int)