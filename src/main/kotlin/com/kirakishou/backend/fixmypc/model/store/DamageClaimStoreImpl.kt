package com.kirakishou.backend.fixmypc.model.store

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteAtomicSequence
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.SqlQuery
import org.apache.ignite.configuration.AtomicConfiguration
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Component
class DamageClaimStoreImpl : DamageClaimStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    lateinit var damageClaimIdGenerator: IgniteAtomicSequence

    //damageClaimId, DamageClaim
    lateinit var damageClaimStore: IgniteCache<Long, DamageClaim>

    //userId, Set<damageClaimId>
    lateinit var damageClaimKeyStore: IgniteCache<Long, MutableSet<Long>>

    @PostConstruct
    fun init() {
        val damageClaimStoreConfig = CacheConfiguration<Long, DamageClaim>()
        damageClaimStoreConfig.backups = 1
        damageClaimStoreConfig.name = Constant.IgniteNames.DAMAGE_CLAIM_STORE
        damageClaimStoreConfig.cacheMode = CacheMode.PARTITIONED
        damageClaimStoreConfig.setIndexedTypes(Long::class.java, DamageClaim::class.java)
        damageClaimStore = ignite.getOrCreateCache(damageClaimStoreConfig)

        val damageClaimKeyStoreConfig = CacheConfiguration<Long, MutableSet<Long>>()
        damageClaimKeyStoreConfig.backups = 1
        damageClaimKeyStoreConfig.name = Constant.IgniteNames.DAMAGE_CLAIM_KEYS_STORE
        damageClaimKeyStoreConfig.cacheMode = CacheMode.PARTITIONED
        damageClaimKeyStoreConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        damageClaimKeyStoreConfig.setIndexedTypes(Long::class.java, MutableSet::class.java)
        damageClaimKeyStore = ignite.getOrCreateCache(damageClaimKeyStoreConfig)

        val atomicConfig = AtomicConfiguration()
        atomicConfig.backups = 3
        atomicConfig.cacheMode = CacheMode.PARTITIONED

        damageClaimIdGenerator = ignite.atomicSequence(Constant.IgniteNames.DAMAGE_CLAIM_GENERATOR, atomicConfig, 0L, true)
    }

    override fun saveOne(damageClaim: DamageClaim): Boolean {
        try {
            ignite.transactions().txStart().use { transaction ->
                try {
                    damageClaim.id = damageClaimIdGenerator.andIncrement
                    val userId = damageClaim.ownerId

                    val keys = get(userId)
                    keys.add(damageClaim.id)

                    damageClaimKeyStore.put(userId, keys)
                    damageClaimStore.put(damageClaim.id, damageClaim)

                    transaction.commit()
                } catch (e: Throwable) {
                    log.e(e)
                    transaction.rollback()
                }
            }

            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun saveMany(damageClaimList: List<DamageClaim>): Boolean {
        try {
            ignite.transactions().txStart().use { transaction ->
                try {
                    val damageClaimKeysMap = hashMapOf<Long, MutableSet<Long>>()
                    for (damageClaim in damageClaimList) {
                        damageClaimKeysMap.putIfAbsent(damageClaim.ownerId, mutableSetOf())
                        damageClaimKeysMap[damageClaim.ownerId]!!.add(damageClaim.id)
                    }

                    damageClaimKeyStore.putAll(damageClaimKeysMap)

                    val damageClaimMap = hashMapOf<Long, DamageClaim>()
                    for (malfunction in damageClaimList) {
                        damageClaimMap.put(malfunction.id, malfunction)
                    }

                    damageClaimStore.putAll(damageClaimMap)

                    transaction.commit()
                } catch (e: Throwable) {
                    log.e(e)
                    transaction.rollback()
                }
            }

            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun findOne(damageClaimId: Long): Fickle<DamageClaim> {
        return Fickle.of(damageClaimStore[damageClaimId])
    }

    override fun findMany(isActive: Boolean, damageClaimIdList: List<Long>): List<DamageClaim> {
        val set = damageClaimIdList.toSet()
        val clientDamageClaims = damageClaimStore.getAll(set).values
        val filtered = clientDamageClaims.filter { it.isActive == isActive }

        return ArrayList(filtered)
    }

    override fun findManyPaged(isActive: Boolean, userId: Long, offset: Long, count: Long): List<DamageClaim> {
        val keySet = damageClaimKeyStore.get(userId) ?: return emptyList()

        val searchKeys = keySet.stream()
                .skip(offset)
                .limit(count)
                .collect(Collectors.toSet())

        val foundDamageClaimMap = damageClaimStore.getAll(searchKeys) ?: return emptyList()
        return foundDamageClaimMap.values.stream()
                .filter { it.isActive != isActive }
                .collect(Collectors.toList())
    }

    override fun findAll(isActive: Boolean): List<DamageClaim> {
        val sql = "SELECT * FROM DamageClaim WHERE is_active = ?"
        val sqlQuery = SqlQuery<Long, DamageClaim>(DamageClaim::class.java, sql).setArgs(isActive)

        return damageClaimStore.query(sqlQuery)
                .all
                .map { it.value }
    }

    override fun deleteOne(damageClaim: DamageClaim): Boolean {
        try {
            ignite.transactions().txStart().use { transaction ->
                try {
                    val keySet = get(damageClaim.ownerId)
                    keySet.remove(damageClaim.id)

                    damageClaimKeyStore.put(damageClaim.ownerId, keySet)
                    damageClaimStore.remove(damageClaim.id)

                    transaction.commit()
                } catch (e: Throwable) {
                    log.e(e)
                    transaction.rollback()
                }
            }

            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun clear() {
        damageClaimStore.clear()
    }

    private fun get(userId: Long): MutableSet<Long> {
        var userDamageClaimKeys = damageClaimKeyStore.get(userId)
        if (userDamageClaimKeys == null) {
            userDamageClaimKeys = mutableSetOf()
        }

        return userDamageClaimKeys
    }
}