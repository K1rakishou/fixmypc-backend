package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import com.kirakishou.backend.fixmypc.model.repository.ignite.SpecialistProfileCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.SpecialistProfileDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class SpecialistProfileRepositoryImpl : SpecialistProfileRepository {

    @Autowired
    private lateinit var dao: SpecialistProfileDao

    @Autowired
    private lateinit var cache: SpecialistProfileCache

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(specialistProfile: SpecialistProfile): Boolean {
        val daoResult = dao.saveOne(specialistProfile)
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

    override fun findOne(userId: Long): Fickle<SpecialistProfile> {
        val cacheResult = cache.findOne(userId)
        if (cacheResult.isPresent()) {
            return cacheResult
        }

        val daoResult = dao.findOne(userId)
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

    override fun findMany(userIdList: List<Long>): List<SpecialistProfile> {
        val cacheResult = cache.findMany(userIdList)
        if (cacheResult.size == userIdList.size) {
            return cacheResult
        }

        val notInCache = userIdList.stream()
                .filter { !contains(cacheResult, it) }
                .collect(Collectors.toList())

        val daoResult = dao.findMany(notInCache)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return cacheResult
        } else {
            if ((daoResult as Either.Value).value.isEmpty()) {
                return cacheResult
            }
        }

        cache.saveMany(daoResult.value)

        val result = ArrayList<SpecialistProfile>()
        result.ensureCapacity(cacheResult.size + daoResult.value.size)

        result.addAll(cacheResult)
        result.addAll(daoResult.value)

        return result
    }

    override fun updateInfo(userId: Long, name: String, phone: String): Boolean {
        val daoResult = dao.updateInfo(userId, name, phone)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        cache.updateInfo(userId, name, phone)
        return true
    }

    override fun updatePhoto(userId: Long, photoName: String): Boolean {
        val daoResult = dao.updatePhoto(userId, photoName)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        cache.updatePhoto(userId, photoName)
        return true
    }

    private fun contains(list: List<SpecialistProfile>, id: Long): Boolean {
        return list.firstOrNull { it.userId == id } != null
    }
}