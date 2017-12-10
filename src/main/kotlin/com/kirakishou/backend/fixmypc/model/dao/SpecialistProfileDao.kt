package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileDao {
    fun saveOne(specialistProfile: SpecialistProfile): Either<Throwable, Boolean>
    fun findOne(userId: Long): Either<Throwable, Fickle<SpecialistProfile>>
    fun findMany(userIdList: List<Long>): Either<Throwable, List<SpecialistProfile>>
    fun updateInfo(userId: Long, name: String, phone: String): Either<Throwable, Boolean>
    fun updatePhoto(userId: Long, photoName: String): Either<Throwable, Boolean>
}