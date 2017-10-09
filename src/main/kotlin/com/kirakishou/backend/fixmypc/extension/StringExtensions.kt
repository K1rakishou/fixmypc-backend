package com.kirakishou.backend.fixmypc.extension

@Throws(IllegalStateException::class)
fun String.getFileExtension(): String {
    val sb = StringBuilder()
    val strLen = this.length - 1
    var dotFound = false

    for (index in (strLen downTo 0)) {
        if (this[index] == '.') {
            dotFound = true
            break
        }

        sb.insert(0, this[index])
    }

    if (!dotFound) {
        return ""
    }

    return sb.toString()
}

fun String.limit(maxLen: Int): String {
    return this.substring(0, Math.min(this.length, maxLen))
}