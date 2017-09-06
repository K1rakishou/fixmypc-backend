package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.TestUtils
import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.repository.ignite.DamageClaimCache
import com.kirakishou.backend.fixmypc.model.repository.ignite.LocationCache
import com.kirakishou.backend.fixmypc.model.repository.postgresql.DamageClaimDao
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.sql.SQLException

class DamageClaimRepositoryImplTest {

    @InjectMocks
    val repository = DamageClaimRepositoryImpl()

    @Mock
    lateinit var damageClaimCache: DamageClaimCache

    @Mock
    lateinit var damageClaimDao: DamageClaimDao

    @Mock
    lateinit var locationCache: LocationCache

    @Mock
    lateinit var userToDamageClaimKeyAffinityRepository: UserToDamageClaimKeyAffinityRepository

    @Mock
    lateinit var log: FileLog

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testSaveOne_ShouldSaveMalfunctionIntoDbAndCache() {
        Mockito.`when`(damageClaimDao.saveOne(TestUtils.anyObject())).thenReturn(Either.Value(true))
        Mockito.`when`(userToDamageClaimKeyAffinityRepository.saveOne(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true)

        val result = repository.saveOne(DamageClaim())

        assertEquals(true, result)
    }

    @Test
    fun testSaveOne_ShouldNotSaveMalfunctionIfDbThrewError() {
        Mockito.`when`(damageClaimDao.saveOne(TestUtils.anyObject())).thenReturn(Either.Error(SQLException()))

        val result = repository.saveOne(DamageClaim())

        assertEquals(false, result)
    }

    @Test
    fun testSaveOne_ShouldRemoveFromDbIfCouldNotSaveToCache() {
        Mockito.`when`(damageClaimDao.saveOne(TestUtils.anyObject())).thenReturn(Either.Value(true))
        Mockito.`when`(userToDamageClaimKeyAffinityRepository.saveOne(Mockito.anyLong(), Mockito.anyLong())).thenReturn(false)

        val result = repository.saveOne(DamageClaim())

        assertEquals(false, result)
        Mockito.verify(damageClaimDao, Mockito.times(1)).deleteOnePermanently(Mockito.anyLong())
    }

    @Test
    fun testFindOne_ShouldReturnMalfunctionIfItIsInCache() {
        Mockito.`when`(damageClaimCache.findOne(Mockito.anyLong())).thenReturn(Fickle.of(DamageClaim(1L)))

        val result = repository.findOne(1)

        assertEquals(true, result.isPresent())
        assertEquals(1L, result.get().id)
    }

    @Test
    fun testFindOne_ShouldReturnMalfunctionFromDbIfItIsNotInCache() {
        Mockito.`when`(damageClaimCache.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(damageClaimDao.findOne(Mockito.anyLong())).thenReturn(Either.Value(Fickle.of(DamageClaim(1))))

        val result = repository.findOne(1)

        assertEquals(true, result.isPresent())
        assertEquals(1L, result.get().id)
    }

    @Test
    fun testFindOne_ShouldReturnEmptyIfMalfunctionIsNotInCacheOrDb() {
        Mockito.`when`(damageClaimCache.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(damageClaimDao.findOne(Mockito.anyLong())).thenReturn(Either.Value(Fickle.empty()))

        val result = repository.findOne(1)

        assertEquals(false, result.isPresent())
    }

    @Test
    fun testFindOne_ShouldReturnEmptyIfDbThrewError() {
        Mockito.`when`(damageClaimCache.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(damageClaimDao.findOne(Mockito.anyLong())).thenReturn(Either.Error(SQLException()))

        val result = repository.findOne(1)

        assertEquals(false, result.isPresent())
    }

    @Test
    fun testFindMany_ShouldReturnEmptyIfUserHasNotAddedAnyMalfunctions() {
        Mockito.`when`(userToDamageClaimKeyAffinityRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(emptyList())

        val result = repository.findMany(0, 0, 5)

        assertEquals(0, result.size)
    }

    @Test
    fun testFindMany_ShouldReturnMalfunctionListIfMalfunctionStoreReturnedEnoughEntries() {
        val malfunctionIdList = listOf<Long>(0, 1, 2, 3, 4)
        val malfunctionList = listOf(DamageClaim(), DamageClaim(), DamageClaim(), DamageClaim(), DamageClaim())
        val count = 5L

        Mockito.`when`(userToDamageClaimKeyAffinityRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(malfunctionIdList)
        Mockito.`when`(damageClaimCache.findMany(malfunctionIdList)).thenReturn(malfunctionList)

        val result = repository.findMany(0, 0, count)

        assertEquals(count, result.size.toLong())
    }

    @Test
    fun testFindMany_shouldReturnOnePage() {
        val malfunctionIdList = listOf<Long>(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
        val allUserMalfunctions = listOf(DamageClaim(0), DamageClaim(1), DamageClaim(2), DamageClaim(3), DamageClaim(4),
                DamageClaim(5), DamageClaim(6), DamageClaim(7), DamageClaim(8), DamageClaim(9),
                DamageClaim(10), DamageClaim(11), DamageClaim(12), DamageClaim(13), DamageClaim(14))

        val firstPageIds = malfunctionIdList.subList(0, 5)
        val secondPageIds = malfunctionIdList.subList(5, 10)
        val thirdPageIds = malfunctionIdList.subList(10, 15)

        val firstPageMalfunctionsFromCache = listOf(DamageClaim(0), DamageClaim(1), DamageClaim(2))
        val secondPageMalfunctionsFromCache = listOf(DamageClaim(5), DamageClaim(6))
        val thirdPageMalfunctionsFromCache = listOf<DamageClaim>()

        Mockito.`when`(userToDamageClaimKeyAffinityRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(firstPageIds)
                .thenReturn(secondPageIds)
                .thenReturn(thirdPageIds)

        Mockito.`when`(damageClaimCache.findMany(firstPageIds)).thenReturn(firstPageMalfunctionsFromCache)
        Mockito.`when`(damageClaimCache.findMany(secondPageIds)).thenReturn(secondPageMalfunctionsFromCache)
        Mockito.`when`(damageClaimCache.findMany(thirdPageIds)).thenReturn(thirdPageMalfunctionsFromCache)
        Mockito.`when`(damageClaimDao.findManyActiveByOwnerId(0)).thenReturn(Either.Value(allUserMalfunctions))

        val result = repository.findMany(0, 0, 5)
        assertEquals(5, result.size)
        assertEquals(0, result[0].id)
        assertEquals(4, result[4].id)

        val result2 = repository.findMany(0, 5, 5)
        assertEquals(5, result2.size)
        assertEquals(5, result2[0].id)
        assertEquals(9, result2[4].id)

        val result3 = repository.findMany(0, 10, 5)
        assertEquals(5, result3.size)
        assertEquals(10, result3[0].id)
        assertEquals(14, result3[4].id)
    }

    @Test
    fun testFindMany_WithIdsCachedAtRandom() {
        val malfunctionIdList = listOf<Long>(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
        val allUserMalfunctions = listOf(DamageClaim(0), DamageClaim(1), DamageClaim(2), DamageClaim(3), DamageClaim(4),
                DamageClaim(5), DamageClaim(6), DamageClaim(7), DamageClaim(8), DamageClaim(9),
                DamageClaim(10), DamageClaim(11), DamageClaim(12), DamageClaim(13), DamageClaim(14))

        val firstPageIds = malfunctionIdList.subList(0, 5)
        val secondPageIds = malfunctionIdList.subList(5, 10)
        val thirdPageIds = malfunctionIdList.subList(10, 15)

        val firstPageMalfunctionsFromCache = listOf(DamageClaim(1), DamageClaim(3), DamageClaim(4)) //0..4
        val secondPageMalfunctionsFromCache = listOf(DamageClaim(6), DamageClaim(8))                //5..10
        val thirdPageMalfunctionsFromCache = listOf(DamageClaim(12), DamageClaim(14))               //10..15

        Mockito.`when`(userToDamageClaimKeyAffinityRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(firstPageIds)
                .thenReturn(secondPageIds)
                .thenReturn(thirdPageIds)

        Mockito.`when`(damageClaimCache.findMany(firstPageIds)).thenReturn(firstPageMalfunctionsFromCache)
        Mockito.`when`(damageClaimCache.findMany(secondPageIds)).thenReturn(secondPageMalfunctionsFromCache)
        Mockito.`when`(damageClaimCache.findMany(thirdPageIds)).thenReturn(thirdPageMalfunctionsFromCache)
        Mockito.`when`(damageClaimDao.findManyActiveByOwnerId(0)).thenReturn(Either.Value(allUserMalfunctions))

        val result = repository.findMany(0, 0, 5)
        assertEquals(5, result.size)
        assertEquals(0, result[0].id)
        assertEquals(4, result[4].id)

        val result2 = repository.findMany(0, 5, 5)
        assertEquals(5, result2.size)
        assertEquals(5, result2[0].id)
        assertEquals(9, result2[4].id)

        val result3 = repository.findMany(0, 10, 5)
        assertEquals(5, result3.size)
        assertEquals(10, result3[0].id)
        assertEquals(14, result3[4].id)
    }
}
































