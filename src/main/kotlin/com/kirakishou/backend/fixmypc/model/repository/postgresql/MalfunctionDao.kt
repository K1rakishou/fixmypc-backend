package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import java.sql.SQLException

interface MalfunctionDao {
    //test won't work unless this method marked with @Throws annotation
    @Throws(SQLException::class)
    fun createNewMalfunctionRequest(malfunction: Malfunction)

    @Throws(SQLException::class)
    fun findMalfunctionRequestById(id: Long): Fickle<Malfunction>

    @Throws(SQLException::class)
    fun getAll(ownerId: Long, isActive: Boolean): List<Malfunction>

    @Throws(SQLException::class)
    fun getUserMalfunctionRequestList(ownerId: Long, isActive: Boolean, offset: Long, count: Int): List<Malfunction>

    @Throws(SQLException::class)
    fun deleteMalfunctionRequest(id: Long)
}