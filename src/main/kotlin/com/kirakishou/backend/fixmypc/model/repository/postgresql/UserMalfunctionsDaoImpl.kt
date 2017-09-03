package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.SQLException
import javax.sql.DataSource

@Component
class UserMalfunctionsDaoImpl : UserMalfunctionsDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    override fun saveOne(ownerId: Long, malfunctionId: Long): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.transactional { connection ->
                connection.prepareStatement("INSERT INTO public.user_malfunctions (owner_id, malfunction_id, deleted_on) VALUES (?, ?, NULL)").use { ps ->
                    ps.setLong(1, ownerId)
                    ps.setLong(2, malfunctionId)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): Either<SQLException, List<Long>> {
        val idsList = arrayListOf<Long>()

        try {
            hikariCP.connection.prepareStatementScrollable("SELECT malfunction_id FROM public.user_malfunctions WHERE " +
                    "owner_id = ? AND deleted_on IS NULL OFFSET ? LIMIT ? ORDER BY id ASC").use { ps ->
                ps.setLong(1, ownerId)
                ps.setLong(2, offset)
                ps.setLong(3, count)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        idsList += rs.getLong("malfunction_id")
                    }
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(idsList)
    }

    override fun findAll(ownerId: Long):Either<SQLException, List<Long>> {
        val idsList = arrayListOf<Long>()

        try {
            hikariCP.connection.prepareStatementScrollable("SELECT malfunction_id FROM public.user_malfunctions WHERE " +
                    "owner_id = ? AND deleted_on IS NULL ORDER BY id ASC").use { ps ->
                ps.setLong(1, ownerId)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        idsList += rs.getLong("malfunction_id")
                    }
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(idsList)
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("UPDATE public.user_malfunctions SET deleted_on = NOW() WHERE owner_id = ? AND malfunction_id = ?").use { ps ->
                    ps.setLong(1, ownerId)
                    ps.setLong(2, malfunctionId)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun deleteOnePermanently(ownerId: Long, malfunctionId: Long): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM public.user_malfunctions WHERE owner_id = ? AND malfunction_id = ? LIMIT 1").use { ps ->
                    ps.setLong(1, ownerId)
                    ps.setLong(2, malfunctionId)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }
}