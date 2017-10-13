package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.User
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteAtomicSequence
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.AtomicConfiguration
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


/**
 * Created by kirakishou on 7/11/2017.
 */

@Component
class UserStoreImpl : UserStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    private val cacheName = Constant.IgniteNames.USER_STORE
    lateinit var userStore: IgniteCache<String, User>
    lateinit var userIdGenerator: IgniteAtomicSequence

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<String, User>(cacheName)
        cacheConfig.backups = 1
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.setIndexedTypes(String::class.java, User::class.java)
        userStore = ignite.getOrCreateCache(cacheConfig)

        val atomicConfig = AtomicConfiguration()
        atomicConfig.backups = 3
        atomicConfig.cacheMode = CacheMode.PARTITIONED
        userIdGenerator = ignite.atomicSequence(Constant.IgniteNames.USER_ID_GENERATOR, atomicConfig, 0L, true)
    }

    override fun saveOne(login: String, user: User): Long {
        user.id = userIdGenerator.andIncrement

        try {
            userStore.put(login, user)
            return user.id
        } catch (e: Throwable) {
            log.e(e)
            return -1L
        }
    }

    override fun findOne(login: String): Fickle<User> {
        return Fickle.of(userStore[login])
    }

    override fun deleteOne(login: String): Boolean {
        try {
            userStore.remove(login)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }
}