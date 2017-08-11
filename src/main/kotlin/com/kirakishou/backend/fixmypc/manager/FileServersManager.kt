package com.kirakishou.backend.fixmypc.manager

import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.FileServerInfo

interface FileServersManager {
    fun init(servers: List<FileServerInfo>, serverPingInterval: Long)
    fun isServerOk(id: Int): Boolean
    fun getWorkingServerOrNothing(): Fickle<FileServerInfo>
    fun getAliveServersCount(): Int
    fun isAtLeastOneServerAlive(): Boolean
    fun at(i: Int): FileServerInfo
}