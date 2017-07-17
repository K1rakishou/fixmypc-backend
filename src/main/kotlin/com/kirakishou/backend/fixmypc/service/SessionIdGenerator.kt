package com.kirakishou.backend.fixmypc.service

/**
 * Created by kirakishou on 7/11/2017.
 */
interface SessionIdGenerator {
    fun generateSessionId(): String
}