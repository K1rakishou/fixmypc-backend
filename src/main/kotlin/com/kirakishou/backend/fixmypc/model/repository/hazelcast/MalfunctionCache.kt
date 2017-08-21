package com.kirakishou.backend.fixmypc.model.repository.hazelcast

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction

interface MalfunctionCache {
    fun save(key: String, malfunction: Malfunction)
    fun get(key: String): Fickle<Malfunction>
    fun delete(key: String)
}