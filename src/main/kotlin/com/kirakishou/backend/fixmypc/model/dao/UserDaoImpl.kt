package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.User
import com.kirakishou.backend.fixmypc.model.exception.DatabaseException
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.async
import java.sql.Statement
import javax.sql.DataSource

/**
 * Created by kirakishou on 7/17/2017.
 */

class UserDaoImpl(
        val hikariCP: DataSource,
        val databaseThreadPool: ThreadPoolDispatcher,
        val fileLog: FileLog
) : UserDao {

    private val TABLE_NAME = " public.users"

    override suspend fun saveOne(user: User): Pair<Boolean, Long> {
        return async(databaseThreadPool) {
            var userId = Fickle.empty<Long>()

            try {
                hikariCP.connection.use { connection ->
                    connection.prepareStatement("INSERT INTO $TABLE_NAME (login, password, account_type, created_on, deleted_on) " +
                            "VALUES (?, ?, ?, NOW(), NULL)", Statement.RETURN_GENERATED_KEYS).use { ps ->

                        ps.setString(1, user.login)
                        ps.setString(2, user.password)
                        ps.setInt(3, user.accountType.value)
                        ps.executeUpdate()

                        ps.generatedKeys.use {
                            if (it.next()) {
                                userId = Fickle.of(it.getLong(1))
                            }
                        }
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseException()
            }

            if (!userId.isPresent()) {
                return@async false to 0L
            }

            return@async true to userId.get()
        }.await()
    }

    override suspend fun findOne(login: String): Fickle<User> {
        var user: User? = null

        try {
            hikariCP.connection.use { connection ->
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
            }
        } catch (error: Throwable) {
            fileLog.e(error)
            throw DatabaseException()
        }

        return Fickle.of(user)
    }

    //for tests only!!!
    override suspend fun deleteOne(login: String): Boolean {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE login = ?").use { ps ->
                    ps.setString(1, login)
                    ps.execute()
                }
            }
        } catch (error: Throwable) {
            fileLog.e(error)
            throw DatabaseException()
        }

        return true
    }
}