package com.kirakishou.backend.fixmypc.model.repository.redis

import com.kirakishou.backend.fixmypc.model.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class LocationStoreImpl : LocationStore {

    @Autowired
    private lateinit var template: RedisTemplate<String, Long>


    override fun addLocation(lat: Double, lon: Double, malfunctionId: Long) {
        template.opsForGeo().geoAdd(Constant.RedisNames.LOCATION_STORE_NAME, Point(lon, lat), malfunctionId)
    }

    override fun removeLocation(malfunctionId: Long) {
        template.opsForGeo().geoRemove(Constant.RedisNames.LOCATION_STORE_NAME, malfunctionId)
    }
}