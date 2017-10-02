package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import com.kirakishou.backend.fixmypc.model.repository.ignite.AssignedSpecialistCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.AssignedSpecialistDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
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

    override fun findOne(damageClaimId: Long, isWorkDone: Boolean): Fickle<AssignedSpecialist> {
        val cacheResult = cache.findOne(damageClaimId, isWorkDone)
        if (cacheResult.isPresent()) {
            return cacheResult
        }

        val daoResult = dao.findOne(damageClaimId, isWorkDone)
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

    override fun findMany(damageClaimIdList: List<Long>, isWorkDone: Boolean): List<AssignedSpecialist> {
        val cacheResult = cache.findMany(damageClaimIdList, isWorkDone)
        if (cacheResult.size == damageClaimIdList.size) {
            return cacheResult
        }

        val notInCache = damageClaimIdList.stream()
                .filter { !contains(cacheResult, it) }
                .collect(Collectors.toList())

        val daoResult = dao.findMany(notInCache, isWorkDone)
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



































