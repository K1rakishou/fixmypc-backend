package com.kirakishou.backend.fixmypc.util

import com.kirakishou.backend.fixmypc.model.Constants

/**
 * Created by kirakishou on 7/16/2017.
 */
object TextUtil {

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
        if (password.length < Constants.MIN_PASSWORD_LENGTH) {
            return false
        } else if (password.length > Constants.MAX_PASSWORD_LENGTH) {
            return false
        }

        return true
    }
}