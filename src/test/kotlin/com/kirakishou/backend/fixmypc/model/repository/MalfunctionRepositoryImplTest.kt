package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.TestUtils
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.model.repository.hazelcast.MalfunctionStore
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
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(MalfunctionDao.Result.Saved())
        Mockito.`when`(userMalfunctionsRepository.saveOne(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true)

        val result = repository.saveOne(Malfunction())

        assertEquals(true, result)
    }

    @Test
    fun testSaveOne_ShouldNotSaveMalfunctionIfDbThrewError() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(MalfunctionDao.Result.DbError(SQLException()))

        val result = repository.saveOne(Malfunction())

        assertEquals(false, result)
    }

    @Test
    fun testSaveOne_ShouldRemoveFromDbIfCouldNotSaveToCache() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(MalfunctionDao.Result.Saved())
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
        Mockito.`when`(malfunctionDao.findOne(Mockito.anyLong())).thenReturn(MalfunctionDao.Result.FoundOne(Malfunction(1)))

        val result = repository.findOne(1)

        assertEquals(true, result.isPresent())
        assertEquals(1L, result.get().id)
    }

    @Test
    fun testFindOne_ShouldReturnEmptyIfMalfunctionIsNotInCacheOrDb() {
        Mockito.`when`(malfunctionStore.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(malfunctionDao.findOne(Mockito.anyLong())).thenReturn(MalfunctionDao.Result.NotFound())

        val result = repository.findOne(1)

        assertEquals(false, result.isPresent())
    }

    @Test
    fun testFindOne_ShouldReturnEmptyIfDbThrewError() {
        Mockito.`when`(malfunctionStore.findOne(Mockito.anyLong())).thenReturn(Fickle.empty())
        Mockito.`when`(malfunctionDao.findOne(Mockito.anyLong())).thenReturn(MalfunctionDao.Result.DbError(SQLException()))

        val result = repository.findOne(1)

        assertEquals(false, result.isPresent())
    }


}

































