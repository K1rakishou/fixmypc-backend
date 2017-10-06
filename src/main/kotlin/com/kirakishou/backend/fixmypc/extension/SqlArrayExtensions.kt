package com.kirakishou.backend.fixmypc.extension

import java.sql.Array
import kotlin.Array as KArray

fun <T> Array.toList(): MutableList<T> {
    val array = this.array
    val outList = mutableListOf<T>()

    for (obj in array as KArray<T>) {
        try {
            outList.add(obj)
        } catch (e: ClassCastException) {
            println("Object is not a T")
            e.printStackTrace()
        }
    }

    return outList
}