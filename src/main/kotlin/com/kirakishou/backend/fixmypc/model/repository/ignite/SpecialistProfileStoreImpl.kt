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
class SpecialistProfileStoreImpl : SpecialistProfileStore {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var specialistProfileStore: IgniteCache<Long, SpecialistProfile>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, SpecialistProfile>()
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.SPECIALIST_PROFILE_CACHE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setIndexedTypes(Long::class.java, SpecialistProfile::class.java)
        //cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        specialistProfileStore = ignite.createCache(cacheConfig)
    }

    override fun saveOne(specialistProfile: SpecialistProfile) {
        specialistProfileStore.put(specialistProfile.userId, specialistProfile)
    }

    override fun saveMany(specialistProfileList: List<SpecialistProfile>) {
        val map = hashMapOf<Long, SpecialistProfile>()

        for (assignedSpecialist in specialistProfileList) {
            map.put(assignedSpecialist.userId, assignedSpecialist)
        }

        specialistProfileStore.putAll(map)
    }

    override fun findOne(userId: Long): Fickle<SpecialistProfile> {
        return Fickle.of(specialistProfileStore.get(userId))
    }

    override fun findMany(userIdList: List<Long>): List<SpecialistProfile> {
        return specialistProfileStore.getAll(userIdList.toSet()).values.stream()
                .filter { it != null }
                .collect(Collectors.toList())
    }

    override fun updateInfo(userId: Long, name: String, phone: String) {
        val lock = specialistProfileStore.lock(userId)
        lock.lock()

        try {
            val profile = specialistProfileStore.get(userId) ?: return
            profile.phone = phone
            profile.name = name
            specialistProfileStore.put(userId, profile)
        } finally {
            lock.unlock()
        }
    }

    override fun updatePhoto(userId: Long, photoName: String) {
        val lock = specialistProfileStore.lock(userId)
        lock.lock()

        try {
            val profile = specialistProfileStore.get(userId) ?: return
            profile.photoName = photoName
            specialistProfileStore.put(userId, profile)
        } finally {
            lock.unlock()
        }
    }
}