package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.model.AccountType
import com.kirakishou.backend.fixmypc.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.*
import javax.sql.DataSource

/**
 * Created by kirakishou on 7/17/2017.
 */

@Repository
class UserRepositoryImpl : UserRepository {

    @Autowired
    lateinit var hikariCP: DataSource

    override fun findByLogin(login: String): Optional<User> {
        var user: Optional<User> = Optional.empty()

        hikariCP.connection.use { connection ->
            connection.prepareStatementScrollable("SELECT * FROM public.users WHERE login = ? AND deleted_on IS NULL LIMIT 1").use { ps ->
                ps.setString(1, login)
                ps.executeQuery().use { rs ->
                    if (rs.first()) {
                        user = Optional.of(User(rs.getString("login"),
                                rs.getString("password"),
                                AccountType.from(rs.getInt("account_type"))))
                    }
                }
            }
        }

        return user
    }

    override fun createNew(user: User) {
        hikariCP.connection.transactional { connection ->
            connection.prepareStatement("INSERT INTO public.users (login, password, account_type, created_on, " +
                    "deleted_on) VALUES (?, ?, ?, NOW(), NULL)").use { ps ->

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