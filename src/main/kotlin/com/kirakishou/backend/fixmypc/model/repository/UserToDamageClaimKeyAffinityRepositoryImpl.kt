package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserToDamageClaimKeyAffinityCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserToDamageClaimKeyAffinityDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class UserToDamageClaimKeyAffinityRepositoryImpl : UserToDamageClaimKeyAffinityRepository {

    @Autowired
    private lateinit var dao: UserToDamageClaimKeyAffinityDao
    
    @Autowired
    private lateinit var cache: UserToDamageClaimKeyAffinityCache
    
    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(ownerId: Long, malfunctionId: Long): Boolean {
        val daoResult = dao.saveOne(ownerId, malfunctionId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        cache.saveOne(ownerId, malfunctionId)
        return true
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val cacheResult = cache.findMany(ownerId, offset, count)
        if (cacheResult.size == count.toInt()) {
            return cacheResult
        }

        val remainder = count - cacheResult.size

        val daoResult = dao.findAll(ownerId)
        if (daoResult is Either.Error) {
            return cacheResult
        }

        val daoResValue = (daoResult as Either.Value).value
        if (daoResValue.isEmpty()) {
            return cacheResult
        }

        cache.saveMany(ownerId, daoResValue)

        val filteredIds = daoResValue.stream()
                .skip(offset)
                .filter { !cacheResult.contains(it) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredIds.isEmpty()) {
            return cacheResult
        }

        filteredIds.addAll(cacheResult)
        return filteredIds
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean {
        val daoResult = dao.deleteOne(ownerId, malfunctionId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        cache.deleteOne(ownerId, malfunctionId)
        return true
    }
}