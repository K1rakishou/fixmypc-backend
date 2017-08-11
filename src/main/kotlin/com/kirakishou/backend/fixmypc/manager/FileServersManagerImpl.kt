package com.kirakishou.backend.fixmypc.manager

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.util.Util
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Component
class FileServersManagerImpl : FileServersManager {
    private var fileServerInfoList = emptyList<FileServerInfo>()
    private var serverPingInterval: Long = 0L
    private val serverId = AtomicInteger(0)

    override fun init(servers: List<FileServerInfo>, serverPingInterval: Long) {
        this.fileServerInfoList = servers
        this.serverPingInterval = TimeUnit.SECONDS.toMillis(serverPingInterval)
    }

    override fun isServerOk(id: Int): Boolean {
        val fileServerInfo = fileServerInfoList[id]
        if (!fileServerInfo.isDiskSpaceOk) {
            return false
        }

        //every N minutes mark the server as working again to see if it's got back
        if (fileServerInfo.timeOfDeath - Util.getTimeFast() > serverPingInterval) {
            fileServerInfo.isWorking = true
        }

        return true
    }

    override fun getWorkingServerOrNothing(): Fickle<FileServerInfo> {
        val serversCount = fileServerInfoList.size

        for (i in 0 until serversCount) {
            val id = serverId.getAndIncrement() % serversCount

            if (isServerOk(id)) {
                val serverInfo = fileServerInfoList[i]
                serverInfo.serverId = id

                return Fickle.of(serverInfo)
            }
        }

        return Fickle.empty()
    }

    override fun getAliveServersCount(): Int {
        var count = 0
        val serversCount = fileServerInfoList.size

        for (i in 0 until serversCount) {
            if (isServerOk(i)) {
                ++count
            }
        }

        return count
    }

    override fun isAtLeastOneServerAlive(): Boolean {
        val serversCount = fileServerInfoList.size

        for (i in 0 until serversCount) {
            if (isServerOk(i)) {
                return true
            }
        }

        return false
    }

    override fun at(i: Int): FileServerInfo {
        return fileServerInfoList[i]
    }
}