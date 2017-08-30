package com.kirakishou.backend.fixmypc.model.repository

interface UserMalfunctionsRepository {
    fun addUserMalfunction(ownerId: Long, malfunctionId: Long): Boolean
    fun getMany(ownerId: Long, offset: Long, count: Long): List<Long>
    fun removeUserMalfunction(ownerId: Long, malfunctionId: Long): Boolean
}