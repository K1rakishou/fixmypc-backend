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
        val idsList = userMalfunctionsStore.findMany(ownerId, offset, count)
        if (idsList.size == count.toInt()) {
            return idsList
        }

        val remainder = count - idsList.size
        val daoResult = userMalfunctionsDao.findAll(ownerId)
        if (daoResult !is UserMalfunctionsDao.Result.FoundMany) {
            return idsList
        }

        val filteredIds = daoResult.idList.stream()
                .filter { !idsList.contains(it) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredIds.isEmpty()) {
            return idsList
        }

        userMalfunctionsStore.saveMany(ownerId, filteredIds)
        filteredIds.addAll(idsList)

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