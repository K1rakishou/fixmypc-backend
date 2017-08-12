package com.kirakishou.backend.fixmypc.manager

import com.kirakishou.backend.fixmypc.extension.lockAndRead
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.util.ServerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class FileServersManagerImpl : FileServersManager {

    @Autowired
    lateinit var log: FileLog

    private val lock = ReentrantReadWriteLock()
    private var fileServerInfoList = emptyList<FileServerInfo>()
    private var serverPingInterval = AtomicLong(0)
    private val serverId = AtomicInteger(0)

    override fun init(servers: List<FileServerInfo>, serverPingInterval: Long) {
        this.fileServerInfoList = servers
        this.serverPingInterval.set(TimeUnit.MINUTES.toMillis(serverPingInterval))
    }

    override fun isServerOk(id: Int): Boolean {
        val fileServerInfo = lock.lockAndRead {
            fileServerInfoList[id]
        }

        //every N minutes mark the fileServerInfo as working again to see if it's got back
        if (fileServerInfo.timeOfDeath - ServerUtils.getTimeFast() > serverPingInterval.get()) {
            log.d("Restoring fileServer isWorking status to true")
            fileServerInfo.isWorking = true
        }

        if (!fileServerInfo.isDiskSpaceOk) {
            log.d("The fileserver has no disk space")
            return false
        }

        return true
    }

    override fun getServers(count: Int): List<ServerWithId> {
        val serversCount = lock.lockAndRead {
            fileServerInfoList.size
        }

        val servers = arrayListOf<ServerWithId>()
        val goodServers = fileServerInfoList.filter { it.isWorking && it.isDiskSpaceOk }

        for (i in 0 until count) {
            val id = serverId.getAndIncrement() % goodServers.size
            servers.add(ServerWithId(id, goodServers[id]))
        }

        return servers
    }

    override fun getServer(): Fickle<ServerWithId> {
        val serversCount = lock.lockAndRead {
            fileServerInfoList.size
        }

        for (i in 0 until serversCount) {
            val id = serverId.getAndIncrement() % serversCount

            if (isServerOk(id)) {
                val serverInfo = lock.lockAndRead {
                    fileServerInfoList[id]
                }

                return Fickle.of(ServerWithId(id, serverInfo))
            }
        }

        log.d("No working fileservers were found")
        return Fickle.empty()
    }

    override fun getAliveServersCount(): Int {
        var count = 0
        val serversCount = lock.lockAndRead {
            fileServerInfoList.size
        }

        for (i in 0 until serversCount) {
            if (isServerOk(i)) {
                ++count
            }
        }

        return count
    }

    override fun isAtLeastOneServerAlive(): Boolean {
        val serversCount = lock.lockAndRead {
            fileServerInfoList.size
        }

        for (i in 0 until serversCount) {
            if (isServerOk(i)) {
                return true
            }
        }

        return false
    }

    override fun notWorking(i: Int) {
        lock.lockAndRead {
            fileServerInfoList[i].isWorking = false
            fileServerInfoList[i].timeOfDeath = ServerUtils.getTimeFast()
        }
    }

    override fun noDiskSpace(i: Int) {
        lock.lockAndRead {
            fileServerInfoList[i].isDiskSpaceOk = false
            fileServerInfoList[i].timeOfDeath = ServerUtils.getTimeFast()
        }
    }

    data class ServerWithId(val id: Int,
                            var fileServerInfo: FileServerInfo)
}