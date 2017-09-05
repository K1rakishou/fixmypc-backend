package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.repository.postgresql.MalfunctionDao
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.annotations.QuerySqlField
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.system.measureTimeMillis


@Component
class LocationCacheImpl : LocationCache {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var malfunctionDao: MalfunctionDao

    @Autowired
    lateinit var log: FileLog

    lateinit var locationCache: IgniteCache<Long, MapLocation>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, MapLocation>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.LOCATION_STORE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED

        locationCache = ignite.createCache(cacheConfig)
        preload()
    }

    fun preload() {
        var totalLoaded = 0L
        log.d("=== Loading damage claims' ids and locations in the cache ===")

        val time = measureTimeMillis {
            locationCache.clear()

            var offset = 0L
            val count = 1000L

            val transaction = ignite.transactions().txStart()

            try {
                while (true) {
                    val itemsFromDb = malfunctionDao.findAllIdsWithLocations(offset, count)
                    totalLoaded += itemsFromDb.size

                    if (itemsFromDb.isEmpty()) {
                        break
                    }

                    val mapOfItems = mutableMapOf<Long, MapLocation>()
                    val geomFactory = GeometryFactory()

                    for ((id, location) in itemsFromDb) {
                        val geometry = geomFactory.createPoint(Coordinate(location.lon, location.lat))
                        mapOfItems.put(id, MapLocation(geometry))
                    }

                    locationCache.putAll(mapOfItems)
                    offset += count
                }

                transaction.commit()
            } catch (e: Throwable) {
                log.e(e)
                transaction.rollback()
            }

        }

        log.d("Done in $time ms, totalLoaded = $totalLoaded")
    }

    override fun saveOne(location: LatLon, malfunctionId: Long) {
        val geomFactory = GeometryFactory()
        val geometry = geomFactory.createPoint(Coordinate(location.lon, location.lat))
        locationCache.put(malfunctionId, MapLocation(geometry))
    }

    override fun deleteOne(malfunctionId: Long) {
        locationCache.remove(malfunctionId)
    }

    data class MapLocation(@QuerySqlField(index = true) val geometry: Geometry)
}