package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileDao {
    fun saveOne(specialistProfile: SpecialistProfile): Either<Throwable, Boolean>
    fun findOne(userId: Long): Either<Throwable, Fickle<SpecialistProfile>>
    fun findMany(userIdList: List<Long>): Either<Throwable, List<SpecialistProfile>>
    fun update(userId: Long, name: String, phone: String, photoName: String): Either<Throwable, Boolean>
    //fun deleteOne(userId: Long): Either<Throwable, Boolean>
}