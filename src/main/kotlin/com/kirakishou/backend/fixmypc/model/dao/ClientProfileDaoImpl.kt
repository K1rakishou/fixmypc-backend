package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.ClientProfile
import com.kirakishou.backend.fixmypc.model.exception.DatabaseUnknownException
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.async
import javax.sql.DataSource

class ClientProfileDaoImpl(
        val hikariCP: DataSource,
        val databaseThreadPool: ThreadPoolDispatcher,
        val fileLog: FileLog
) : ClientProfileDao {

    private val TABLE_NAME = "public.client_profiles"

    override suspend fun saveOne(clientProfile: ClientProfile): Boolean {
        return async(databaseThreadPool) {
            try {
                hikariCP.connection.use { connection ->
                    connection.prepareStatement("INSERT INTO $TABLE_NAME (user_id, name, phone) " +
                            "VALUES (?, ?, ?) ON CONFLICT (user_id) " +
                            "DO UPDATE SET name = EXCLUDED.name, phone = EXCLUDED.phone").use { ps ->
                        ps.setLong(1, clientProfile.userId)
                        ps.setString(2, clientProfile.name)
                        ps.setString(3, clientProfile.phone)

                        ps.executeUpdate()
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async true
        }.await()
    }

    override suspend fun findOne(userId: Long): Fickle<ClientProfile> {
        return async(databaseThreadPool) {
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
                                        rs.getString("phone"))
                            }
                        }
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async Fickle.of(clientProfile)
        }.await()
    }

    /*override fun deleteOne(userId: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE user_id = ?").use { ps ->
                    ps.setLong(1, userId)
                    ps.execute()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }*/
}































