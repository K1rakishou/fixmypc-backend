package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.model.entity.LatLon

interface LocationStore {
    fun saveOne(location: LatLon, malfunctionId: Long)
    fun findWithin(skip: Long, centroid: LatLon, radius: Double, count: Long): List<Long>
    fun deleteOne(malfunctionId: Long)
}