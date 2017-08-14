package com.kirakishou.backend.fixmypc.manager

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FileServersManagerTest {

    @InjectMocks
    val manager = FileServersManagerImpl()

    @Mock
    lateinit var log: FileLog

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun shouldReturnFourServers() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        val servers = manager.getServers(4)
        assertEquals(4, servers.size)
    }

    @Test
    fun shouldReturnNoServersIfThereAreNoWorking() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", false, true))
        manager.init(fileServerInfoList, -1L)

        val servers = manager.getServers(4)
        assertEquals(0, servers.size)
    }

    @Test
    fun shouldReturnNoServersIfTheyAllRunOutOfDiskSpace() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, false))
        manager.init(fileServerInfoList, -1L)

        val servers = manager.getServers(4)
        assertEquals(0, servers.size)
    }

    @Test
    fun shouldReturnServerWithHost127001() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        val server = manager.getServer()
        assertEquals(true, server.isPresent())
        assertEquals("127.0.0.1", server.get().fileServerInfo.host)
    }

    @Test
    fun shouldReturnServerWithHost127004() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        val server = manager.getServer()
        assertEquals(true, server.isPresent())
        assertEquals("127.0.0.4", server.get().fileServerInfo.host)
    }

    @Test
    fun shouldReturnNoServer() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, false))
        manager.init(fileServerInfoList, -1L)

        val server = manager.getServer()
        assertEquals(false, server.isPresent())
    }

    @Test
    fun shouldReturnFourActiveServersCount() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        val count = manager.getAliveServersCount()
        assertEquals(4, count)
    }

    @Test
    fun shouldReturnZeroActiveServersCount() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, false))
        manager.init(fileServerInfoList, -1L)

        val count = manager.getAliveServersCount()
        assertEquals(0, count)
    }

    @Test
    fun shouldReturnTrueIfOneServerIsAliveOrHasDiskSpace() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        val isOneAlive = manager.isAtLeastOneServerAlive()
        assertEquals(true, isOneAlive)
    }

    @Test
    fun shouldReturnFalseIfNoServersAreAliveOrHaveDiskSpace() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", false, true))
        manager.init(fileServerInfoList, -1L)

        val isOneAlive = manager.isAtLeastOneServerAlive()
        assertEquals(false, isOneAlive)
    }

    @Test
    fun shouldMarkServerAsNotWorking() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        manager.notWorking(0)

        val servers = manager.getServers(3)
        assertEquals(servers.size, 3)
        assertEquals("127.0.0.2", servers[0].fileServerInfo.host)
        assertEquals("127.0.0.3", servers[1].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[2].fileServerInfo.host)
    }

    @Test
    fun shouldMarkServerAsNotRunOutOfDiskSpace() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        manager.noDiskSpace(0)

        val servers = manager.getServers(3)
        assertEquals(servers.size, 3)
        assertEquals("127.0.0.2", servers[0].fileServerInfo.host)
        assertEquals("127.0.0.3", servers[1].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[2].fileServerInfo.host)
    }

    @Test
    fun shouldReturnSixServersDistributedRoundRobinWayWhenTwoAreDown() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        val servers = manager.getServers(6)
        assertEquals(6, servers.size)

        assertEquals("127.0.0.3", servers[0].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[1].fileServerInfo.host)
        assertEquals("127.0.0.3", servers[2].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[3].fileServerInfo.host)
        assertEquals("127.0.0.3", servers[4].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[5].fileServerInfo.host)
    }

    @Test
    fun shouldReturnFiveEntriesOfTheSameServer() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, false))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", false, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, -1L)

        val servers = manager.getServers(5)
        assertEquals(5, servers.size)

        assertEquals("127.0.0.4", servers[0].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[1].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[2].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[3].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[4].fileServerInfo.host)
    }

    @Test
    fun shouldMarkServerAsWorkingAfter200Milliseconds() {
        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        manager.init(fileServerInfoList, 100)
        manager.notWorking(0)

        var servers = manager.getServers(4)
        assertEquals(4, servers.size)

        assertEquals("127.0.0.2", servers[0].fileServerInfo.host)
        assertEquals("127.0.0.3", servers[1].fileServerInfo.host)
        assertEquals("127.0.0.4", servers[2].fileServerInfo.host)
        assertEquals("127.0.0.2", servers[3].fileServerInfo.host)

        Thread.sleep(200)

        servers = manager.getServers(4)
        assertEquals(4, servers.size)

        var found = false

        for (server in servers) {
            if (server.fileServerInfo.host == "127.0.0.1") {
                found = true
            }
        }

        assertEquals(true, found)
    }
}

















