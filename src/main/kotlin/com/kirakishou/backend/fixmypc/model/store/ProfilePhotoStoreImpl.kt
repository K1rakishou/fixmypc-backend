package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ProfilePhotoStoreImpl : ProfilePhotoStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    lateinit var profilePhotoStore: IgniteCache<Long, ProfilePhoto>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, ProfilePhoto>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.PROFILE_PHOTO_STORE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setIndexedTypes(Long::class.java, ProfilePhoto::class.java)
        //cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        profilePhotoStore = ignite.getOrCreateCache(cacheConfig)
    }

    override fun saveOne(profilePhoto: ProfilePhoto): Boolean {
        try {
            profilePhotoStore.put(profilePhoto.userId, profilePhoto)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun findOne(userId: Long): Fickle<ProfilePhoto> {
        return Fickle.of(profilePhotoStore[userId])
    }
}