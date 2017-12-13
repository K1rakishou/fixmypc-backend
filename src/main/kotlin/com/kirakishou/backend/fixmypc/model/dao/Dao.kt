package com.kirakishou.backend.fixmypc.model.dao

import java.sql.Connection

interface Dao {
    suspend fun <T> transactionalDatabaseRequest(connection: Connection, body: suspend (connection: Connection) -> T?): T?
    suspend fun <T> databaseRequest(connection: Connection, body: suspend (connection: Connection) -> T?): T?
}