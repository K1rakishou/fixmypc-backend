package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Created by kirakishou on 7/17/2017.
 */

@Repository
class UserRepositoryImpl : UserRepository {

    @Autowired
    lateinit var template: JdbcTemplate

    override fun findOne(id: Long): Optional<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findByLogin(login: String):  Optional<User> {
        val user = template.queryForList("SELECT * FROM public.users WHERE login = ? AND deleted_on IS NULL LIMIT 1", arrayOf(login), User::class.java)
        if (user.isEmpty()) {
            return Optional.empty()
        }

        return Optional.of(user[0])
    }

    override fun findAll(): List<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(user: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}