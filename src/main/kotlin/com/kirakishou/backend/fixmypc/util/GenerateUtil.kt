package com.kirakishou.backend.fixmypc.util

import java.security.SecureRandom

/**
 * Created by kirakishou on 7/11/2017.
 */
object GenerateUtil {
    val alphabetWithSpecialSymbols = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_!@#$%^&*()"
    val alphabetWithoutSpecialSymbols = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val random = SecureRandom()

    fun generateRandomString(len: Int, alphabet: String): String {
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
        return generateRandomString(16, alphabetWithSpecialSymbols)
    }

    fun generateImageName(): String {
        return generateRandomString(64, alphabetWithoutSpecialSymbols)
    }
}