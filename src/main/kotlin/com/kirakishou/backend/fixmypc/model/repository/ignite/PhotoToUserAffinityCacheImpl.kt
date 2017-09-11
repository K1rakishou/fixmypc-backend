package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.dto.PhotoInfoDTO
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

@Component
class PhotoToUserAffinityCacheImpl : PhotoToUserAffinityCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var photoInfoCache: IgniteCache<String, PhotoInfoDTO>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<String, PhotoInfoDTO>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.PHOTO_TO_USER_AFFINITY_CACHE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        photoInfoCache = ignite.createCache(cacheConfig)
    }

    override fun getOne(imageName: String): Fickle<PhotoInfoDTO> {
        return Fickle.of(photoInfoCache.get(imageName))
    }

    override fun saveOne(imageName: String, info: PhotoInfoDTO) {
        photoInfoCache.put(imageName, info)
    }
}