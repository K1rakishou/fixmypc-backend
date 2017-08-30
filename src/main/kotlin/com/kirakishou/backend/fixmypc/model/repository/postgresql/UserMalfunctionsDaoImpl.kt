package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.log.FileLog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.SQLException
import javax.sql.DataSource

@Component
class UserMalfunctionsDaoImpl : UserMalfunctionsDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    @Autowired
    private lateinit var log: FileLog

    @Throws(SQLException::class)
    override fun addMalfunction(ownerId: Long, malfunctionId: Long) {
        hikariCP.connection.transactional { connection ->
            connection.prepareStatement("INSERT INTO public.user_malfunctions (owner_id, malfunction_id, deleted_on) VALUES (?, ?, NOW())").use { ps ->
                ps.setLong(1, ownerId)
                ps.setLong(2, malfunctionId)
                ps.executeUpdate()
            }
        }
    }

    @Throws(SQLException::class)
    override fun getMany(ownerId: Long, offset: Long, count: Long): List<Long> {
        val idsList = arrayListOf<Long>()

        hikariCP.connection.prepareStatementScrollable("SELECT malfunction_id FROM public.user_malfunctions WHERE " +
                "owner_id = ? AND deleted_on IS NULL OFFSET ? LIMIT ?").use { ps ->
            ps.setLong(1, ownerId)
            ps.setLong(2, offset)
            ps.setLong(3, count)

            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    idsList += rs.getLong("malfunction_id")
                }
            }
        }

        return idsList
    }

    @Throws(SQLException::class)
    override fun getAll(ownerId: Long): List<Long> {
        val idsList = arrayListOf<Long>()

        hikariCP.connection.prepareStatementScrollable("SELECT malfunction_id FROM public.user_malfunctions WHERE " +
                "owner_id = ? AND deleted_on IS NULL").use { ps ->
            ps.setLong(1, ownerId)

            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    idsList += rs.getLong("malfunction_id")
                }
            }
        }

        return idsList
    }

    @Throws(SQLException::class)
    override fun removeMalfunction(ownerId: Long, malfunctionId: Long) {
        hikariCP.connection.use { connection ->
            connection.prepareStatement("UPDATE public.user_malfunctions SET deleted_on = NOW() WHERE owner_id = ? AND malfunction_id = ?").use { ps ->
                ps.setLong(1, ownerId)
                ps.setLong(2, malfunctionId)
                ps.executeUpdate()
            }
        }
    }
}