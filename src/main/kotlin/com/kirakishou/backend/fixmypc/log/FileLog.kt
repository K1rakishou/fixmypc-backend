package com.kirakishou.backend.fixmypc.log

interface FileLog {
    fun e(message: String)
    fun e(exception: Throwable)
    fun d(message: String)
    fun w(message: String)
}