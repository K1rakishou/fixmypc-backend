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
    private lateinit var userToDamageClaimKeyAffinityDao: UserToDamageClaimKeyAffinityDao
    
    @Autowired
    private lateinit var userToDamageClaimKeyAffinityCache: UserToDamageClaimKeyAffinityCache
    
    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(ownerId: Long, malfunctionId: Long): Boolean {
        val daoResult = userToDamageClaimKeyAffinityDao.saveOne(ownerId, malfunctionId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        userToDamageClaimKeyAffinityCache.saveOne(ownerId, malfunctionId)
        return true
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val cacheResult = userToDamageClaimKeyAffinityCache.findMany(ownerId, offset, count)
        if (cacheResult.size == count.toInt()) {
            return cacheResult
        }

        val remainder = count - cacheResult.size

        val daoResult = userToDamageClaimKeyAffinityDao.findAll(ownerId)
        if (daoResult is Either.Error) {
            return cacheResult
        }

        val daoResValue = (daoResult as Either.Value).value
        if (daoResValue.isEmpty()) {
            return cacheResult
        }

        userToDamageClaimKeyAffinityCache.saveMany(ownerId, daoResValue)

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
        val daoResult = userToDamageClaimKeyAffinityDao.deleteOne(ownerId, malfunctionId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        userToDamageClaimKeyAffinityCache.deleteOne(ownerId, malfunctionId)
        return true
    }
}