package com.kirakishou.backend.fixmypc.model.repository.postgresql

import java.sql.SQLException

interface UserMalfunctionsDao {
    @Throws(SQLException::class)
    fun addMalfunction(ownerId: Long, malfunctionId: Long)

    @Throws(SQLException::class)
    fun getMany(ownerId: Long, offset: Long, count: Long): List<Long>

    @Throws(SQLException::class)
    fun getAll(ownerId: Long): List<Long>

    @Throws(SQLException::class)
    fun removeMalfunction(ownerId: Long, malfunctionId: Long)
}