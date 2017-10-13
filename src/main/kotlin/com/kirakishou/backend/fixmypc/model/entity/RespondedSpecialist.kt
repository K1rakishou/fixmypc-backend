package com.kirakishou.backend.fixmypc.model.entity

import org.apache.ignite.cache.query.annotations.QuerySqlField

data class RespondedSpecialist(@QuerySqlField(index = true, name = "id")
                               var id: Long = -1L,

                               val userId: Long = -1L,

                               @QuerySqlField(index = true, name = "damage_claim_id")
                               val damageClaimId: Long = -1L)