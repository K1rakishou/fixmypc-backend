package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

class ProfilePhotoCacheImpl : ProfilePhotoCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var profilePhotoCache: IgniteCache<Long, ProfilePhoto>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, ProfilePhoto>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.PROFILE_PHOTO_CACHE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        profilePhotoCache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(profilePhoto: ProfilePhoto) {
        profilePhotoCache.put(profilePhoto.userId, profilePhoto)
    }

    override fun findOne(userId: Long): Fickle<ProfilePhoto> {
        return Fickle.of(profilePhotoCache[userId])
    }
}