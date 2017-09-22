package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.repository.ignite.DamageClaimCache
import com.kirakishou.backend.fixmypc.model.repository.ignite.LocationCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.DamageClaimDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

@Component
class DamageClaimRepositoryImpl : DamageClaimRepository {

    @Autowired
    private lateinit var damageClaimCache: DamageClaimCache

    @Autowired
    private lateinit var damageClaimDao: DamageClaimDao

    @Autowired
    private lateinit var locationCache: LocationCache

    @Autowired
    private lateinit var userToDamageClaimKeyAffinityRepository: UserToDamageClaimKeyAffinityRepository

    @Autowired
    private lateinit var log: FileLog

    override fun saveOne(damageClaim: DamageClaim): Boolean {
        val daoResult = damageClaimDao.saveOne(damageClaim)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return false
        } else {
            if (!(daoResult as Either.Value).value) {
                return false
            }
        }

        val repositoryResult = userToDamageClaimKeyAffinityRepository.saveOne(damageClaim.ownerId, damageClaim.id)
        if (!repositoryResult) {
            damageClaimDao.deleteOnePermanently(damageClaim.id)
            return false
        }

        locationCache.saveOne(LatLon(damageClaim.lat, damageClaim.lon), damageClaim.id)
        return true
    }

    override fun findOne(damageClaimId: Long): Fickle<DamageClaim> {
        val storeResult = damageClaimCache.findOne(damageClaimId)
        if (storeResult.isPresent()) {
            return storeResult
        }

        val daoResult = damageClaimDao.findOne(damageClaimId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return Fickle.empty()
        } else {
            if (!(daoResult as Either.Value).value.isPresent()) {
                return Fickle.empty()
            }
        }

        damageClaimCache.saveOne(daoResult.value.get())
        return daoResult.value
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<DamageClaim> {
        val malfunctionIdList = userToDamageClaimKeyAffinityRepository.findMany(ownerId, offset, count)
        if (malfunctionIdList.isEmpty()) {
            return emptyList()
        }

        val cacheResult = damageClaimCache.findMany(malfunctionIdList)

        if (cacheResult.size == count.toInt()) {
            return cacheResult
                    .stream()
                    .sorted(DamageClaimComparator())
                    .collect(Collectors.toList())
        }

        val daoResult = damageClaimDao.findManyActiveByOwnerId(ownerId)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return emptyList()
        }

        val malfunctionsFromDb = (daoResult as Either.Value).value
        val remainder = count - cacheResult.size

        val filteredMalfunctionList = malfunctionsFromDb.stream()
                .skip(offset)
                .filter { !containsDamageClaimId(it.id, cacheResult) }
                .limit(remainder)
                .collect(Collectors.toList())

        if (filteredMalfunctionList.isEmpty()) {
            return cacheResult
                    .stream()
                    .sorted(DamageClaimComparator())
                    .collect(Collectors.toList())
        }

        damageClaimCache.saveMany(filteredMalfunctionList)
        filteredMalfunctionList.addAll(0, cacheResult)

        return filteredMalfunctionList
                .stream()
                .sorted(DamageClaimComparator())
                .collect(Collectors.toList())
    }

    override fun findMany(idsToSearch: List<Long>): List<DamageClaim> {
        val cacheResult = damageClaimCache.findMany(idsToSearch) as ArrayList
        if (cacheResult.size == idsToSearch.size) {
            return cacheResult
        }

        val remainderList = idsToSearch.filter { !containsDamageClaimId(it, cacheResult) }
        val daoResult = damageClaimDao.findManyActiveByIdList(remainderList)
        if (daoResult is Either.Error) {
            log.e(daoResult.error)
            return cacheResult
        }

        val daoResultVal = (daoResult as Either.Value).value
        damageClaimCache.saveMany(daoResultVal)
        cacheResult.addAll(daoResultVal)

        return cacheResult
    }

    override fun deleteOne(ownerId: Long, damageClaimId: Long): Boolean {
        try {
            val daoResult = damageClaimDao.deleteOne(damageClaimId)
            if (daoResult is Either.Error) {
                return false
            } else {
                if (!(daoResult as Either.Value).value) {
                    return false
                }
            }

            userToDamageClaimKeyAffinityRepository.deleteOne(ownerId, damageClaimId)
            locationCache.deleteOne(damageClaimId)

            return true
        } catch (e: Exception) {
            log.e(e)
        }

        return false
    }

    private class DamageClaimComparator : Comparator<DamageClaim> {
        override fun compare(dc1: DamageClaim, dc2: DamageClaim): Int {
            if (dc1.id < dc2.id) {
                return -1
            } else if (dc1.id > dc2.id) {
                return 1
            }

            return 0
        }
    }

    private fun containsDamageClaimId(id: Long, malfunctionsList: List<DamageClaim>): Boolean {
        for (malfunction in malfunctionsList) {
            if (malfunction.id == id) {
                return true
            }
        }

        return false
    }
}