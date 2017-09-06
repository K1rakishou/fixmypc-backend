package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdsSortedByDistanceDTO
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.repository.postgresql.DamageClaimDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import kotlin.system.measureTimeMillis

@Component
class LocationCacheImpl : LocationCache {

    @Autowired
    lateinit var template: RedisTemplate<String, Long>

    @Autowired
    lateinit var damageClaimDao: DamageClaimDao

    @Autowired
    lateinit var log: FileLog

    @PostConstruct
    fun init() {
        preload()
    }

    override fun saveOne(location: LatLon, malfunctionId: Long) {
        template.opsForGeo().geoAdd(Constant.RedisNames.LOCATION_CACHE_NAME, Point(location.lon, location.lat), malfunctionId)
    }

    override fun findWithin(page: Long, centroid: LatLon, radius: Double, count: Long): List<DamageClaimIdsSortedByDistanceDTO> {
        return template.opsForGeo().geoRadius(Constant.RedisNames.LOCATION_CACHE_NAME,
                Circle(Point(centroid.lon, centroid.lat), Distance(radius, RedisGeoCommands.DistanceUnit.KILOMETERS)),
                RedisGeoCommands.GeoRadiusCommandArgs
                        .newGeoRadiusArgs()
                        .includeDistance()
                        .sortAscending()).content
                .stream()
                .skip(page * count)
                .limit(count)
                .map { DamageClaimIdsSortedByDistanceDTO(it.content.name, it.distance.value, it.distance.metric.abbreviation) }
                .collect(Collectors.toList())
    }

    override fun deleteOne(malfunctionId: Long) {
        template.opsForGeo().geoRemove(Constant.RedisNames.LOCATION_CACHE_NAME, malfunctionId)
    }

    private fun preload() {
        var totalLoaded = 0L
        log.d("=== Loading damage claims' ids and locations in the cache ===")

        val time = measureTimeMillis {
            var offset = 0L
            val count = 1000L

            try {
                val mapOfItems = mutableMapOf<Long, Point>()

                while (true) {
                    val itemsFromDb = damageClaimDao.findAllIdsWithLocations(offset, count)
                    totalLoaded += itemsFromDb.size

                    if (itemsFromDb.isEmpty()) {
                        break
                    }

                    for ((id, location) in itemsFromDb) {
                        mapOfItems.put(id, Point(location.lon, location.lat))
                    }

                    offset += count
                }

                template.opsForGeo().geoAdd(Constant.RedisNames.LOCATION_CACHE_NAME, mapOfItems)
            } catch (e: Throwable) {
                log.e(e)
            }

        }

        log.d("=== Done in $time ms, totalLoaded = $totalLoaded ===")
    }
}