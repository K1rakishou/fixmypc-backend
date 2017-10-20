package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ClientProfileStoreImpl : ClientProfileStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    private val tableName = "ClientProfile"
    lateinit var clientProfileStore: IgniteCache<Long, ClientProfile>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, ClientProfile>(Constant.IgniteNames.CLIENT_PROFILE_STORE)
        cacheConfig.backups = 1
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setIndexedTypes(Long::class.java, ClientProfile::class.java)

        clientProfileStore = ignite.getOrCreateCache(cacheConfig)
    }

    override fun saveOne(clientProfile: ClientProfile): Boolean {
        try {
            clientProfileStore.put(clientProfile.userId, clientProfile)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun findOne(userId: Long): Fickle<ClientProfile> {
        return Fickle.of(clientProfileStore[userId])
    }

    override fun update(userId: Long, name: String, phone: String) {
        val lock = clientProfileStore.lock(userId)
        lock.lock()

        try {
            val profile = clientProfileStore[userId]
            if (profile == null) {
                log.e("No profile was found with user id $userId")
                return
            }

            profile.name = name
            profile.phone = phone

            clientProfileStore.put(userId, profile)
        } finally {
            lock.unlock()
        }
    }

    override fun deleteOne(userId: Long): Boolean {
        try {
            clientProfileStore.remove(userId)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }
}