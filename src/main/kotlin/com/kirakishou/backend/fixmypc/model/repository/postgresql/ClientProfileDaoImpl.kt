package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactionalUse
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.SQLException
import javax.sql.DataSource

@Component
class ClientProfileDaoImpl : ClientProfileDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    private val TABLE_NAME = "public.client_profiles"

    override fun saveOne(clientProfile: ClientProfile): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.transactionalUse { connection ->
                connection.prepareStatement("INSERT INTO $TABLE_NAME (user_id, name, phone, is_profile_filled_out) " +
                        "VALUES (?, ?, ?, ?) ON CONFLICT (user_id) " +
                        "DO UPDATE SET name = EXCLUDED.name, phone = EXCLUDED.phone, is_profile_filled_out = EXCLUDED.is_profile_filled_out").use { ps ->
                    ps.setLong(1, clientProfile.userId)
                    ps.setString(2, clientProfile.name)
                    ps.setString(3, clientProfile.phone)
                    ps.setBoolean(4, clientProfile.isFilledOut)

                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findOne(userId: Long): Either<SQLException, Fickle<ClientProfile>> {
        var clientProfile: ClientProfile? = null

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT name, phone, is_profile_filled_out FROM $TABLE_NAME WHERE user_id = ?").use { ps ->
                    ps.setLong(1, userId)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            clientProfile = ClientProfile(
                                    userId,
                                    rs.getString("name"),
                                    rs.getString("phone"),
                                    rs.getBoolean("is_profile_filled_out"))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(Fickle.of(clientProfile))
    }

    override fun deleteOne(userId: Long): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE user_id = ?").use { ps ->
                    ps.setLong(1, userId)
                    ps.execute()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }
}































