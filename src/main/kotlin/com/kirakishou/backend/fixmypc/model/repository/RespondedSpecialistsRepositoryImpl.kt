package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import com.kirakishou.backend.fixmypc.model.repository.ignite.RespondedSpecialistsCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.RespondedSpecialistsDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class RespondedSpecialistsRepositoryImpl : RespondedSpecialistsRepository {

    @Autowired
    lateinit var dao: RespondedSpecialistsDao

    @Autowired
    lateinit var cache: RespondedSpecialistsCache

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean {
        val daoResult = dao.saveOne(respondedSpecialist)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        return true
    }

    override fun findAllForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist> {
        val cacheResult = cache.findManyForDamageClaimPaged(damageClaimId, skip, count)
        if (cacheResult.size == count.toInt()) {
            return cacheResult
        }

        val daoResult = dao.findAllForDamageClaimPaged(damageClaimId, skip, count)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return emptyList()
        }

        val specialistsFromDb = (daoResult as Either.Value).value
        val remainder = count - cacheResult.size

        val filteredSpecialistsList = specialistsFromDb.stream()
                .skip(skip)
                .filter { !containsSpecialistId(it.id, cacheResult) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredSpecialistsList.isEmpty()) {
            return cacheResult
        }

        cache.saveMany(damageClaimId, filteredSpecialistsList)
        filteredSpecialistsList.addAll(0, cacheResult)

        return filteredSpecialistsList
    }

    override fun containsOne(userId: Long, damageClaimId: Long): Boolean {
        val cacheResult = cache.findOne(userId, damageClaimId)
        if (cacheResult.isPresent()) {
            return true
        }

        val daoResult = dao.findOne(userId, damageClaimId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        }

        val specialistFickle = (daoResult as Either.Value).value
        if (!specialistFickle.isPresent()) {
            return false
        }

        cache.saveOne(specialistFickle.get())
        return true
    }

    override fun deleteAllForDamageClaim(damageClaimId: Long): Boolean {
        val daoResult = dao.deleteAllForDamageClaim(damageClaimId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        cache.deleteAllForDamageClaim(damageClaimId)
        return true
    }

    private fun containsSpecialistId(id: Long, cacheResult: List<RespondedSpecialist>): Boolean {
        for (res in cacheResult) {
            if (res.id == id) {
                return true
            }
        }

        return false
    }
}

























