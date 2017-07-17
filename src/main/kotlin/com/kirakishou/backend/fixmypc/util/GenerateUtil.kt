package com.kirakishou.backend.fixmypc.util

import java.util.*

/**
 * Created by kirakishou on 7/11/2017.
 */
object GenerateUtil {
    val alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_!@#$%^&*()"
    val random = Random(Date().time)

    fun generateRandomString(len: Int): String {
        val bytes = ByteArray(len)
        random.nextBytes(bytes)

        val sb = StringBuilder()
        val alphabetLen = alphabet.length

        for (i in 0 until len) {
            sb.append(alphabet[Math.abs(bytes[i] % alphabetLen)])
        }

        return sb.toString()
    }

    fun generateSessionId(): String {
        return generateRandomString(16)
    }
}