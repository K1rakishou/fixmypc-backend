package com.kirakishou.backend.fixmypc

import org.mockito.Mockito

object TestUtils {

    fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }
}