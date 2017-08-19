package com.kirakishou.backend.fixmypc.extension

import com.kirakishou.backend.fixmypc.log.FileLog
import java.sql.Connection

fun Connection.transactional(log: FileLog, body: (connection: Connection) -> Unit) {
    try {
        this.autoCommit = false
        body.invoke(this)
        this.commit()

    } catch (e: Exception) {
        log.e(e)
        this.rollback()

    } finally {
        this.autoCommit = true
        this.close()
    }
}