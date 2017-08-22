package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.MalfunctionCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.MalfunctionDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class MalfunctionRepositoryImpl : MalfunctionRepository {

    @Autowired
    private lateinit var malfunctionCache: MalfunctionCache

    @Autowired
    private lateinit var malfunctionDao: MalfunctionDao

    override fun createMalfunction(malfunction: Malfunction) {
        malfunctionDao.createNewMalfunctionRequest(malfunction)
        malfunctionCache.save(malfunction.owner_id, malfunction)
    }

    override fun getMalfunctionById(ownerId: Long, malfunctionId: Long): Fickle<Malfunction> {
        return malfunctionCache.get(ownerId, malfunctionId)
    }

    override fun getUserMalfunctions(ownerId: Long, offset: Long, count: Long): List<Malfunction> {
        val malfunctionsList = malfunctionCache.getSome(ownerId, offset, count)
        if (malfunctionsList.size == count.toInt()) {
            return malfunctionsList
        }

        val remainder = count - malfunctionsList.size
        val malfunctionsFromDb = malfunctionDao.getAllUserMalfunctions(ownerId)
        val filteredMF = malfunctionsFromDb.stream()
                .filter { mf -> malfunctionsList.contains(mf) }
                .limit(remainder)
                .collect(Collectors.toList())

        malfunctionCache.saveMany(ownerId, malfunctionsFromDb)

        filteredMF.addAll(malfunctionsList)
        return malfunctionsFromDb
    }

    override fun deleteMalfunction(ownerId: Long, malfunctionId: Long) {
        malfunctionCache.delete(ownerId, malfunctionId)
    }
}