package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.LatLon
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
        val malfunctionDaoResult = malfunctionDao.saveOne(malfunction)
        if (malfunctionDaoResult !is MalfunctionDao.Result.Saved) {
            if (malfunctionDaoResult is MalfunctionDao.Result.DbError) {
                log.e(malfunctionDaoResult.e)
                return false
            }
        }

        val userMalfunctionRepositoryResult = userMalfunctionsRepository.saveOne(malfunction.ownerId, malfunction.id)
        if (!userMalfunctionRepositoryResult) {
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
        if (daoResult !is MalfunctionDao.Result.FoundOne) {
            if (daoResult is MalfunctionDao.Result.DbError) {
                log.e(daoResult.e)
            }

            return Fickle.empty()
        }

        return Fickle.of(daoResult.malfunction)
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Malfunction> {
        val ids = userMalfunctionsRepository.findMany(ownerId, offset, count)
        if (ids.isEmpty()) {
            return emptyList()
        }

        val malfunctionsList = malfunctionStore.findMany(ids)
        if (malfunctionsList.size == count.toInt()) {
            return malfunctionsList
        }

        val remainder = count - malfunctionsList.size
        val malfunctionDaoResult = malfunctionDao.findManyActive(ownerId)
        if (malfunctionDaoResult !is MalfunctionDao.Result.FoundMany) {
            when (malfunctionDaoResult) {
                is MalfunctionDao.Result.DbError -> {
                    log.e(malfunctionDaoResult.e)
                    return emptyList()
                }
            }
        }

        val malfunctionsFromDb = malfunctionDaoResult.malfunctions
        val filteredMF = malfunctionsFromDb.stream()
                .filter { !contains(it.id, malfunctionsList) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredMF.isEmpty()) {
            return emptyList()
        }

        malfunctionStore.saveMany(filteredMF)
        filteredMF.addAll(malfunctionsList)

        return filteredMF
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Boolean {
        try {
            malfunctionDao.deleteOne(malfunctionId)
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