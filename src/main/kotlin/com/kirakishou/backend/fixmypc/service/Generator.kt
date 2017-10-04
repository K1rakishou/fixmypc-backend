package com.kirakishou.backend.fixmypc.service

/**
 * Created by kirakishou on 7/11/2017.
 */
interface Generator {
    fun generateSessionId(): String
    fun generateImageName(): String
    fun generateTempFileName(): String
    fun generateMalfunctionRequestId(): String
}