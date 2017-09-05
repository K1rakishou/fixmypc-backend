package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.repository.ignite.LocationCache
import com.kirakishou.backend.fixmypc.model.repository.ignite.MalfunctionCache
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

    @Autowired
    private lateinit var locationCache: LocationCache

    @Autowired
    private lateinit var userMalfunctionsRepository: UserMalfunctionsRepository

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(damageClaim: DamageClaim): Boolean {
        val daoResult = malfunctionDao.saveOne(damageClaim)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        val repositoryResult = userMalfunctionsRepository.saveOne(damageClaim.ownerId, damageClaim.id)
        if (!repositoryResult) {
            //couldn't store in the userMalfunctionsRepository so we need to delete it from DB as well
            malfunctionDao.deleteOnePermanently(damageClaim.id)
            return false
        }

        locationCache.saveOne(LatLon(damageClaim.lat, damageClaim.lon), damageClaim.id)
        return true
    }

    override fun findOne(malfunctionId: Long): Fickle<DamageClaim> {
        val storeResult = malfunctionCache.findOne(malfunctionId)
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

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<DamageClaim> {
        val malfunctionIdList = userMalfunctionsRepository.findMany(ownerId, offset, count)
        if (malfunctionIdList.isEmpty()) {
            log.d("MalfunctionRepository No ids found in id repository. Returning emptyList")
            return emptyList()
        }

        log.d("MalfunctionRepository Found ${malfunctionIdList.size} out of $count ids in the repository")
        val cacheResult = malfunctionCache.findMany(malfunctionIdList)

        if (cacheResult.size == count.toInt()) {
            log.d("MalfunctionRepository That's enough, returning them")
            return sort(cacheResult)
        }

        log.d("MalfunctionRepository Found ${cacheResult.size} out of $count items in the cache. Searching the rest in the DB")
        val daoResult = malfunctionDao.findManyActive(ownerId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return emptyList()
        }

        val malfunctionsFromDb = (daoResult as Either.Value).value
        val remainder = count - cacheResult.size
        log.d("MalfunctionRepository Found ${malfunctionsFromDb.size} items in the DB")

        val filteredMalfunctionList = malfunctionsFromDb.stream()
                .skip(offset)
                .filter { !contains(it.id, cacheResult) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredMalfunctionList.isEmpty()) {
            log.d("MalfunctionRepository Nothing left after filtering DB items, returning cached items")
            return sort(cacheResult)
        }

        log.d("MalfunctionRepository Caching DB items")
        malfunctionCache.saveMany(filteredMalfunctionList)
        filteredMalfunctionList.addAll(0, cacheResult)

        log.d("MalfunctionRepository Total items returned ${filteredMalfunctionList.size}")
        return sort(filteredMalfunctionList)
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
            locationCache.deleteOne(malfunctionId)

            return true
        } catch (e: Exception) {
            log.e(e)
        }

        return false
    }

    private fun sort(damageClaimList: List<DamageClaim>): List<DamageClaim> {
        return damageClaimList.stream()
                .sorted { mf1, mf2 -> comparator(mf1, mf2) }
                .collect(Collectors.toList())
    }

    private fun comparator(mf1: DamageClaim, mf2: DamageClaim): Int {
        if (mf1.id < mf2.id) {
            return -1
        } else if (mf1.id > mf2.id) {
            return 1
        }

        return 0
    }

    private fun contains(id: Long, malfunctionsList: List<DamageClaim>): Boolean {
        for (malfunction in malfunctionsList) {
            if (malfunction.id == id) {
                return true
            }
        }

        return false
    }
}