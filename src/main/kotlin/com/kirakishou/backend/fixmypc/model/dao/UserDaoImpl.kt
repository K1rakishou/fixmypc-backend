package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.exception.DatabaseUnknownException
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.async
import java.sql.Connection
import java.sql.Statement

/**
 * Created by kirakishou on 7/17/2017.
 */

class UserDaoImpl(
        val databaseThreadPool: ThreadPoolDispatcher,
        val fileLog: FileLog
) : UserDao, AbstractDao() {

    private val TABLE_NAME = " public.users"

    override suspend fun saveOne(user: User, connection: Connection): Pair<Boolean, Long> {
        return async(databaseThreadPool) {
            var userId = Fickle.empty<Long>()

            try {
                connection.prepareStatement("INSERT INTO $TABLE_NAME (login, password, account_type, created_on, deleted_on) " +
                        "VALUES (?, ?, ?, NOW(), NULL)", Statement.RETURN_GENERATED_KEYS).use { ps ->

                    ps.setString(1, user.login)
                    ps.setString(2, user.password)
                    ps.setInt(3, user.accountType.value)
                    ps.executeUpdate()

                    ps.generatedKeys.use { rs ->
                        if (rs.next()) {
                            userId = Fickle.of(rs.getLong("id"))
                        }
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            if (!userId.isPresent()) {
                return@async false to 0L
            }

            return@async true to userId.get()
        }.await()
    }

    override suspend fun findOne(login: String, connection: Connection): Fickle<User> {
        var user: User? = null

        try {
            connection.prepareStatementScrollable("SELECT * FROM $TABLE_NAME WHERE login = ? AND deleted_on IS NULL LIMIT 1").use { ps ->
                ps.setString(1, login)

                ps.executeQuery().use { rs ->
                    if (rs.first()) {
                        user = User(
                                rs.getLong("id"),
                                rs.getString("login"),
                                rs.getString("password"),
                                AccountType.from(rs.getInt("account_type")),
                                rs.getTimestamp("created_on").time)
                    }
                }
            }
        } catch (error: Throwable) {
            fileLog.e(error)
            throw DatabaseUnknownException()
        }

        return Fickle.of(user)
    }

    //for tests only!!!
    override suspend fun deleteOne(login: String, connection: Connection): Boolean {
        try {
            connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE login = ?").use { ps ->
                ps.setString(1, login)
                ps.execute()
            }
        } catch (error: Throwable) {
            fileLog.e(error)
            throw DatabaseUnknownException()
        }

        return true
    }
}