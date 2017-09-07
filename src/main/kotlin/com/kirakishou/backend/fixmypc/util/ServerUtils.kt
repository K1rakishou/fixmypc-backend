package com.kirakishou.backend.fixmypc.util

import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object ServerUtils {
    private val MD5 = "MD5"

    fun md5(data: ByteArray): ByteArray {
        val md5Instance: MessageDigest = try {
            MessageDigest.getInstance(MD5)
        } catch (e: NoSuchAlgorithmException) {
            throw e
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

    fun getTimeFast(): Long {
        return System.currentTimeMillis()
    }

    fun purgeDirectory(dir: File) {
        for (file in dir.listFiles()!!) {
            if (file.isDirectory) {
                purgeDirectory(file)
            }

            file.delete()
        }
    }
}