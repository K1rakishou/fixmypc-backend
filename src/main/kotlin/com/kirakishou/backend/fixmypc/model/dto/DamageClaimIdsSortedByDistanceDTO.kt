package com.kirakishou.backend.fixmypc.model.dto

data class DamageClaimIdsSortedByDistanceDTO(val id: Long,
                                             val distance: Double,
                                             val metric: String)