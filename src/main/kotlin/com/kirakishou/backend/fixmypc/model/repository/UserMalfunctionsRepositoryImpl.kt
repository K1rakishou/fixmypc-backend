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

    override fun addUserMalfunction(ownerId: Long, malfunctionId: Long): Boolean {
        var isOk = true

        try {
            userMalfunctionsDao.addMalfunction(ownerId, malfunctionId)
        } catch (e: Exception) {
            isOk = false
            log.e(e)

            userMalfunctionsDao.removeMalfunction(ownerId, malfunctionId)
        }

        if (isOk) {
            try {
                userMalfunctionsStore.addMalfunction(ownerId, malfunctionId)
            } catch (e: Exception) {
                isOk = false
                log.e(e)

                userMalfunctionsStore.removeMalfunction(ownerId, malfunctionId)
            }
        }

        return isOk
    }

    override fun getMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val idsList = userMalfunctionsStore.getMany(ownerId, offset, count)
        if (idsList.size == count.toInt()) {
            return idsList
        }

        val remainder = count - idsList.size
        val idsFromDb = userMalfunctionsDao.getAll(ownerId)
        val filteredIds = idsFromDb.stream()
                .filter { !idsList.contains(it) }
                .limit(remainder)
                .collect(Collectors.toList())

        userMalfunctionsStore.addMany(ownerId, filteredIds)
        filteredIds.addAll(idsList)

        return filteredIds
    }

    override fun removeUserMalfunction(ownerId: Long, malfunctionId: Long): Boolean {
        try {
            userMalfunctionsDao.removeMalfunction(ownerId, malfunctionId)
            userMalfunctionsStore.removeMalfunction(ownerId, malfunctionId)
        } catch (e: Exception) {
            return false
        }

        return true
    }
}