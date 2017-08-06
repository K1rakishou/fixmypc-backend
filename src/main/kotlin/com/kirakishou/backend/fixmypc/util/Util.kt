package com.kirakishou.backend.fixmypc.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Util {
    private val MD5 = "MD5"

    fun md5(data: ByteArray): ByteArray {
        var md5Instance: MessageDigest? = null

        try {
            md5Instance = MessageDigest.getInstance(MD5)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException(e)
        }

        if (md5Instance == null) {
            throw NullPointerException("Digest cannot be null!")
        }

        return md5Instance.digest(data)
    }

    fun toHexString(bytes: ByteArray): String {
        val hexString = StringBuilder()

        for (i in bytes.indices) {
            val hex = Integer.toHexString(0xFF and bytes[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }

            hexString.append(hex)
        }

        return hexString.toString()
    }
}