package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactionalUse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class UserToDamageClaimKeyAffinityDaoImpl : UserToDamageClaimKeyAffinityDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    override fun saveOne(ownerId: Long, malfunctionId: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.transactionalUse { connection ->
                connection.prepareStatement("INSERT INTO public.user_to_damage_claim_key_affinity " +
                        "(owner_id, damage_claim_id, deleted_on) VALUES (?, ?, NULL)").use { ps ->
                    ps.setLong(1, ownerId)
                    ps.setLong(2, malfunctionId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findMany(ownerId: Long, offset: Long, count: Long): Either<Throwable, List<Long>> {
        val idsList = arrayListOf<Long>()

        try {
            hikariCP.connection.prepareStatementScrollable("SELECT damage_claim_id FROM public.user_to_damage_claim_key_affinity WHERE " +
                    "owner_id = ? AND deleted_on IS NULL OFFSET ? LIMIT ? ORDER BY id ASC").use { ps ->
                ps.setLong(1, ownerId)
                ps.setLong(2, offset)
                ps.setLong(3, count)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        idsList += rs.getLong("damage_claim_id")
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(idsList)
    }

    override fun findAll(ownerId: Long): Either<Throwable, List<Long>> {
        val idsList = arrayListOf<Long>()

        try {
            hikariCP.connection.prepareStatementScrollable("SELECT damage_claim_id FROM public.user_to_damage_claim_key_affinity WHERE " +
                    "owner_id = ? AND deleted_on IS NULL ORDER BY id ASC").use { ps ->
                ps.setLong(1, ownerId)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        idsList += rs.getLong("damage_claim_id")
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(idsList)
    }

    override fun deleteOne(ownerId: Long, malfunctionId: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("UPDATE public.user_to_damage_claim_key_affinity SET deleted_on = NOW() " +
                        "WHERE owner_id = ? AND damage_claim_id = ?").use { ps ->
                    ps.setLong(1, ownerId)
                    ps.setLong(2, malfunctionId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun deleteOnePermanently(ownerId: Long, malfunctionId: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM public.user_to_damage_claim_key_affinity " +
                        "WHERE owner_id = ? AND damage_claim_id = ? LIMIT 1").use { ps ->
                    ps.setLong(1, ownerId)
                    ps.setLong(2, malfunctionId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }
}