package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import javax.sql.DataSource

/**
 * Created by kirakishou on 7/17/2017.
 */

@Repository
class UserDaoImpl : UserDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    @Autowired
    private lateinit var log: FileLog

    override fun findByLogin(login: String): Fickle<User> {
        var user: Fickle<User> = Fickle.empty()

        hikariCP.connection.use { connection ->
            connection.prepareStatementScrollable("SELECT * FROM public.users WHERE login = ? AND deleted_on IS NULL LIMIT 1").use { ps ->
                ps.setString(1, login)
                ps.executeQuery().use { rs ->
                    if (rs.first()) {
                        user = Fickle.of(User(
                                rs.getLong("id"),
                                rs.getString("login"),
                                rs.getString("password"),
                                AccountType.from(rs.getInt("account_type")),
                                rs.getTimestamp("created_on")))
                    }
                }
            }
        }

        return user
    }

    override fun createNew(user: User) {
        hikariCP.connection.transactional(log) { connection ->
            connection.prepareStatement("INSERT INTO public.users (login, password, account_type, created_on, deleted_on) " +
                    "VALUES (?, ?, ?, NOW(), NULL)").use { ps ->

                ps.setString(1, user.login)
                ps.setString(2, user.password)
                ps.setInt(3, user.accountType.value)
                ps.executeUpdate()
            }
        }
    }

    //for tests only!!!
    override fun deleteByLogin(login: String) {
        hikariCP.connection.use { connection ->
            connection.prepareStatement("DELETE FROM public.users WHERE login = ?").use { ps ->
                ps.setString(1, login)
                ps.execute()
            }
        }
    }
}