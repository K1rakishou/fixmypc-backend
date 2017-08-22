package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionCache {
    fun save(key: Long, malfunction: Malfunction)
    fun get(key: Long): List<Malfunction>
    fun delete(key: Long)
}