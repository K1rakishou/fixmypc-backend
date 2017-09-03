package com.kirakishou.backend.fixmypc.model.entity

data class FileServerInfo(val host: String,
                          var isWorking: Boolean = true,
                          var timeOfDeath: Long = 0L)