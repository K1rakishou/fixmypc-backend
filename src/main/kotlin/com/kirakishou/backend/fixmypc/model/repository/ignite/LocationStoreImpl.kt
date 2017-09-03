package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.annotations.QuerySqlField
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Component
class LocationStoreImpl : LocationStore {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var locationCache: IgniteCache<Long, MapLocation>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, MapLocation>()
        cacheConfig.backups = 2
        cacheConfig.name = Constant.IgniteNames.LOCATION_STORE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED

        locationCache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(location: LatLon, malfunctionId: Long) {
        val reader = WKTReader()
        val geometry = reader.read("POINT(${location.lon} ${location.lat})")
        locationCache.put(malfunctionId, MapLocation(geometry))
    }

    override fun deleteOne(malfunctionId: Long) {
        locationCache.remove(malfunctionId)
    }

    data class MapLocation(@QuerySqlField(index = true) val geometry: Geometry)
}