package com.kirakishou.backend.fixmypc.model.dto

import com.kirakishou.backend.fixmypc.model.entity.LatLon

data class DamageClaimIdLocationDTO(val id: Long,
                                    val location: LatLon)