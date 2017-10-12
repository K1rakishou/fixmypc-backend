package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class SpecialistProfileCacheImpl : SpecialistProfileCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var cache: IgniteCache<Long, SpecialistProfile>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, SpecialistProfile>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.SPECIALIST_PROFILE_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setIndexedTypes(Long::class.java, SpecialistProfile::class.java)
        //cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        cache = ignite.createCache(cacheConfig)
    }

    override fun saveOne(specialistProfile: SpecialistProfile) {
        cache.put(specialistProfile.userId, specialistProfile)
    }

    override fun saveMany(specialistProfileList: List<SpecialistProfile>) {
        val map = hashMapOf<Long, SpecialistProfile>()

        for (assignedSpecialist in specialistProfileList) {
            map.put(assignedSpecialist.userId, assignedSpecialist)
        }

        cache.putAll(map)
    }

    override fun findOne(userId: Long): Fickle<SpecialistProfile> {
        return Fickle.of(cache.get(userId))
    }

    override fun findMany(userIdList: List<Long>): List<SpecialistProfile> {
        return cache.getAll(userIdList.toSet()).values.stream()
                .filter { it != null }
                .collect(Collectors.toList())
    }

    override fun updateInfo(userId: Long, name: String, phone: String) {
        val lock = cache.lock(userId)
        lock.lock()

        try {
            val profile = cache.get(userId) ?: return
            profile.phone = phone
            profile.name = name
            cache.put(userId, profile)
        } finally {
            lock.unlock()
        }
    }

    override fun updatePhoto(userId: Long, photoName: String) {
        val lock = cache.lock(userId)
        lock.lock()

        try {
            val profile = cache.get(userId) ?: return
            profile.photoName = photoName
            cache.put(userId, profile)
        } finally {
            lock.unlock()
        }
    }
}