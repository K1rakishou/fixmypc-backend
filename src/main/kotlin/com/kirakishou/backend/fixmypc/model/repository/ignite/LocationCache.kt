package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdsSortedByDistanceDTO
import com.kirakishou.backend.fixmypc.model.entity.LatLon

interface LocationCache {
    fun saveOne(location: LatLon, malfunctionId: Long)
    fun findWithin(page: Long, centroid: LatLon, radius: Double, count: Long): List<DamageClaimIdsSortedByDistanceDTO>
    fun deleteOne(malfunctionId: Long)
}