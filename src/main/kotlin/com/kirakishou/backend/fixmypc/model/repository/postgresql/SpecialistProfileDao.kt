package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

interface SpecialistProfileDao {
    fun saveOne(specialistProfile: SpecialistProfile): Either<Throwable, Boolean>
    fun findMany(userIdList: List<Long>): Either<Throwable, List<SpecialistProfile>>
    //fun deleteOne(userId: Long): Either<Throwable, Boolean>
}