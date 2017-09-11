package com.kirakishou.backend.fixmypc.model.repository.ignite

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.SortedSet
import java.util.TreeSet
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration
import kotlin.collections.ArrayList

@Component
class UserToDamageClaimKeyAffinityCacheImpl : UserToDamageClaimKeyAffinityCache {

    @Autowired
    lateinit var ignite: Ignite

    lateinit var userMalfunctionStore: IgniteCache<Long, SortedSet<Long>>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, SortedSet<Long>>()
        cacheConfig.backups = 0
        cacheConfig.name = Constant.IgniteNames.USER_MALFUNCTION_CACHE_NAME
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.TEN_MINUTES, Duration.TEN_MINUTES, Duration.TEN_MINUTES))

        userMalfunctionStore = ignite.createCache(cacheConfig)
    }

    override fun saveOne(ownerId: Long, malfunctionId: Long) {
        val lock = userMalfunctionStore.lock(ownerId)
        lock.lock()

        try {
            val userMalfunctions = get(ownerId)
            userMalfunctions.add(malfunctionId)
            userMalfunctionStore.put(ownerId, userMalfunctions)
        } finally {
            lock.unlock()
        }
    }

    override fun saveMany(ownerId: Long, malfunctionIdList: List<Long>) {
        val lock = userMalfunctionStore.lock(ownerId)
        lock.lock()

        try {
            val userMalfunctions = get(ownerId)
            userMalfunctions.addAll(malfunctionIdList)
            userMalfunctionStore.put(ownerId, userMalfunctions)
        } finally {
            lock.unlock()
        }
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val userAllMalfunctions = userMalfunctionStore.get(ownerId) ?: return emptyList()

        return userAllMalfunctions.stream()
                .skip(offset)
                .limit(count)
                .collect(Collectors.toList())
    }

    override fun findAll(ownerId: Long): List<Long> {
        val userAllMalfunctions = userMalfunctionStore.get(ownerId) ?: return emptyList()
        return ArrayList(userAllMalfunctions)
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long) {
        val lock = userMalfunctionStore.lock(ownerId)
        lock.lock()

        try {
            val userMalfunctions = get(ownerId)
            userMalfunctions.remove(malfunctionId)
            userMalfunctionStore.put(ownerId, userMalfunctions)
        } finally {
            lock.unlock()
        }
    }

    override fun deleteAll(ownerId: Long) {
        userMalfunctionStore.remove(ownerId)
    }

    override fun clear() {
        userMalfunctionStore.clear()
    }

    private fun get(ownerId: Long): SortedSet<Long> {
        var userMalfunctions = userMalfunctionStore.get(ownerId)
        if (userMalfunctions == null) {
            userMalfunctions = TreeSet(IdComparator())
        }
        return userMalfunctions
    }

    private class IdComparator : Comparator<Long> {
        override fun compare(id1: Long, id2: Long): Int {
            if (id1 < id2) {
                return -1
            } else if (id1 > id2) {
                return 1
            }

            return 0
        }
    }
}