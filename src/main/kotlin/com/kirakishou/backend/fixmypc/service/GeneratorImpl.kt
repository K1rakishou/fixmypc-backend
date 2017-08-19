package com.kirakishou.backend.fixmypc.service

import org.springframework.stereotype.Component
import java.security.SecureRandom

/**
 * Created by kirakishou on 7/11/2017.
 */

@Component
class GeneratorImpl : Generator {
    private val numericAlphabeticSpecialSymbols = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_!@#$%^&*()"
    private val numericAlphabetic = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val random = SecureRandom()

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

    override fun generateSessionId(): String {
        return generateRandomString(64, numericAlphabeticSpecialSymbols)
    }

    override fun generateImageName(): String {
        return generateRandomString(64, numericAlphabetic)
    }

    override fun generateMalfunctionRequestId(): String {
        return generateRandomString(64, numericAlphabetic)
    }
}