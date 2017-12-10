package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import com.kirakishou.backend.fixmypc.model.exception.DatabaseException
import com.kirakishou.backend.fixmypc.util.TextUtils
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.async
import org.springframework.stereotype.Component
import java.sql.Timestamp
import javax.sql.DataSource

@Component
class SpecialistProfileDaoImpl(
        val hikariCP: DataSource,
        val databaseThreadPool: ThreadPoolDispatcher,
        val fileLog: FileLog
) : SpecialistProfileDao {

    private val TABLE_NAME = "public.specialist_profiles"

    override suspend fun saveOne(specialistProfile: SpecialistProfile): Boolean {
        return async(databaseThreadPool) {
            try {
                hikariCP.connection.use { connection ->
                    connection.prepareStatement("INSERT INTO $TABLE_NAME (user_id, name, rating, photo_name, phone," +
                            "registered_on, success_repairs, fail_repairs) VALUES (?, ?, ?, ?, ?, ?, ?)").use { ps ->

                        ps.setLong(1, specialistProfile.userId)
                        ps.setString(2, specialistProfile.name)
                        ps.setFloat(3, specialistProfile.rating)
                        ps.setString(4, specialistProfile.photoName)
                        ps.setString(5, specialistProfile.phone)
                        ps.setTimestamp(6, Timestamp(specialistProfile.registeredOn))
                        ps.setInt(7, specialistProfile.successRepairs)
                        ps.setInt(8, specialistProfile.failRepairs)

                        ps.executeUpdate()
                    }
                }

            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseException()
            }

            return@async true
        }.await()
    }

    override suspend fun findOne(userId: Long): Fickle<SpecialistProfile> {
        return async(databaseThreadPool) {
            var specialistProfile = Fickle.empty<SpecialistProfile>()

            try {
                hikariCP.connection.use { connection ->
                    connection.prepareStatementScrollable("SELECT name, rating, photo_name, phone, registered_on, success_repairs, fail_repairs " +
                            "FROM $TABLE_NAME WHERE user_id = ?").use { ps ->
                        ps.setLong(1, userId)

                        ps.executeQuery().use { rs ->
                            if (rs.first()) {
                                specialistProfile = Fickle.of(SpecialistProfile(
                                        userId,
                                        rs.getString("name") ?: "",
                                        rs.getFloat("rating"),
                                        rs.getString("photo_name") ?: "",
                                        rs.getString("phone") ?: "",
                                        rs.getTimestamp("registered_on").time,
                                        rs.getInt("success_repairs"),
                                        rs.getInt("fail_repairs")))
                            }
                        }
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseException()
            }

            return@async specialistProfile
        }.await()
    }

    override suspend fun findMany(userIdList: List<Long>): List<SpecialistProfile> {
        return async(databaseThreadPool) {
            val profileList = arrayListOf<SpecialistProfile>()
            val ids = TextUtils.createStatementForList(userIdList.size)
            val sql = "SELECT name, rating, photo_name, phone, registered_on, success_repairs, fail_repairs FROM $TABLE_NAME " +
                    "WHERE user_id IN ($ids)"

            try {
                hikariCP.connection.use { connection ->
                    connection.prepareStatementScrollable(sql).use { ps ->
                        for ((i, id) in userIdList.withIndex()) {
                            ps.setLong(i + 1, id)
                        }

                        ps.executeQuery().use { rs ->
                            var index = 0

                            while (rs.next()) {
                                profileList += SpecialistProfile(
                                        userIdList[index],
                                        rs.getString("name") ?: "",
                                        rs.getFloat("rating"),
                                        rs.getString("photo_name") ?: "",
                                        rs.getString("phone") ?: "",
                                        rs.getTimestamp("registered_on").time,
                                        rs.getInt("success_repairs"),
                                        rs.getInt("fail_repairs"))

                                ++index
                            }
                        }
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseException()
            }

            return@async profileList
        }.await()
    }

    override suspend fun updateInfo(userId: Long, name: String, phone: String): Boolean {
        return async(databaseThreadPool) {
            try {
                hikariCP.connection.use { connection ->
                    connection.prepareStatement("UPDATE $TABLE_NAME SET phone = ?, name = ? WHERE user_id = ?").use { ps ->
                        ps.setString(1, phone)
                        ps.setString(2, name)
                        ps.setLong(3, userId)

                        ps.executeUpdate()
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseException()
            }

            return@async true
        }.await()
    }

    override suspend fun updatePhoto(userId: Long, photoName: String): Boolean {
        return async(databaseThreadPool) {
            try {
                hikariCP.connection.use { connection ->
                    connection.prepareStatement("UPDATE $TABLE_NAME SET photo_name = ? WHERE user_id = ?").use { ps ->
                        ps.setString(1, photoName)
                        ps.setLong(2, userId)

                        ps.executeUpdate()
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseException()
            }

            return@async true
        }.await()
    }

    /*override fun deleteOne(userId: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement()
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }
    }*/

}

























