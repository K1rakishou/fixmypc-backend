package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.model.entity.AssignedSpecialist
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class AssignedSpecialistDaoImpl : AssignedSpecialistDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    private val TABLE_NAME = "public.assigned_specialists"

    override fun saveOne(assignedSpecialist: AssignedSpecialist): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("INSERT INTO $TABLE_NAME (damage_claim_id, user_id, who_assigned_user_id, is_work_done) VALUES (?, ?, false)").use { ps ->
                    ps.setLong(1, assignedSpecialist.damageClaimId)
                    ps.setLong(2, assignedSpecialist.specialistUserId)
                    ps.setLong(3, assignedSpecialist.whoAssignedUserId)

                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findOne(damageClaimId: Long, isWorkDone: Boolean): Either<Throwable, Fickle<AssignedSpecialist>> {
        var assignedSpecialist = Fickle.empty<AssignedSpecialist>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT specialist_user_id FROM $TABLE_NAME WHERE damage_claim_id = ? AND is_work_done = ?").use { ps ->
                    ps.setLong(1, damageClaimId)
                    ps.setBoolean(2, isWorkDone)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            assignedSpecialist = Fickle.of(AssignedSpecialist(
                                    damageClaimId,
                                    rs.getLong("specialist_user_id"),
                                    rs.getLong("who_assigned_user_id"),
                                    isWorkDone))
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(assignedSpecialist)
    }

    override fun findMany(damageClaimIdList: List<Long>, isWorkDone: Boolean): Either<Throwable, List<AssignedSpecialist>> {
        val idsStatement = TextUtils.createStatementForList(damageClaimIdList.size)
        val sql = "SELECT specialist_user_id FROM $TABLE_NAME WHERE damage_claim_id IN ($idsStatement) AND is_work_done = ?"
        val assignedSpecialistList = mutableListOf<AssignedSpecialist>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable(sql).use { ps ->
                    var index = 0

                    for (id in damageClaimIdList) {
                        ps.setLong(index + 1, id)
                        ++index
                    }

                    ps.setBoolean(index + 1, isWorkDone)

                    ps.executeQuery().use { rs ->
                        index = 0

                        while (rs.next()) {
                            assignedSpecialistList += AssignedSpecialist(
                                    damageClaimIdList[index],
                                    rs.getLong("specialist_user_id"),
                                    rs.getLong("who_assigned_user_id"),
                                    isWorkDone)

                            ++index
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(assignedSpecialistList)
    }

}