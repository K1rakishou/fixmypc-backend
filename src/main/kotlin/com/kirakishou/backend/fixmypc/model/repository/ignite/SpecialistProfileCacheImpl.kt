package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

@Component
class SpecialistProfileCacheImpl : SpecialistProfileCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var cache: IgniteCache<Long, SpecialistProfile>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, SpecialistProfile>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.SPECIALIST_PROFILE_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

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
}