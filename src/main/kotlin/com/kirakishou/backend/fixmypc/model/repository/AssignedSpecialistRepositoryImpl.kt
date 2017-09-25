package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import com.kirakishou.backend.fixmypc.model.repository.ignite.AssignedSpecialistCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.AssignedSpecialistDao
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Collectors

class AssignedSpecialistRepositoryImpl : AssignedSpecialistRepository {

    @Autowired
    private lateinit var dao: AssignedSpecialistDao

    @Autowired
    private lateinit var cache: AssignedSpecialistCache

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(assignedSpecialist: AssignedSpecialist): Boolean {
        val daoResult = dao.saveOne(assignedSpecialist)
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

    override fun findOne(damageClaimId: Long, isActive: Boolean): Fickle<AssignedSpecialist> {
        val cacheResult = cache.findOne(damageClaimId, isActive)
        if (cacheResult.isPresent()) {
            return cacheResult
        }

        val daoResult = dao.findOne(damageClaimId, isActive)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        } else {
            if (!(daoResult as Either.Value).value.isPresent()) {
                return Fickle.empty()
            }
        }

        cache.saveOne(daoResult.value.get())
        return daoResult.value
    }

    override fun findMany(damageClaimIdList: List<Long>, isActive: Boolean): List<AssignedSpecialist> {
        val cacheResult = cache.findMany(damageClaimIdList, isActive)
        if (cacheResult.size == damageClaimIdList.size) {
            return cacheResult
        }

        val notInCache = damageClaimIdList.stream()
                .filter { !contains(cacheResult, it) }
                .collect(Collectors.toList())

        val daoResult = dao.findMany(notInCache, isActive)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return cacheResult
        } else {
            if ((daoResult as Either.Value).value.isEmpty()) {
                return cacheResult
            }
        }

        cache.saveMany(daoResult.value)

        val result = ArrayList<AssignedSpecialist>()
        result.ensureCapacity(cacheResult.size + daoResult.value.size)

        result.addAll(cacheResult)
        result.addAll(daoResult.value)

        return result
    }

    private fun contains(list: List<AssignedSpecialist>, id: Long): Boolean {
        return list.firstOrNull { it.damageClaimId == id } != null
    }
}



































