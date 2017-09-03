package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.AccountType
import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.model.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Created by kirakishou on 7/17/2017.
 */

@Repository
class UserDaoImpl : UserDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    override fun saveOne(user: User): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.transactional { connection ->
                connection.prepareStatement("INSERT INTO public.users (login, password, account_type, created_on, deleted_on) " +
                        "VALUES (?, ?, ?, NOW(), NULL)").use { ps ->

                    ps.setString(1, user.login)
                    ps.setString(2, user.password)
                    ps.setInt(3, user.accountType.value)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findOne(login: String): Either<SQLException, Fickle<User>> {
        var user: User? = null

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT * FROM public.users WHERE login = ? AND deleted_on IS NULL LIMIT 1").use { ps ->
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
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(Fickle.of(user))
    }

    //for tests only!!!
    override fun deleteOne(login: String): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM public.users WHERE login = ?").use { ps ->
                    ps.setString(1, login)
                    ps.execute()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }
}