package com.kirakishou.backend.fixmypc.model

data class FileServerInfo(val host: String,
                          var isDiskSpaceOk: Boolean = true,
                          var isWorking: Boolean = true,
                          var timeOfDeath: Long = 0L)