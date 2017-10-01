package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactionalUse
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdLocationDTO
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.Statement
import java.sql.Timestamp
import javax.sql.DataSource

@Component
class DamageClaimDaoImpl : DamageClaimDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    private val TABLE_NAME = "public.damage_claims"
    private val PHOTOS_TABLE_NAME = "public.damage_claims_photos"

    override fun saveOne(damageClaim: DamageClaim): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("INSERT INTO $TABLE_NAME (owner_id, category, description, " +
                        "folder_name, lat, lon, is_active, created_on, deleted_on) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)", Statement.RETURN_GENERATED_KEYS).use { ps ->

                    ps.setLong(1, damageClaim.ownerId)
                    ps.setInt(2, damageClaim.category)
                    ps.setString(3, damageClaim.description)
                    ps.setString(4, damageClaim.folderName)
                    ps.setDouble(5, damageClaim.lat)
                    ps.setDouble(6, damageClaim.lon)
                    ps.setBoolean(7, damageClaim.isActive)
                    ps.setTimestamp(8, Timestamp(damageClaim.createdOn))
                    ps.executeUpdate()

                    ps.generatedKeys.use {
                        if (it.next()) {
                            damageClaim.id = it.getLong(1)
                        }
                    }
                }

                connection.prepareStatement("INSERT INTO $PHOTOS_TABLE_NAME (damage_claim_id, " +
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

        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findOne(id: Long): Either<Throwable, Fickle<DamageClaim>> {
        var damageClaim: DamageClaim? = null

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT * FROM $TABLE_NAME WHERE id = ? AND" +
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
                                    rs.getTimestamp("created_on").time)
                        }
                    }

                    damageClaim?.let { mf ->
                        getImagesByDamageClaimId(connection, arrayListOf(mf), listOf(mf.id))
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(Fickle.of(damageClaim))
    }

    override fun findPaged(ownerId: Long, isActive: Boolean,
                           offset: Long, count: Int): Either<Throwable, List<DamageClaim>> {
        val malfunctions = arrayListOf<DamageClaim>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("SELECT id, category, description, created_on, folder_name, " +
                        "lat, lon FROM $TABLE_NAME WHERE owner_id = ? AND is_active = ? AND " +
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
                                    rs.getTimestamp("created_on").time)

                            malfunctions.add(malfunction)
                            ids.add(malfunction.id)
                        }
                    }

                    getImagesByDamageClaimId(connection, malfunctions, ids)
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(malfunctions)
    }

    override fun findManyByIdList(isActive: Boolean, idsToSearch: List<Long>): Either<Throwable, List<DamageClaim>> {
        val damageClaimsList = arrayListOf<DamageClaim>()
        val ids = TextUtils.createStatementForList(idsToSearch.size)
        val sql = "SELECT id, owner_id, category, is_active, description, created_on, folder_name, lat, lon " +
                "FROM $TABLE_NAME WHERE id IN ($ids) AND is_active = ? AND deleted_on IS NULL ORDER BY id ASC"

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    var index = 0

                    for (id in idsToSearch) {
                        ps.setLong(index, id)
                        ++index
                    }

                    ps.setBoolean(index, isActive)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val malfunction = DamageClaim(
                                    rs.getLong("id"),
                                    rs.getLong("owner_id"),
                                    isActive,
                                    rs.getString("folder_name"),
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on").time)

                            damageClaimsList.add(malfunction)
                        }
                    }

                    getImagesByDamageClaimId(connection, damageClaimsList, idsToSearch)
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(damageClaimsList)
    }

    override fun findManyByOwnerId(isActive: Boolean, ownerId: Long): Either<Throwable, List<DamageClaim>> {
        val malfunctions = arrayListOf<DamageClaim>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("SELECT id, category, description, created_on, folder_name, lat, lon " +
                        "FROM $TABLE_NAME WHERE owner_id = ? AND is_active = ? AND deleted_on IS NULL ORDER BY id ASC").use { ps ->

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
                                    rs.getTimestamp("created_on").time)

                            malfunctions.add(malfunction)
                            ids.add(malfunction.id)
                        }
                    }

                    if (malfunctions.isNotEmpty() && ids.isNotEmpty()) {
                        getImagesByDamageClaimId(connection, malfunctions, ids)
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(malfunctions)
    }

    override fun findAllIdsWithLocations(offset: Long, count: Long): List<DamageClaimIdLocationDTO> {
        val items = arrayListOf<DamageClaimIdLocationDTO>()

        hikariCP.connection.use { connection ->
            connection.prepareStatement("SELECT id, lat, lon FROM $TABLE_NAME WHERE deleted_on IS NULL OFFSET ? LIMIT ?").use { ps ->
                ps.setLong(1, offset)
                ps.setLong(2, count)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val item = DamageClaimIdLocationDTO(
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

    override fun deleteOne(id: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.transactionalUse { connection ->
                deleteDamageClaim(connection, id)
                deletePhoto(connection, id)
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun deleteOnePermanently(id: Long): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE id = ? LIMIT 1").use { ps ->
                    ps.setLong(1, id)
                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    private fun getImagesByDamageClaimId(connection: Connection, damageClaims: ArrayList<DamageClaim>,
                                         damageClaimIdList: List<Long>) {
        if (damageClaims.isEmpty() || damageClaimIdList.isEmpty()) {
            return
        }

        val malfunctionIdsCount = damageClaimIdList.size
        val idsToSearch = TextUtils.createStatementForList(malfunctionIdsCount)

        val sql = "SELECT damage_claim_id, photo_name FROM $PHOTOS_TABLE_NAME WHERE damage_claim_id IN ($idsToSearch) " +
                "AND deleted_on IS NULL"

        connection.prepareStatement(sql).use { ps ->
            for (i in 0 until malfunctionIdsCount) {
                ps.setLong(i + 1, damageClaimIdList[i])
            }

            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    val id = rs.getLong("damage_claim_id")
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
        connection.prepareStatement("UPDATE $PHOTOS_TABLE_NAME SET deleted_on = NOW() WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeUpdate()
        }
    }

    private fun deleteDamageClaim(connection: Connection, id: Long) {
        connection.prepareStatement("UPDATE $TABLE_NAME SET deleted_on = NOW() WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeUpdate()
        }
    }
}






























