package com.kirakishou.backend.fixmypc.util

import org.springframework.core.io.buffer.DataBuffer

object Utils {

    fun dataBufferToString(dataBufferList: List<DataBuffer>): String {
        val fullLength = dataBufferList.sumBy { it.readableByteCount() }
        val array = ByteArray(fullLength)
        var offset = 0

        for (dataBuffer in dataBufferList) {
            val bufferSize = dataBuffer.readableByteCount()
            val dataBufferArray = ByteArray(bufferSize)

            dataBuffer.read(dataBufferArray)
            System.arraycopy(dataBufferArray, 0, array, offset, bufferSize)

            offset += bufferSize
        }

        return String(array)
    }

}