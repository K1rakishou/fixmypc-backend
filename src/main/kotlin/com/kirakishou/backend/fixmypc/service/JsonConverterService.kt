package com.kirakishou.backend.fixmypc.service

import com.google.gson.Gson
import com.kirakishou.backend.fixmypc.util.Utils
import org.springframework.core.io.buffer.DataBuffer

class JsonConverterService(
        val gson: Gson
) {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> fromJson(dataBufferList: List<DataBuffer>): T {
        return gson.fromJson(Utils.dataBufferToString(dataBufferList), T::class.java) as T
    }

    fun toJson(data: Any): String {
        return gson.toJson(data)
    }
}