package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class RespondedSpecialistsDaoImpl : RespondedSpecialistsDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    private val TABLE_NAME = "public.responded_specialists"

    override fun saveOne(respondedSpecialist: RespondedSpecialist): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("INSERT INTO $TABLE_NAME " +
                        "(user_id, damage_claim_id, closed_at) VALUES (?, ?, NULL)").use { ps ->
                    ps.setLong(1, respondedSpecialist.userId)
                    ps.setLong(2, respondedSpecialist.damageClaimId)

                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findOne(userId: Long, damageClaimId: Long): Either<Throwable, Fickle<RespondedSpecialist>> {
        var specialist: RespondedSpecialist? = null

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT id FROM $TABLE_NAME WHERE damage_claim_id = ? AND user_id = ? LIMIT 1").use { ps ->
                    ps.setLong(1, damageClaimId)
                    ps.setLong(2, userId)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            specialist = RespondedSpecialist(
                                    rs.getLong("id"),
                                    userId,
                                    damageClaimId
                            )
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(Fickle.of(specialist))
    }

    override fun findAllForDamageClaimPaged(damageClaimId: Long, skip: Long, count: Long): Either<Throwable, List<RespondedSpecialist>> {
        val respondedSpecialistsList = mutableListOf<RespondedSpecialist>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("SELECT id, user_id FROM $TABLE_NAME " +
                        "WHERE damage_claim_id = ? AND closed_at is NULL OFFSET ? LIMIT ?").use { ps ->
                    ps.setLong(1, damageClaimId)
                    ps.setLong(2, skip)
                    ps.setLong(3, count)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            respondedSpecialistsList += RespondedSpecialist(
                                    rs.getLong("id"),
                                    rs.getLong("user_id"),
                                    damageClaimId)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(respondedSpecialistsList)
    }

    override fun deleteAllForDamageClaim(damageClaimId: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("UPDATE $TABLE_NAME SET closed_at = NOW() WHERE id = ?").use { ps ->
                    ps.setLong(1, damageClaimId)

                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }
}