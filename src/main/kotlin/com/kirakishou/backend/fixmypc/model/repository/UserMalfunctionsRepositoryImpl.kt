package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.UserMalfunctionsStore
import com.kirakishou.backend.fixmypc.model.repository.postgresql.UserMalfunctionsDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class UserMalfunctionsRepositoryImpl : UserMalfunctionsRepository {

    @Autowired
    private lateinit var userMalfunctionsDao: UserMalfunctionsDao
    
    @Autowired
    private lateinit var userMalfunctionsStore: UserMalfunctionsStore
    
    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(ownerId: Long, malfunctionId: Long): Boolean {
        val daoResult = userMalfunctionsDao.saveOne(ownerId, malfunctionId)
        if (daoResult !is UserMalfunctionsDao.Result.Saved) {
            if (daoResult is UserMalfunctionsDao.Result.DbError) {
                log.e(daoResult.e)
                return false
            }
        }

        userMalfunctionsStore.saveOne(ownerId, malfunctionId)
        return true
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val cacheResult = userMalfunctionsStore.findMany(ownerId, offset, count)
        if (cacheResult.size == count.toInt()) {
            return cacheResult
        }

        val remainder = count - cacheResult.size
        val daoResult = userMalfunctionsDao.findAll(ownerId)

        if (daoResult !is UserMalfunctionsDao.Result.FoundMany) {
            return cacheResult
        }

        val ids = daoResult.idList
        val filteredIds = ids.stream()
                .skip(offset)
                .filter { !cacheResult.contains(it) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredIds.isEmpty()) {
            return cacheResult
        }

        userMalfunctionsStore.saveMany(ownerId, filteredIds)
        filteredIds.addAll(cacheResult)

        return filteredIds
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean {
        val daoResult = userMalfunctionsDao.deleteOne(ownerId, malfunctionId)
        if (daoResult !is UserMalfunctionsDao.Result.Saved) {
            if (daoResult is UserMalfunctionsDao.Result.DbError) {
                log.e(daoResult.e)
                return false
            }
        }

        userMalfunctionsStore.deleteOne(ownerId, malfunctionId)
        return true
    }
}