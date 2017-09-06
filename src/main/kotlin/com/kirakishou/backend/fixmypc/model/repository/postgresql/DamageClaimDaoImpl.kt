package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdLocationDTO
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

@Repository
class DamageClaimDaoImpl : DamageClaimDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    override fun saveOne(damageClaim: DamageClaim): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.transactional { connection ->
                connection.prepareStatement("INSERT INTO public.damage_claims (owner_id, category, description, " +
                        "folder_name, lat, lon, is_active, created_on, deleted_on) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)", Statement.RETURN_GENERATED_KEYS).use { ps ->

                    ps.setLong(1, damageClaim.ownerId)
                    ps.setInt(2, damageClaim.category)
                    ps.setString(3, damageClaim.description)
                    ps.setString(4, damageClaim.damageClaimRequestId)
                    ps.setDouble(5, damageClaim.lat)
                    ps.setDouble(6, damageClaim.lon)
                    ps.setBoolean(7, damageClaim.isActive)
                    ps.setTimestamp(8, damageClaim.createdOn)
                    ps.executeUpdate()

                    ps.generatedKeys.use {
                        if (it.next()) {
                            damageClaim.id = it.getLong(1)
                        }
                    }
                }

                connection.prepareStatement("INSERT INTO public.damage_claims_photos (malfunction_id, " +
                        "photo_name, photo_type, deleted_on) " +
                        "VALUES (?, ?, ?, NULL)").use { ps ->

                    for (imageName in damageClaim.imageNamesList) {
                        ps.setLong(1, damageClaim.id)
                        ps.setString(2, imageName)
                        ps.setInt(3, Constant.ImageTypes.IMAGE_TYPE_MALFUNCTION_PHOTO)

                        ps.addBatch()
                    }

                    ps.executeBatch()
                }
            }

        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findOne(id: Long): Either<SQLException, Fickle<DamageClaim>> {
        var damageClaim: DamageClaim? = null

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT * FROM public.damage_claims WHERE id = ? AND" +
                        " deleted_on IS NULL AND is_active = true LIMIT 1").use { ps ->

                    ps.setLong(1, id)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            damageClaim = DamageClaim(
                                    rs.getLong("id"),
                                    rs.getLong("owner_id"),
                                    true,
                                    rs.getString("folder_name"),
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on"))
                        }
                    }

                    damageClaim?.let { mf ->
                        getImagesByMalfunctionId(connection, arrayListOf(mf), listOf(mf.id))
                    }
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(Fickle.of(damageClaim))
    }

    override fun findPaged(ownerId: Long, isActive: Boolean,
                           offset: Long, count: Int): Either<SQLException, List<DamageClaim>> {
        val malfunctions = arrayListOf<DamageClaim>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("SELECT id, category, description, created_on, folder_name, " +
                        "lat, lon FROM public.damage_claims WHERE owner_id = ? AND is_active = ? AND " +
                        "deleted_on IS NULL ORDER BY id ASC OFFSET ? LIMIT ?").use { ps ->

                    ps.setLong(1, ownerId)
                    ps.setBoolean(2, isActive)
                    ps.setLong(3, offset)
                    ps.setInt(4, count)
                    val ids = arrayListOf<Long>()

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val malfunction = DamageClaim(
                                    rs.getLong("id"),
                                    ownerId,
                                    isActive,
                                    rs.getString("folder_name"),
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on"))

                            malfunctions.add(malfunction)
                            ids.add(malfunction.id)
                        }
                    }

                    getImagesByMalfunctionId(connection, malfunctions, ids)
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(malfunctions)
    }

    override fun findManyActive(idsToSearch: List<Long>): Either<SQLException, List<DamageClaim>> {
        TODO()
    }

    override fun findManyActive(ownerId: Long): Either<SQLException, List<DamageClaim>> {
        return getMany(ownerId, true)
    }

    override fun findManyInactive(ownerId: Long): Either<SQLException, List<DamageClaim>> {
        return getMany(ownerId, false)
    }

    override fun findAllIdsWithLocations(offset: Long, count: Long): List<DamageClaimIdLocationDTO> {
        val items = arrayListOf<DamageClaimIdLocationDTO>()

        hikariCP.connection.use { connection ->
            connection.prepareStatement("SELECT id, lat, lon FROM public.damage_claims WHERE deleted_on IS NULL OFFSET ? LIMIT ?").use { ps ->
                ps.setLong(1, offset)
                ps.setLong(2, count)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val item =  DamageClaimIdLocationDTO(
                                rs.getLong("id"),
                                LatLon(rs.getDouble("lat"),
                                        rs.getDouble("lon")))

                        items.add(item)
                    }
                }
            }
        }

        return items
    }

    override fun deleteOne(id: Long): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.transactional { connection ->
                deleteMalfunction(connection, id)
                deletePhoto(connection, id)
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun deleteOnePermanently(id: Long): Either<SQLException, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM public.damage_claims WHERE id = ? LIMIT 1").use { ps ->
                    ps.setLong(1, id)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    private fun getMany(ownerId: Long, isActive: Boolean): Either<SQLException, List<DamageClaim>> {
        val malfunctions = arrayListOf<DamageClaim>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("SELECT id, category, description, created_on, folder_name, lat, lon " +
                        "FROM public.damage_claims WHERE owner_id = ? AND is_active = ? AND deleted_on IS NULL ORDER BY id ASC").use { ps ->

                    ps.setLong(1, ownerId)
                    ps.setBoolean(2, isActive)
                    val ids = arrayListOf<Long>()

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val malfunction = DamageClaim(
                                    rs.getLong("id"),
                                    ownerId,
                                    isActive,
                                    rs.getString("folder_name"),
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on"))

                            malfunctions.add(malfunction)
                            ids.add(malfunction.id)
                        }
                    }

                    getImagesByMalfunctionId(connection, malfunctions, ids)
                }
            }
        } catch (e: SQLException) {
            return Either.Error(e)
        }

        return Either.Value(malfunctions)
    }

    private fun getImagesByMalfunctionId(connection: Connection, damageClaims: ArrayList<DamageClaim>,
                                         malfunctionIdList: List<Long>) {
        val malfunctionIdsCount = malfunctionIdList.size
        val idsToSearch = TextUtils.createStatementForList(malfunctionIdsCount)

        val sql = "SELECT malfunction_id, photo_name FROM public.damage_claims_photos WHERE malfunction_id IN ($idsToSearch) " +
                "AND deleted_on IS NULL"

        connection.prepareStatement(sql).use { ps ->
            for (i in 0 until malfunctionIdsCount) {
                ps.setLong(i + 1, malfunctionIdList[i])
            }

            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    val id = rs.getLong("malfunction_id")
                    val imageName = rs.getString("photo_name")

                    val malfunction = damageClaims.firstOrNull { it.id == id }
                    if (malfunction == null) {
                        throw NullPointerException("MalfunctionIdList does not contain this id: $id")
                    }

                    malfunction.imageNamesList.add(imageName)
                }
            }
        }
    }

    private fun deletePhoto(connection: Connection, id: Long) {
        connection.prepareStatement("UPDATE public.damage_claims_photos SET deleted_on = NOW() WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeUpdate()
        }
    }

    private fun deleteMalfunction(connection: Connection, id: Long) {
        connection.prepareStatement("UPDATE public.damage_claims_photos SET deleted_on = NOW() WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeUpdate()
        }
    }
}






























