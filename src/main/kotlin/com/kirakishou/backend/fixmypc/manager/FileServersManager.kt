package com.kirakishou.backend.fixmypc.manager

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.FileServerInfo

interface FileServersManager {
    fun init(servers: List<FileServerInfo>, serverPingInterval: Long)
    fun isServerOk(id: Int): Boolean
    fun getServers(count: Int): List<FileServersManagerImpl.ServerWithId>
    fun getServer(): Fickle<FileServersManagerImpl.ServerWithId>
    fun getAliveServersCount(): Int
    fun isAtLeastOneServerAlive(): Boolean
    fun notWorking(i: Int)
    fun noDiskSpace(i: Int)
}