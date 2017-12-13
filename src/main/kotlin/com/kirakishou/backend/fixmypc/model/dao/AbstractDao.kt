package com.kirakishou.backend.fixmypc.model.dao

import java.sql.Connection

abstract class AbstractDao : Dao {

    override suspend fun <T> transactionalDatabaseRequest(connection: Connection, body: suspend (connection: Connection) -> T?): T? {
        return connection.use {
            connection.autoCommit = false
            return@use body(it)
        }
    }

    override suspend fun <T> databaseRequest(connection: Connection, body: suspend (connection: Connection) -> T?): T? {
        return connection.use {
            return@use body(it)
        }
    }
}