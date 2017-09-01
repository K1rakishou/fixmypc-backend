package com.kirakishou.backend.fixmypc.model.repository

import com.kirakishou.backend.fixmypc.TestUtils
import com.kirakishou.backend.fixmypc.log.FileLog
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
    fun testShouldSaveMalfunctionIntoDbAndCache() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(MalfunctionDao.Result.Saved())
        Mockito.`when`(userMalfunctionsRepository.saveOne(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true)

        val result = repository.saveOne(Malfunction())

        assertEquals(true, result)
    }

    @Test
    fun testShouldNotSaveMalfunctionIfDbThrewError() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(MalfunctionDao.Result.DbError(SQLException()))

        val result = repository.saveOne(Malfunction())

        assertEquals(false, result)
    }

    @Test
    fun testShouldRemoveFromDbIfCouldnotSaveToCache() {
        Mockito.`when`(malfunctionDao.saveOne(TestUtils.anyObject())).thenReturn(MalfunctionDao.Result.Saved())
        Mockito.`when`(userMalfunctionsRepository.saveOne(Mockito.anyLong(), Mockito.anyLong())).thenReturn(false)

        val result = repository.saveOne(Malfunction())

        assertEquals(false, result)
        Mockito.verify(malfunctionDao, Mockito.times(1)).deleteOnePermanently(Mockito.anyLong())
    }
}

































