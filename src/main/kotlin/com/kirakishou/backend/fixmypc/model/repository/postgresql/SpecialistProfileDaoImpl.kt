package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Timestamp
import javax.sql.DataSource

@Component
class SpecialistProfileDaoImpl : SpecialistProfileDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    private val TABLE_NAME = "public.specialist_profiles"

    override fun saveOne(specialistProfile: SpecialistProfile): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("INSERT INTO $TABLE_NAME (user_id, name, rating, photo_name, " +
                        "registered_on, success_repairs, fail_repairs) VALUES (?, ?, ?, ?, ?, ?, ?)").use { ps ->

                    ps.setLong(1, specialistProfile.userId)
                    ps.setString(2, specialistProfile.name)
                    ps.setFloat(3, specialistProfile.rating)
                    ps.setString(4, specialistProfile.photoName)
                    ps.setTimestamp(5, Timestamp(specialistProfile.registeredOn))
                    ps.setInt(6, specialistProfile.successRepairs)
                    ps.setInt(7, specialistProfile.failRepairs)

                    ps.executeUpdate()
                }
            }

        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findMany(userIdList: List<Long>): Either<Throwable, List<SpecialistProfile>> {
        val profileList = arrayListOf<SpecialistProfile>()
        val ids = TextUtils.createStatementForList(userIdList.size)
        val sql = "SELECT name, rating, photo_name, registered_on, success_repairs, fail_repairs FROM $TABLE_NAME " +
                "WHERE user_id IN ($ids)"

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    for ((i, id) in userIdList.withIndex()) {
                        ps.setLong(i + 1, id)
                    }

                    ps.executeQuery().use { rs ->
                        var index = 0

                        while (rs.next()) {
                            profileList += SpecialistProfile(
                                    userIdList[index],
                                    rs.getString("name"),
                                    rs.getFloat("rating"),
                                    rs.getString("photo_name"),
                                    rs.getTimestamp("registered_on").time,
                                    rs.getInt("success_repairs"),
                                    rs.getInt("fail_repairs"))

                            ++index
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(profileList)
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
























