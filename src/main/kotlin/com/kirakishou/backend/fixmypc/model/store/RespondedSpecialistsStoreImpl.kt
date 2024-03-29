package com.kirakishou.backend.fixmypc.model.store

/*
@Component
class RespondedSpecialistsStoreImpl : RespondedSpecialistsStore {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    private val tableName = "RespondedSpecialist"

    //key is RespondedSpecialistId
    lateinit var respondedSpecialistsCache: IgniteCache<Long, RespondedSpecialist>
    lateinit var respondedSpecialistIdGenerator: IgniteAtomicSequence

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, RespondedSpecialist>(Constant.IgniteNames.RESPONDED_SPECIALISTS_STORE)
        cacheConfig.backups = 1
        cacheConfig.name = Constant.IgniteNames.RESPONDED_SPECIALISTS_STORE
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setIndexedTypes(Long::class.java, RespondedSpecialist::class.java)
        respondedSpecialistsCache = ignite.getOrCreateCache(cacheConfig)

        val atomicConfig = AtomicConfiguration()
        atomicConfig.backups = 3
        atomicConfig.cacheMode = CacheMode.PARTITIONED
        respondedSpecialistIdGenerator = ignite.atomicSequence(Constant.IgniteNames.RESPONDED_SPECIALIST_ID_GENERATOR, atomicConfig, 0L, true)
    }

    override fun saveOne(respondedSpecialist: RespondedSpecialist): Boolean {
        try {
            respondedSpecialist.id = respondedSpecialistIdGenerator.andIncrement
            respondedSpecialistsCache.put(respondedSpecialist.id, respondedSpecialist)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun containsOne(damageClaimId: Long, userId: Long): Boolean {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? AND user_id = ?"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId, userId)

        val result = respondedSpecialistsCache.query(sqlQuery).use {
            it.all.firstOrNull {
                it.value.userId == userId
            }
        }

        return result != null
    }

    override fun findOne(damageClaimId: Long, userId: Long): Fickle<RespondedSpecialist> {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? AND user_id = ?"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId)

        val respondedSpecialist = respondedSpecialistsCache.query(sqlQuery).use { it.all }
        if (respondedSpecialist.isEmpty()) {
            return Fickle.empty()
        }

        return Fickle.of(respondedSpecialist.map { it.value }.first())
    }

    override fun findMany(damageClaimIdList: List<Long>): List<RespondedSpecialist> {
        val statement = TextUtils.createStatementForList(damageClaimIdList.size)
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id IN ($statement)"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(*damageClaimIdList.toTypedArray())

        return respondedSpecialistsCache.query(sqlQuery).use { query -> query.all.map { it.value } }
    }

    override fun findManyForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): List<RespondedSpecialist> {
        val sql = "SELECT * FROM $tableName WHERE damage_claim_id = ? OFFSET ? LIMIT ?"
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId, skip, count)

        return respondedSpecialistsCache.query(sqlQuery).use { entries -> entries.all.map { it.value } }
    }

    override fun updateSetViewed(damageClaimId: Long, userId: Long): Boolean {
        try {
            val sql = "UPDATE $tableName SET was_viewed = true WHERE damage_claim_id = ? AND user_id = ?"
            val sqlQuery = SqlFieldsQuery(sql).setArgs(damageClaimId, userId)

            respondedSpecialistsCache.query(sqlQuery)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun findAll(): List<RespondedSpecialist> {
        val sql = "SELECT * FROM $tableName "
        val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql)

        return respondedSpecialistsCache.query(sqlQuery).use { entries -> entries.all.map { it.value } }
    }

    override fun deleteAllForDamageClaim(damageClaimId: Long): Boolean {
        try {
            val sql = "DELETE FROM $tableName WHERE damage_claim_id = ?"
            val sqlQuery = SqlQuery<Long, RespondedSpecialist>(RespondedSpecialist::class.java, sql).setArgs(damageClaimId)

            respondedSpecialistsCache.query(sqlQuery)
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }
}





























*/







