package com.kirakishou.backend.fixmypc.model.dao

import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.toList
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.dto.DamageClaimIdLocationDTO
import com.kirakishou.backend.fixmypc.model.entity.DamageClaim
import com.kirakishou.backend.fixmypc.model.entity.LatLon
import com.kirakishou.backend.fixmypc.model.exception.DatabaseUnknownException
import com.kirakishou.backend.fixmypc.util.TextUtils
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.async
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.Statement
import java.sql.Timestamp

@Component
class DamageClaimDaoImpl(
        private val databaseThreadPool: ThreadPoolDispatcher,
        private val fileLog: FileLog
) : DamageClaimDao, AbstractDao() {

    private val TABLE_NAME = "public.damage_claims"

    override suspend fun saveOne(damageClaim: DamageClaim, connection: Connection): Boolean {
        return async(databaseThreadPool) {
            try {
                connection.prepareStatement("INSERT INTO $TABLE_NAME (owner_id, category, description, " +
                        "lat, lon, is_active, created_on, photos_array, deleted_on) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)", Statement.RETURN_GENERATED_KEYS).use { ps ->

                    val sqlArray = connection.createArrayOf("VARCHAR", damageClaim.imageNamesList.toTypedArray())

                    ps.setLong(1, damageClaim.id)
                    ps.setInt(2, damageClaim.category)
                    ps.setString(3, damageClaim.description)
                    ps.setDouble(4, damageClaim.lat)
                    ps.setDouble(5, damageClaim.lon)
                    ps.setBoolean(6, damageClaim.isActive)
                    ps.setTimestamp(7, Timestamp(damageClaim.createdOn))
                    ps.setArray(8, sqlArray)
                    ps.executeUpdate()

                    ps.generatedKeys.use {
                        if (it.next()) {
                            damageClaim.id = it.getLong(1)
                        }
                    }
                }

            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async true
        }.await()
    }

    override suspend fun findOne(id: Long, connection: Connection): Fickle<DamageClaim> {
        return async(databaseThreadPool) {
            var damageClaim: DamageClaim? = null

            try {
                connection.prepareStatementScrollable("SELECT * FROM $TABLE_NAME WHERE id = ? AND" +
                        " deleted_on IS NULL AND is_active = true LIMIT 1").use { ps ->

                    ps.setLong(1, id)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            val sqlArray = rs.getArray("photos_array")

                            damageClaim = DamageClaim(
                                    rs.getLong("id"),
                                    rs.getLong("owner_id"),
                                    true,
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on").time,
                                    sqlArray.toList())
                        }
                    }

                    /*damageClaim?.let { mf ->
                        getImagesByDamageClaimId(connection, arrayListOf(mf), listOf(mf.id))
                    }*/
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async Fickle.of(damageClaim)
        }.await()
    }

    override suspend fun findPaged(ownerId: Long, isActive: Boolean,
                                   offset: Long, count: Int, connection: Connection): List<DamageClaim> {
        return async(databaseThreadPool) {
            val malfunctions = arrayListOf<DamageClaim>()

            try {
                connection.prepareStatement("SELECT id, category, description, created_on, " +
                        "lat, lon, photos_array FROM $TABLE_NAME WHERE owner_id = ? AND is_active = ? AND " +
                        "deleted_on IS NULL ORDER BY id ASC OFFSET ? LIMIT ?").use { ps ->

                    ps.setLong(1, ownerId)
                    ps.setBoolean(2, isActive)
                    ps.setLong(3, offset)
                    ps.setInt(4, count)
                    //val ids = arrayListOf<Long>()

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val sqlArray = rs.getArray("photos_array")

                            val malfunction = DamageClaim(
                                    rs.getLong("id"),
                                    ownerId,
                                    isActive,
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on").time,
                                    sqlArray.toList())

                            malfunctions.add(malfunction)
                        }
                    }

                    //getImagesByDamageClaimId(connection, malfunctions, ids)
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async malfunctions
        }.await()
    }

    suspend override fun findAll(isActive: Boolean, connection: Connection): List<DamageClaim> {
        return async(databaseThreadPool) {
            val malfunctions = arrayListOf<DamageClaim>()

            try {
                connection.prepareStatement("SELECT * FROM $TABLE_NAME WHERE is_active = ?").use { ps ->
                    ps.setBoolean(1, isActive)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            malfunctions += DamageClaim.fromResultSet(rs)
                        }
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async malfunctions
        }.await()
    }

    suspend override fun findAll(connection: Connection): List<DamageClaim> {
        return async(databaseThreadPool) {
            val malfunctions = arrayListOf<DamageClaim>()

            try {
                connection.prepareStatement("SELECT * FROM $TABLE_NAME").use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            malfunctions += DamageClaim.fromResultSet(rs)
                        }
                    }
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async malfunctions
        }.await()
    }

    override suspend fun findManyByIdList(isActive: Boolean, idsToSearch: List<Long>, connection: Connection): List<DamageClaim> {
        return async(databaseThreadPool) {
            val damageClaimsList = arrayListOf<DamageClaim>()
            val ids = TextUtils.createStatementForList(idsToSearch.size)
            val sql = "SELECT id, owner_id, category, is_active, description, created_on, lat, lon, photos_array " +
                    "FROM $TABLE_NAME WHERE id IN ($ids) AND is_active = ? AND deleted_on IS NULL ORDER BY id ASC"

            try {
                connection.prepareStatement(sql).use { ps ->
                    var index = 1

                    for (id in idsToSearch) {
                        ps.setLong(index, id)
                        ++index
                    }

                    ps.setBoolean(index, isActive)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val sqlArray = rs.getArray("photos_array")

                            val malfunction = DamageClaim(
                                    rs.getLong("id"),
                                    rs.getLong("owner_id"),
                                    isActive,
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on").time,
                                    sqlArray.toList())

                            damageClaimsList.add(malfunction)
                        }
                    }

                    //getImagesByDamageClaimId(connection, damageClaimsList, idsToSearch)
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async damageClaimsList
        }.await()
    }

    override suspend fun findManyByOwnerId(isActive: Boolean, ownerId: Long, connection: Connection): List<DamageClaim> {
        return async(databaseThreadPool) {
            val malfunctions = arrayListOf<DamageClaim>()

            try {
                connection.prepareStatement("SELECT id, category, description, created_on, lat, lon, photos_array " +
                        "FROM $TABLE_NAME WHERE owner_id = ? AND is_active = ? AND deleted_on IS NULL ORDER BY id ASC").use { ps ->

                    ps.setLong(1, ownerId)
                    ps.setBoolean(2, isActive)
                    //val ids = arrayListOf<Long>()

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val sqlArray = rs.getArray("photos_array")

                            val malfunction = DamageClaim(
                                    rs.getLong("id"),
                                    ownerId,
                                    isActive,
                                    rs.getInt("category"),
                                    rs.getString("description"),
                                    rs.getDouble("lat"),
                                    rs.getDouble("lon"),
                                    rs.getTimestamp("created_on").time,
                                    sqlArray.toList())

                            malfunctions.add(malfunction)
                            //ids.add(malfunction.id)
                        }
                    }

                    /*if (malfunctions.isNotEmpty() && ids.isNotEmpty()) {
                        getImagesByDamageClaimId(connection, malfunctions, ids)
                    }*/
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async malfunctions
        }.await()
    }

    override suspend fun findAllIdsWithLocations(offset: Long, count: Long, connection: Connection): List<DamageClaimIdLocationDTO> {
        return async(databaseThreadPool) {
            val items = arrayListOf<DamageClaimIdLocationDTO>()

            try {
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
            } catch (error: Throwable) {
                fileLog.e(error)
                throw DatabaseUnknownException()
            }

            return@async items
        }.await()
    }

    override suspend fun deleteOne(id: Long, connection: Connection): Boolean {
        try {
            connection.prepareStatement("UPDATE $TABLE_NAME SET deleted_on = NOW() WHERE id = ?").use { ps ->
                ps.setLong(1, id)
                ps.executeUpdate()
            }
        } catch (error: Throwable) {
            fileLog.e(error)
            throw DatabaseUnknownException()
        }

        return true
    }

    override suspend fun deleteOnePermanently(id: Long, connection: Connection): Boolean {
        try {
            connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE id = ? LIMIT 1").use { ps ->
                ps.setLong(1, id)
                ps.executeUpdate()
            }
        } catch (error: Throwable) {
            fileLog.e(error)
            throw DatabaseUnknownException()
        }

        return true
    }
}






























