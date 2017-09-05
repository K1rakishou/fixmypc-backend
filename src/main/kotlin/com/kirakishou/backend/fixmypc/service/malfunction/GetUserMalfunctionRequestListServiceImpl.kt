package com.kirakishou.backend.fixmypc.service.malfunction

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.MyExpiryPolicyFactory
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.repository.MalfunctionRepository
import com.kirakishou.backend.fixmypc.model.repository.ignite.MalfunctionCache
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserMalfunctionsCache
import com.kirakishou.backend.fixmypc.model.repository.ignite.UserCache
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import io.reactivex.Single
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.query.SqlQuery
import org.apache.ignite.cache.query.annotations.QuerySqlField
import org.apache.ignite.configuration.CacheConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.cache.expiry.Duration

@Component
class GetUserMalfunctionRequestListServiceImpl : GetUserMalfunctionRequestListService {

    @Autowired
    private lateinit var userCache: UserCache

    @Autowired
    private lateinit var malfunctionRepository: MalfunctionRepository

    @Autowired
    lateinit var malfunctionCache: MalfunctionCache

    @Autowired
    lateinit var userMalfunctionsCache: UserMalfunctionsCache

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    lateinit var ignite: Ignite

    lateinit var testStore: IgniteCache<Long, Entry>

    @PostConstruct
    fun init() {
        val cacheConfig = CacheConfiguration<Long, Entry>()
        cacheConfig.backups = 0
        cacheConfig.name = "test"
        cacheConfig.cacheMode = CacheMode.PARTITIONED
        cacheConfig.atomicityMode = CacheAtomicityMode.TRANSACTIONAL
        cacheConfig.setExpiryPolicyFactory(MyExpiryPolicyFactory(Duration.FIVE_MINUTES, Duration.FIVE_MINUTES, Duration.FIVE_MINUTES))
        cacheConfig.setIndexedTypes(Long::class.java, Entry::class.java)

        testStore = ignite.createCache(cacheConfig)
    }

    override fun getUserMalfunctionRequestList(sessionId: String, offset: Long): Single<GetUserMalfunctionRequestListService.Get.Result> {
        test()

        //user must re login if sessionId was removed from the cache
        val userFickle = userCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("sessionId $sessionId was not found in the cache")
            return Single.just(GetUserMalfunctionRequestListService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        val malfunctionList = malfunctionRepository.findMany(user.id, offset, Constant.MAX_MALFUNCTIONS_PER_PAGE)

        return Single.just(GetUserMalfunctionRequestListService.Get.Result.Ok(malfunctionList))
    }

    private fun test() {
        val rnd = Random()
        val r = WKTReader()

        for (i in 0L..999L) {
            val x = rnd.nextInt(10000)
            val y = rnd.nextInt(10000)
            val geo = r.read("POINT($x $y)")

            testStore.put(i, Entry(geo))
        }

        val q = SqlQuery<Int, Entry>(Entry::class.java, "coords && ?")

        for (i in 0..9) {
            val cond = r.read("POLYGON((0 0, 0 " + rnd.nextInt(10000) + ", " +
                    rnd.nextInt(10000) + " " + rnd.nextInt(10000) + ", " +
                    rnd.nextInt(10000) + " 0, 0 0))")

            val entries = testStore.query(q.setArgs(cond)).all
            println("Fetched points [cond=" + cond + ", cnt=" + entries.size + ']')
        }

    }

    data class Entry(@QuerySqlField(index = true)
                     private val coords: Geometry)
}