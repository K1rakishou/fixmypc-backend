package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserMalfunctionsCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserMalfunctionsDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class UserMalfunctionsRepositoryImpl : UserMalfunctionsRepository {

    @Autowired
    private lateinit var userMalfunctionsDao: UserMalfunctionsDao
    
    @Autowired
    private lateinit var userMalfunctionsCache: UserMalfunctionsCache
    
    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(ownerId: Long, malfunctionId: Long): Boolean {
        val daoResult = userMalfunctionsDao.saveOne(ownerId, malfunctionId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        userMalfunctionsCache.saveOne(ownerId, malfunctionId)
        return true
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val cacheResult = userMalfunctionsCache.findMany(ownerId, offset, count)
        if (cacheResult.size == count.toInt()) {
            log.d("UserMalfunctionsRepository Found enough ids in the cache")
            return cacheResult
        }

        val remainder = count - cacheResult.size
        log.d("UserMalfunctionsRepository Found ${cacheResult.size} out of $count ids in the cache. Searching for the rest in the DB")

        val daoResult = userMalfunctionsDao.findAll(ownerId)
        if (daoResult is Either.Error) {
            log.d("UserMalfunctionsRepository DB threw error ${daoResult.error}")
            return cacheResult
        }

        val daoResValue = (daoResult as Either.Value).value
        if (daoResValue.isEmpty()) {
            log.d("UserMalfunctionsRepository Found no ids in the DB")
            return cacheResult
        }

        log.d("UserMalfunctionsRepository Found ${daoResValue.size} ids in the DB. Caching them.")
        userMalfunctionsCache.saveMany(ownerId, daoResValue)

        val filteredIds = daoResValue.stream()
                .skip(offset)
                .filter { !cacheResult.contains(it) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredIds.isEmpty()) {
            log.d("UserMalfunctionsRepository No ids left after filtering. Returning ${cacheResult.size} ids from the cache")
            return cacheResult
        }

        filteredIds.addAll(cacheResult)
        log.d("UserMalfunctionsRepository Returning total ${filteredIds.size} ids")

        return filteredIds
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean {
        val daoResult = userMalfunctionsDao.deleteOne(ownerId, malfunctionId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        userMalfunctionsCache.deleteOne(ownerId, malfunctionId)
        return true
    }
}