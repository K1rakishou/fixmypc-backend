package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.model.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Statement
import javax.sql.DataSource

/**
 * Created by kirakishou on 7/17/2017.
 */

@Component
class UserDaoImpl : UserDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    private val TABLE_NAME = " public.users"

    override fun saveOne(user: User): Either<Throwable, Pair<Boolean, Long>> {
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
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        if (!userId.isPresent()) {
            return Either.Value(false to 0L)
        }

        return Either.Value(true to userId.get())
    }

    override fun findOne(login: String): Either<Throwable, Fickle<User>> {
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
                                    rs.getTimestamp("created_on"))
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(Fickle.of(user))
    }

    //for tests only!!!
    override fun deleteOne(login: String): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE login = ?").use { ps ->
                    ps.setString(1, login)
                    ps.execute()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }
}