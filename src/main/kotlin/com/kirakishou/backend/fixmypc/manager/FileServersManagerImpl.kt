package com.kirakishou.backend.fixmypc.manager

import com.kirakishou.backend.fixmypc.extension.lockRead
import com.kirakishou.backend.fixmypc.extension.lockReadReturn
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.util.ServerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class FileServersManagerImpl : FileServersManager {

    @Autowired
    private lateinit var log: FileLog

    private val lock = ReentrantReadWriteLock()
    private var fileServerInfoList = emptyList<FileServerInfo>()
    private var serverPingInterval = AtomicLong(0)
    private val serverId = AtomicInteger(0)

    override fun init(servers: List<FileServerInfo>, serverPingIntervalMsecs: Long) {
        this.fileServerInfoList = servers
        this.serverPingInterval.set(serverPingIntervalMsecs)
    }

    //TL Note: daijoubu means all right
    private fun daijoubu(id: Int): Boolean {
        val fileServerInfo = lock.lockReadReturn {
            fileServerInfoList[id]
        }

        //every N minutes mark the fileServerInfo as working again to see if it's got back
        val timeInterval = serverPingInterval.get()

        //if timeInterval == -1L -> check is disabled, we don't need to check further
        //if timeOfDeath == 0L -> server has never been marked as dead, we don't need to check further
        //else -> check how much time passed since the server was marked as dead.
        //If it's more that timeInterval - mark server as alive so we can try to find out if it's been restarted
        if (timeInterval != -1L && (fileServerInfo.timeOfDeath != 0L && ServerUtils.getTimeFast() - fileServerInfo.timeOfDeath > timeInterval)) {
            log.d("Restoring fileServer isWorking status to true")
            fileServerInfo.isWorking = true
        } else {
            if (!fileServerInfo.isWorking) {
                return false
            }
        }

        return true
    }

    override fun getServers(count: Int): List<ServerWithId> {
        val servers = arrayListOf<ServerWithId>()
        val goodServers = arrayListOf<Int>()

        val serversCount = lock.lockReadReturn {
            fileServerInfoList.size
        }

        for (i in 0 until serversCount) {
            if (daijoubu(i)) {
                goodServers.add(i)
            }
        }

        if (goodServers.isEmpty()) {
            return emptyList()
        }

        for (i in 0 until count) {
            val goodServerIndex = serverId.getAndIncrement() % goodServers.size
            val id = goodServers[goodServerIndex]

            val serverInfo = lock.lockReadReturn {
                fileServerInfoList[id]
            }

            servers.add(ServerWithId(id, serverInfo))
        }

        return servers
    }

    override fun getServer(): Fickle<ServerWithId> {
        val serversCount = lock.lockReadReturn {
            fileServerInfoList.size
        }

        for (i in 0 until serversCount) {
            val id = serverId.getAndIncrement() % serversCount

            if (daijoubu(id)) {
                val serverInfo = lock.lockReadReturn {
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
        val serversCount = lock.lockReadReturn {
            fileServerInfoList.size
        }

        for (i in 0 until serversCount) {
            if (daijoubu(i)) {
                ++count
            }
        }

        return count
    }

    override fun isAtLeastOneServerAlive(): Boolean {
        val serversCount = lock.lockReadReturn {
            fileServerInfoList.size
        }

        for (i in 0 until serversCount) {
            if (daijoubu(i)) {
                return true
            }
        }

        return false
    }

    override fun notWorking(id: Int) {
        if (id < 0 || id > fileServerInfoList.size) {
            throw RuntimeException("getHostById dad id")
        }

        lock.lockRead {
            fileServerInfoList[id].isWorking = false
            fileServerInfoList[id].timeOfDeath = ServerUtils.getTimeFast()
        }
    }

    override fun getHostById(id: Int): String {
        if (id < 0 || id > fileServerInfoList.size) {
            throw RuntimeException("getHostById dad id")
        }

        return lock.lockReadReturn {
            fileServerInfoList[id].host
        }
    }

    data class ServerWithId(val id: Int,
                            var fileServerInfo: FileServerInfo)
}