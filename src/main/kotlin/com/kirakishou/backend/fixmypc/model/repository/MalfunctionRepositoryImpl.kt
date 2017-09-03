package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.MalfunctionStore
import com.kirakishou.backend.fixmypc.model.repository.ignite.LocationStore
import com.kirakishou.backend.fixmypc.model.repository.postgresql.MalfunctionDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class MalfunctionRepositoryImpl : MalfunctionRepository {

    @Autowired
    private lateinit var malfunctionStore: MalfunctionStore

    @Autowired
    private lateinit var malfunctionDao: MalfunctionDao

    @Autowired
    private lateinit var locationStore: LocationStore

    @Autowired
    private lateinit var userMalfunctionsRepository: UserMalfunctionsRepository

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(malfunction: Malfunction): Boolean {
        val daoResult = malfunctionDao.saveOne(malfunction)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        val repositoryResult = userMalfunctionsRepository.saveOne(malfunction.ownerId, malfunction.id)
        if (!repositoryResult) {
            //couldn't store in the userMalfunctionsRepository so we need to delete it from DB as well
            malfunctionDao.deleteOnePermanently(malfunction.id)
            return false
        }

        locationStore.saveOne(LatLon(malfunction.lat, malfunction.lon), malfunction.id)
        return true
    }

    override fun findOne(malfunctionId: Long): Fickle<Malfunction> {
        val storeResult = malfunctionStore.findOne(malfunctionId)
        if (storeResult.isPresent()) {
            return storeResult
        }

        val daoResult = malfunctionDao.findOne(malfunctionId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        }

        return (daoResult as Either.Value).value
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Malfunction> {
        val malfunctionIdList = userMalfunctionsRepository.findMany(ownerId, offset, count)
        if (malfunctionIdList.isEmpty()) {
            return emptyList()
        }

        val cacheResult = malfunctionStore.findMany(malfunctionIdList)
        if (cacheResult.size == count.toInt()) {
            return cacheResult
        }

        val daoResult = malfunctionDao.findManyActive(ownerId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return emptyList()
        }

        val malfunctionsFromDb = (daoResult as Either.Value).value
        val remainder = count - cacheResult.size

        val filteredMalfunctionList = malfunctionsFromDb.stream()
                .skip(offset)
                .filter { !contains(it.id, cacheResult) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredMalfunctionList.isEmpty()) {
            return cacheResult
        }

        malfunctionStore.saveMany(filteredMalfunctionList)
        filteredMalfunctionList.addAll(0, cacheResult)

        return filteredMalfunctionList
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean {
        try {
            val daoResult = malfunctionDao.deleteOne(malfunctionId)
            if (daoResult is Either.Error) {
                return false
            } else {
                if (!(daoResult as Either.Value).value) {
                    return false
                }
            }

            userMalfunctionsRepository.deleteOne(ownerId, malfunctionId)
            locationStore.deleteOne(malfunctionId)

            return true
        } catch (e: Exception) {
            log.e(e)
        }

        return false
    }

    private fun contains(id: Long, malfunctionsList: List<Malfunction>): Boolean {
        for (malfunction in malfunctionsList) {
            if (malfunction.id == id) {
                return true
            }
        }

        return false
    }
}