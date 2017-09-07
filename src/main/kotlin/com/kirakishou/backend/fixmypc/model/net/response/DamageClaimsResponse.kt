package com.kirakishou.backend.fixmypc.model.net.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim

data class DamageClaimsResponse(@JsonProperty("test")
                                val damageClaims: List<DamageClaim>,

                                @JsonProperty(Constant.SerializedNames.SERVER_ERROR_CODE)
                                val errorCode: Int)