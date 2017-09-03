package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.model.entity.LatLon

interface LocationStore {
    fun saveOne(location: LatLon, malfunctionId: Long)
    fun deleteOne(malfunctionId: Long)
}