package com.kirakishou.backend.fixmypc.model.entity

import java.io.Serializable

data class AssignedSpecialist(val damageClaimId: Long,
                              val userId: Long,
                              val isActive: Boolean) : Serializable