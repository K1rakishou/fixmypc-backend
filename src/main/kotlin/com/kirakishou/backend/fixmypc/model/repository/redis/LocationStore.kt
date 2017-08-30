package com.kirakishou.backend.fixmypc.model.repository.redis

interface LocationStore {
    fun addLocation(lat: Double, lon: Double, malfunctionId: Long)
    fun removeLocation(malfunctionId: Long)
}