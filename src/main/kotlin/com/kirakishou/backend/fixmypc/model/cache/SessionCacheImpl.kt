package com.kirakishou.backend.fixmypc.model.cache

/*@Component
class SessionCacheImpl : SessionCache {

    @Autowired
    lateinit var ignite: Ignite

    @Autowired
    lateinit var log: FileLog

    //damageClaimId, DamageClaim
    lateinit var sessionCache: IgniteCache<String, User>

    @PostConstruct
    fun init() {
        val sessionCacheConfig = CacheConfiguration<String, User>()
        sessionCacheConfig.backups = 0
        sessionCacheConfig.name = Constant.IgniteNames.SESSION_CACHE
        sessionCacheConfig.cacheMode = CacheMode.PARTITIONED
        sessionCacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.ONE_HOUR, Duration.ONE_HOUR, Duration.ONE_HOUR))

        sessionCache = ignite.getOrCreateCache(sessionCacheConfig)
    }

    override fun saveOne(sessionId: String, user: User) {
        sessionCache.put(sessionId, user)
    }

    override fun findOne(sessionId: String): Fickle<User> {
        return Fickle.of(sessionCache[sessionId])
    }
}*/