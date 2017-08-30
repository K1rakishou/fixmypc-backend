package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.MalfunctionStore
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.UserMalfunctionsStore
import com.kirakishou.backend.fixmypc.model.repository.postgresql.MalfunctionDao
import com.kirakishou.backend.fixmypc.model.repository.redis.LocationStore
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
    private lateinit var userMalfunctionsStore: UserMalfunctionsStore

    @Autowired
    private lateinit var log: FileLog

    override fun createMalfunction(malfunction: Malfunction): Boolean {
        var isOk = true

        try {
            malfunctionDao.createNewMalfunctionRequest(malfunction)
        } catch (e: Exception) {
            isOk = false
            log.e(e)

            malfunctionDao.deleteMalfunctionRequest(malfunction.id)
        }

        if (isOk) {
            try {
                malfunctionStore.save(malfunction)
            } catch (e: Exception) {
                isOk = false
                log.e(e)

                malfunctionStore.delete(malfunction.id)
            }
        }

        if (isOk) {
            try {
                locationStore.addLocation(malfunction.lat, malfunction.lon, malfunction.id)
            } catch (e: Exception) {
                isOk = false
                log.e(e)

                locationStore.removeLocation(malfunction.id)
            }
        }

        return isOk
    }

    override fun get(malfunctionId: Long): Fickle<Malfunction> {
        return malfunctionStore.get(malfunctionId)
    }

    override fun getMany(ownerId: Long, offset: Long, count: Long): List<Malfunction> {
        val ids = userMalfunctionsStore.getMany(ownerId, offset, count)
        if (ids.isEmpty()) {
            return emptyList()
        }

        val malfunctionsList = malfunctionStore.getMany(ids)
        if (malfunctionsList.size == count.toInt()) {
            return malfunctionsList
        }

        val remainder = count - malfunctionsList.size
        val malfunctionsFromDb = malfunctionDao.getAll(ownerId, true)
        val filteredMF = malfunctionsFromDb.stream()
                .filter { !contains(it.id, malfunctionsList) }
                .limit(remainder)
                .collect(Collectors.toList())

        malfunctionStore.saveMany(filteredMF)
        filteredMF.addAll(malfunctionsList)

        return malfunctionsFromDb
    }

    private fun contains(id: Long, malfunctionsList: List<Malfunction>): Boolean {
        for (malfunction in malfunctionsList) {
            if (malfunction.id == id) {
                return true
            }
        }

        return false
    }

    override fun deleteMalfunction(ownerId: Long, malfunctionId: Long): Boolean {
        try {
            malfunctionDao.deleteMalfunctionRequest(malfunctionId)
            malfunctionStore.delete(malfunctionId)
            locationStore.removeLocation(malfunctionId)

            return true
        } catch (e: Exception) {
            log.e(e)
        }

        return false
    }
}