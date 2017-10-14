package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.LatLon
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
class LocationStoreImpl : LocationStore {

    @Autowired
    lateinit var template: RedisTemplate<String, Long>

    @Autowired
    lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    lateinit var log: FileLog

    @PostConstruct
    fun init() {
        warmUpCache()
    }

    override fun saveOne(location: LatLon, malfunctionId: Long) {
        template.opsForGeo().geoAdd(Constant.RedisNames.LOCATION_CACHE_NAME, Point(location.lon, location.lat), malfunctionId)
    }

    override fun findWithin(skip: Long, centroid: LatLon, radius: Double, count: Long): List<Long> {
        val result =  template.opsForGeo().geoRadius(
                Constant.RedisNames.LOCATION_CACHE_NAME,
                Circle(Point(centroid.lon, centroid.lat), Distance(radius, RedisGeoCommands.DistanceUnit.KILOMETERS)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().sortAscending()
        ).content

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

    private fun warmUpCache() {
        log.d("=== Loading damage claims' ids and locations in the specialistProfileStore ===")
        var totalLoaded = 0L

        val time = measureTimeMillis {
            val mapOfItems = mutableMapOf<Long, Point>()

            val damageClaimsList = damageClaimStore.findAll(true)
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

        log.d("=== Done in $time ms, totalLoaded = $totalLoaded ===")
    }
}