package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.TestUtils
import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.repository.ignite.MalfunctionStore
import com.kirakishou.backend.fixmypc.model.repository.ignite.LocationStore
import com.kirakishou.backend.fixmypc.model.repository.postgresql.MalfunctionDao
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.sql.SQLException

class MalfunctionRepositoryImplTest {

    @InjectMocks
    val repository = MalfunctionRepositoryImpl()

    @Mock
    lateinit var malfunctionStore: MalfunctionStore

    @Mock
    lateinit var malfunctionDao: MalfunctionDao

    @Mock
    lateinit var locationStore: LocationStore

    @Mock
    lateinit var userMalfunctionsRepository: UserMalfunctionsRepository

    @Mock
    lateinit var log: FileLog

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testSaveOne_ShouldSaveMalfunctionIntoDbAndCache() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(Either.Value(true))
        Mockito.`when`(userMalfunctionsRepository.saveOne(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true)

        val result = repository.saveOne(Malfunction())

        assertEquals(true, result)
    }

    @Test
    fun testSaveOne_ShouldNotSaveMalfunctionIfDbThrewError() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(Either.Error(SQLException()))

        val result = repository.saveOne(Malfunction())

        assertEquals(false, result)
    }

    @Test
    fun testSaveOne_ShouldRemoveFromDbIfCouldNotSaveToCache() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(Either.Value(true))
        Mockito.`when`(userMalfunctionsRepository.saveOne(Mockito.anyLong(), Mockito.anyLong())).thenReturn(false)

        val result = repository.saveOne(Malfunction())

        assertEquals(false, result)
        Mockito.verify(malfunctionDao, Mockito.times(1)).deleteOnePermanently(Mockito.anyLong())
    }

    @Test
    fun testFindOne_ShouldReturnMalfunctionIfItIsInCache() {
        Mockito.`when`(malfunctionStore.findOne(Mockito.anyLong())).thenReturn(Fickle.of(Malfunction(1L)))

        val result = repository.findOne(1)

        assertEquals(true, result.isPresent())
        assertEquals(1L, result.get().id)
    }

    @Test
    fun testFindOne_ShouldReturnMalfunctionFromDbIfItIsNotInCache() {
        Mockito.`when`(malfunctionStore.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(malfunctionDao.findOne(Mockito.anyLong())).thenReturn(Either.Value(Fickle.of(Malfunction(1))))

        val result = repository.findOne(1)

        assertEquals(true, result.isPresent())
        assertEquals(1L, result.get().id)
    }

    @Test
    fun testFindOne_ShouldReturnEmptyIfMalfunctionIsNotInCacheOrDb() {
        Mockito.`when`(malfunctionStore.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(malfunctionDao.findOne(Mockito.anyLong())).thenReturn(Either.Value(Fickle.empty()))

        val result = repository.findOne(1)

        assertEquals(false, result.isPresent())
    }

    @Test
    fun testFindOne_ShouldReturnEmptyIfDbThrewError() {
        Mockito.`when`(malfunctionStore.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(malfunctionDao.findOne(Mockito.anyLong())).thenReturn(Either.Error(SQLException()))

        val result = repository.findOne(1)

        assertEquals(false, result.isPresent())
    }

    @Test
    fun testFindMany_ShouldReturnEmptyIfUserHasNotAddedAnyMalfunctions() {
        Mockito.`when`(userMalfunctionsRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(emptyList())

        val result = repository.findMany(0, 0, 5)

        assertEquals(0, result.size)
    }

    @Test
    fun testFindMany_ShouldReturnMalfunctionListIfMalfunctionStoreReturnedEnoughEntries() {
        val malfunctionIdList = listOf<Long>(0, 1, 2, 3, 4)
        val malfunctionList = listOf(Malfunction(), Malfunction(), Malfunction(), Malfunction(), Malfunction())
        val count = 5L

        Mockito.`when`(userMalfunctionsRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(malfunctionIdList)
        Mockito.`when`(malfunctionStore.findMany(malfunctionIdList)).thenReturn(malfunctionList)

        val result = repository.findMany(0, 0, count)

        assertEquals(count, result.size.toLong())
    }

    @Test
    fun testFindMany_shouldReturnOnePage() {
        val malfunctionIdList = listOf<Long>(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
        val allUserMalfunctions = listOf(Malfunction(0), Malfunction(1), Malfunction(2), Malfunction(3), Malfunction(4),
                Malfunction(5), Malfunction(6), Malfunction(7), Malfunction(8), Malfunction(9),
                Malfunction(10), Malfunction(11), Malfunction(12), Malfunction(13), Malfunction(14))

        val firstPageIds = malfunctionIdList.subList(0, 5)
        val secondPageIds = malfunctionIdList.subList(5, 10)
        val thirdPageIds = malfunctionIdList.subList(10, 15)

        val firstPageMalfunctionsFromCache = listOf(Malfunction(0), Malfunction(1), Malfunction(2))
        val secondPageMalfunctionsFromCache = listOf(Malfunction(5), Malfunction(6))
        val thirdPageMalfunctionsFromCache = listOf<Malfunction>()

        Mockito.`when`(userMalfunctionsRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(firstPageIds)
                .thenReturn(secondPageIds)
                .thenReturn(thirdPageIds)

        Mockito.`when`(malfunctionStore.findMany(firstPageIds)).thenReturn(firstPageMalfunctionsFromCache)
        Mockito.`when`(malfunctionStore.findMany(secondPageIds)).thenReturn(secondPageMalfunctionsFromCache)
        Mockito.`when`(malfunctionStore.findMany(thirdPageIds)).thenReturn(thirdPageMalfunctionsFromCache)
        Mockito.`when`(malfunctionDao.findManyActive(0)).thenReturn(Either.Value(allUserMalfunctions))

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
        val allUserMalfunctions = listOf(Malfunction(0), Malfunction(1), Malfunction(2), Malfunction(3), Malfunction(4),
                Malfunction(5), Malfunction(6), Malfunction(7), Malfunction(8), Malfunction(9),
                Malfunction(10), Malfunction(11), Malfunction(12), Malfunction(13), Malfunction(14))

        val firstPageIds = malfunctionIdList.subList(0, 5)
        val secondPageIds = malfunctionIdList.subList(5, 10)
        val thirdPageIds = malfunctionIdList.subList(10, 15)

        val firstPageMalfunctionsFromCache = listOf(Malfunction(1), Malfunction(3), Malfunction(4)) //0..4
        val secondPageMalfunctionsFromCache = listOf(Malfunction(6), Malfunction(8))                //5..10
        val thirdPageMalfunctionsFromCache = listOf(Malfunction(12), Malfunction(14))               //10..15

        Mockito.`when`(userMalfunctionsRepository.findMany(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(firstPageIds)
                .thenReturn(secondPageIds)
                .thenReturn(thirdPageIds)

        Mockito.`when`(malfunctionStore.findMany(firstPageIds)).thenReturn(firstPageMalfunctionsFromCache)
        Mockito.`when`(malfunctionStore.findMany(secondPageIds)).thenReturn(secondPageMalfunctionsFromCache)
        Mockito.`when`(malfunctionStore.findMany(thirdPageIds)).thenReturn(thirdPageMalfunctionsFromCache)
        Mockito.`when`(malfunctionDao.findManyActive(0)).thenReturn(Either.Value(allUserMalfunctions))

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
































