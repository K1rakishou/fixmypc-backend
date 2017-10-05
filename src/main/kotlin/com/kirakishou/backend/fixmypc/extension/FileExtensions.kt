package com.kirakishou.backend.fixmypc.extension

import java.io.File

public fun File.deleteOnExitScope(func: (file: File) -> Unit) {
    try {
        func(this)
    } finally {
        this.delete()
    }
}