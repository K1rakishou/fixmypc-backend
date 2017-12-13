package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.dao.DamageClaimDao
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.RedisTemplate
import java.util.stream.Collectors
import javax.sql.DataSource
import kotlin.system.measureTimeMillis

class LocationStoreImpl(
        private val hikariCP: DataSource,
        private val template: RedisTemplate<String, Long>,
        private val damageClaimDao: DamageClaimDao,
        private val fileLog: FileLog
) : LocationStore {

    init {
        fillWithData()
    }

    override fun saveOne(location: LatLon, malfunctionId: Long) {
        template.opsForGeo().geoAdd(Constant.RedisNames.LOCATION_CACHE_NAME, Point(location.lon, location.lat), malfunctionId)
    }

    override fun findWithin(skip: Long, centroid: LatLon, radius: Double, count: Long): List<Long> {
        val result = template.opsForGeo().geoRadius(
                Constant.RedisNames.LOCATION_CACHE_NAME,
                Circle(Point(centroid.lon, centroid.lat), Distance(radius, RedisGeoCommands.DistanceUnit.KILOMETERS)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().sortAscending()
        )!!.content

        return result
                .stream()
                .skip(skip)
                .limit(count)
                .map { it.content.name }
                .collect(Collectors.toList())
    }

    override fun deleteOne(malfunctionId: Long) {
        template.opsForGeo().geoRemove(Constant.RedisNames.LOCATION_CACHE_NAME, malfunctionId)
    }

    private fun fillWithData() {
        runBlocking {
            fileLog.d("=== Loading damage claims' ids and locations in the specialistProfileStore ===")
            var totalLoaded = 0L

            val time = measureTimeMillis {
                val mapOfItems = mutableMapOf<Long, Point>()

                val damageClaimsList = damageClaimDao.databaseRequest(hikariCP.connection) { connection ->
                    damageClaimDao.findAll(true, connection)
                }!!

                if (damageClaimsList.isNotEmpty()) {
                    totalLoaded += damageClaimsList.size

                    for (damageClaim in damageClaimsList) {
                        mapOfItems.put(damageClaim.id, Point(damageClaim.lon, damageClaim.lat))
                    }

                    if (mapOfItems.isNotEmpty()) {
                        template.opsForGeo().geoAdd(Constant.RedisNames.LOCATION_CACHE_NAME, mapOfItems)
                    }
                }
            }

            fileLog.d("=== Done in $time ms, totalLoaded = $totalLoaded ===")
        }
    }
}