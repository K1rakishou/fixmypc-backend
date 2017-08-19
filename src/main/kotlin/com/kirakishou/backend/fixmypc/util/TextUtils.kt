package com.kirakishou.backend.fixmypc.util

import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.ExtractedImageInfo

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

    fun checkPasswordLenCorrect(password: String): Boolean {
        if (password.length < Constant.MIN_PASSWORD_LENGTH) {
            return false
        } else if (password.length > Constant.MAX_PASSWORD_LENGTH) {
            return false
        }

        return true
    }

    fun parseImageName(imageName: String): ExtractedImageInfo {
        val strings = imageName.split("_")
        val serverIdStr = strings[0].removeRange(0..0)
        val name = strings[1].removeRange(0..0)

        return ExtractedImageInfo(serverIdStr.toInt(), name)
    }
}