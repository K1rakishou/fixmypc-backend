package com.kirakishou.backend.fixmypc.util

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.ExtractedImageInfo

/**
 * Created by kirakishou on 7/16/2017.
 */
object TextUtils {

    fun checkLoginCorrect(login: String): Boolean {
        if (!login.contains('@')) {
            return false
        }

        if (!login.contains('.')) {
            return false
        }

        return true
    }

    fun checkLoginLenCorrect(login: String): Boolean {
        if (login.length > Constant.TextLength.MAX_LOGIN_LENGTH) {
            return false
        }

        return true
    }

    fun checkPasswordLenCorrect(password: String): Boolean {
        if (password.length < Constant.TextLength.MIN_PASSWORD_LENGTH) {
            return false
        } else if (password.length > Constant.TextLength.MAX_PASSWORD_LENGTH) {
            return false
        }

        return true
    }

    fun parseImageName(imageName: String): ExtractedImageInfo {
        try {
            val strings = imageName.split("_")
            if (strings.size != 2) {
                return ExtractedImageInfo(isNameOk = false)
            }

            val serverIdStr = strings[0].removeRange(0..0)
            val serverId = try {
                serverIdStr.toInt()
            } catch (e: NumberFormatException) {
                return ExtractedImageInfo(isNameOk = false)
            }

            val name = strings[1].removeRange(0..0)
            if (name.isEmpty()) {
                return ExtractedImageInfo(isNameOk = false)
            }

            return ExtractedImageInfo(serverId, name, true)
        } catch (e: Throwable) {
            return ExtractedImageInfo(isNameOk = false)
        }
    }

    fun createStatementForList(length: Int): String {
        val sb = StringBuilder()

        for (i in 0 until length) {
            sb.append("?")

            if (i < length - 1) {
                sb.append(", ")
            }
        }

        return sb.toString()
    }

    fun extractExtension(inputStr: String): String {
        val sb = StringBuilder()
        val strLen = inputStr.length - 1

        for (index in (strLen downTo 0)) {
            if (inputStr[index] == '.') {
                break
            }

            sb.insert(0, inputStr[index])
        }

        return sb.toString()
    }
}