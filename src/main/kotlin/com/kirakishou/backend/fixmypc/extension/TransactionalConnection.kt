package com.kirakishou.backend.fixmypc.extension

import java.sql.Connection

fun Connection.transactional(body: (connection: Connection) -> Unit) {
    try {
        this.autoCommit = false
        body.invoke(this)
        this.commit()

    } catch (e: Exception) {
        this.rollback()
        throw e

    } finally {
        this.autoCommit = true
        this.close()
    }
}