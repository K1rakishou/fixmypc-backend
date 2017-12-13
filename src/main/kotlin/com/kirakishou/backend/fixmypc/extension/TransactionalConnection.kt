package com.kirakishou.backend.fixmypc.extension

import java.sql.Connection

suspend fun Connection.transactionalUse(body: suspend (connection: Connection) -> Unit) {
    try {
        this.autoCommit = false
        body.invoke(this)
        this.commit()

    } catch (e: Throwable) {
        this.rollback()
        throw e

    } finally {
        this.close()
    }
}