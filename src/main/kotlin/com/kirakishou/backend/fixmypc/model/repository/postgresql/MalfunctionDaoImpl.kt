package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import com.kirakishou.backend.fixmypc.util.TextUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

@Repository
class MalfunctionDaoImpl : MalfunctionDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    @Autowired
    private lateinit var log: FileLog

    @Throws(SQLException::class)
    override fun createNewMalfunctionRequest(malfunction: Malfunction) {
        hikariCP.connection.transactional { connection ->
            connection.prepareStatement("INSERT INTO public.malfunctions (owner_id, category, description, malfunction_request_id, lat, lon, is_active, created_on, deleted_on) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)", Statement.RETURN_GENERATED_KEYS).use { ps ->

                ps.setLong(1, malfunction.ownerId)
                ps.setInt(2, malfunction.category)
                ps.setString(3, malfunction.description)
                ps.setString(4, malfunction.malfunctionRequestId)
                ps.setDouble(5, malfunction.lat)
                ps.setDouble(6, malfunction.lon)
                ps.setBoolean(7, malfunction.isActive)
                ps.setTimestamp(8, malfunction.createdOn)
                ps.executeUpdate()

                ps.generatedKeys.use {
                    if (it.next()) {
                        malfunction.id = it.getLong(1)
                    }
                }
            }

            connection.prepareStatement("INSERT INTO public.malfunction_photos (malfunction_id, image_name, image_type, deleted_on) " +
                    "VALUES (?, ?, ?, NULL)").use { ps ->

                for (imageName in malfunction.imageNamesList) {
                    ps.setLong(1, malfunction.id)
                    ps.setString(2, imageName)
                    ps.setInt(3, Constant.ImageTypes.IMAGE_TYPE_MALFUNCTION_PHOTO)

                    ps.addBatch()
                }

                ps.executeBatch()
            }
        }
    }

    @Throws(SQLException::class)
    override fun findMalfunctionRequestById(id: Long): Fickle<Malfunction> {
        var malfunction: Malfunction? = null

        hikariCP.connection.use { connection ->
            connection.prepareStatementScrollable("SELECT * FROM public.malfunctions WHERE id = ? AND" +
                    " deleted_on IS NULL AND is_active = true LIMIT 1").use { ps ->

                ps.setLong(1, id)

                ps.executeQuery().use { rs ->
                    if (rs.first()) {
                        malfunction = Malfunction(
                                rs.getLong("id"),
                                rs.getLong("owner_id"),
                                true,
                                rs.getString("malfunction_request_id"),
                                rs.getInt("category"),
                                rs.getString("description"),
                                rs.getDouble("lat"),
                                rs.getDouble("lon"),
                                rs.getTimestamp("created_on"))
                    }
                }

                malfunction?.let { mf ->
                    getImagesByMalfunctionId(connection, arrayListOf(mf), listOf(mf.id))
                }
            }
        }

        return Fickle.of(malfunction)
    }

    @Throws(SQLException::class)
    private fun getImagesByMalfunctionId(connection: Connection, malfunctions: ArrayList<Malfunction>, malfunctionIdList: List<Long>) {
        val malfunctionIdsCount = malfunctionIdList.size
        val idsToSearch = TextUtils.createStatementForList(malfunctionIdsCount)

        val sql = "SELECT malfunction_id, image_name FROM public.malfunction_photos WHERE malfunction_id IN ($idsToSearch) " +
                "AND deleted_on IS NULL"

        connection.prepareStatement(sql).use { ps ->
            for (i in 0 until malfunctionIdsCount) {
                ps.setLong(i + 1, malfunctionIdList[i])
            }

            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    val id = rs.getLong("malfunction_id")
                    val imageName = rs.getString("image_name")

                    val malfunction = malfunctions.firstOrNull { it.id == id }
                    if (malfunction == null) {
                        throw NullPointerException("MalfunctionIdList does not contain this id: $id")
                    }

                    malfunction.imageNamesList.add(imageName)
                }
            }
        }
    }

    @Throws(SQLException::class)
    override fun getUserMalfunctionRequestList(ownerId: Long, isActive: Boolean, offset: Long, count: Int): List<Malfunction> {
        val malfunctions = arrayListOf<Malfunction>()

        hikariCP.connection.use { connection ->
            connection.prepareStatement("SELECT id, category, description, created_on, malfunction_request_id, lat, lon " +
                    "FROM public.malfunctions WHERE owner_id = ? AND is_active = ? AND deleted_on IS NULL OFFSET ? LIMIT ?").use { ps ->

                ps.setLong(1, ownerId)
                ps.setBoolean(2, isActive)
                ps.setLong(3, offset)
                ps.setInt(4, count)
                val ids = arrayListOf<Long>()

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val malfunction = Malfunction(
                                rs.getLong("id"),
                                ownerId,
                                isActive,
                                rs.getString("malfunction_request_id"),
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

        return malfunctions
    }

    @Throws(SQLException::class)
    override fun getAllUserMalfunctions(ownerId: Long, isActive: Boolean): List<Malfunction> {
        val malfunctions = arrayListOf<Malfunction>()

        hikariCP.connection.use { connection ->
            connection.prepareStatement("SELECT id, category, description, created_on, malfunction_request_id, lat, lon " +
                    "FROM public.malfunctions WHERE owner_id = ? AND is_active = ? AND deleted_on IS NULL ORDER BY created_on DESC").use { ps ->

                ps.setLong(1, ownerId)
                ps.setBoolean(2, isActive)
                val ids = arrayListOf<Long>()

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val malfunction = Malfunction(
                                rs.getLong("id"),
                                ownerId,
                                isActive,
                                rs.getString("malfunction_request_id"),
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

        return malfunctions
    }

    @Throws(SQLException::class)
    override fun deleteMalfunctionRequest(id: Long) {
        hikariCP.connection.transactional { connection ->
            deleteMalfunction(connection, id)
            deletePhoto(connection, id)
        }
    }

    @Throws(SQLException::class)
    private fun deletePhoto(connection: Connection, id: Long) {
        connection.prepareStatement("UPDATE public.malfunction_photos SET deleted_on = NOW() WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeUpdate()
        }
    }

    @Throws(SQLException::class)
    private fun deleteMalfunction(connection: Connection, id: Long) {
        connection.prepareStatement("UPDATE public.malfunctions SET deleted_on = NOW() WHERE id = ?").use { ps ->
            ps.setLong(1, id)
            ps.executeUpdate()
        }
    }
}






























