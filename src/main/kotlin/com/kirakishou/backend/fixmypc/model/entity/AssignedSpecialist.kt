package com.kirakishou.backend.fixmypc.model.entity

import java.io.Serializable

data class AssignedSpecialist(val damageClaimId: Long,
                              val specialistUserId: Long,
                              val whoAssignedUserId: Long,
                              val isWorkDone: Boolean) : Serializable