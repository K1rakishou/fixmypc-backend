package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
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
        hikariCP.connection.transactional(log) { connection ->
            connection.prepareStatement("INSERT INTO public.malfunctions (owner_id, category, description, malfunction_request_id, created_on, deleted_on) " +
                    "VALUES (?, ?, ?, ?, NOW(), NULL)", Statement.RETURN_GENERATED_KEYS).use { ps ->

                ps.setLong(1, malfunction.owner_id)
                ps.setInt(2, malfunction.category)
                ps.setString(3, malfunction.description)
                ps.setString(4, malfunction.malfunctionRequestId)
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
        var malfunction: Fickle<Malfunction> = Fickle.empty()

        hikariCP.connection.use { connection ->
            connection.prepareStatementScrollable("SELECT * FROM public.malfunctions WHERE id = ? AND" +
                    " deleted_on IS NULL LIMIT 1").use { ps ->

                ps.setLong(1, id)

                ps.executeQuery().use { rs ->
                    if (rs.first()) {
                        malfunction = Fickle.of(Malfunction(
                                rs.getLong("id"),
                                rs.getLong("owner_id"),
                                rs.getString("malfunction_request_id"),
                                rs.getInt("category"),
                                rs.getString("description"),
                                rs.getTimestamp("created_on")))

                        malfunction.get().imageNamesList = getImagesByMalfunctionId(connection, malfunction.get().id)
                    }
                }
            }
        }

        return malfunction
    }

    //TODO: Rewrite this function to accept list of malfunctionId
    @Throws(SQLException::class)
    private fun getImagesByMalfunctionId(connection: Connection, malfunctionId: Long): List<String> {
        val images = arrayListOf<String>()

        connection.prepareStatement("SELECT image_name FROM public.malfunction_photos WHERE malfunction_id = ? " +
                "AND deleted_on IS NULL LIMIT ${Constant.MALFUNCTION_MAX_IMAGES_PER_REQUEST}").use { ps ->

            ps.setLong(1, malfunctionId)

            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    images.add(rs.getString("image_name"))
                }
            }

        }

        return images
    }

    @Throws(SQLException::class)
    override fun getUserMalfunctionRequestList(ownerId: Long, offset: Long, count: Int): List<Malfunction> {
        val malfunctions = arrayListOf<Malfunction>()

        hikariCP.connection.use { connection ->
            connection.prepareStatement("SELECT id, category, description, created_on, malfunction_request_id " +
                    "FROM public.malfunctions WHERE owner_id = ? AND deleted_on IS NULL OFFSET ? LIMIT ?").use { ps ->

                ps.setLong(1, ownerId)
                ps.setLong(2, offset)
                ps.setInt(3, count)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val malfunction = Malfunction(
                                rs.getLong("id"),
                                ownerId,
                                rs.getString("malfunction_request_id"),
                                rs.getInt("category"),
                                rs.getString("description"),
                                rs.getTimestamp("created_on"))

                        malfunction.imageNamesList = getImagesByMalfunctionId(connection, malfunction.id)
                    }
                }
            }
        }

        return malfunctions
    }

    override fun getAllUserMalfunctions(ownerId: Long): List<Malfunction> {
        val malfunctions = arrayListOf<Malfunction>()

        hikariCP.connection.use { connection ->
            connection.prepareStatement("SELECT id, category, description, created_on, malfunction_request_id " +
                    "FROM public.malfunctions WHERE owner_id = ? AND deleted_on IS NULL").use { ps ->

                ps.setLong(1, ownerId)

                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val malfunction = Malfunction(
                                rs.getLong("id"),
                                ownerId,
                                rs.getString("malfunction_request_id"),
                                rs.getInt("category"),
                                rs.getString("description"),
                                rs.getTimestamp("created_on"))

                        malfunction.imageNamesList = getImagesByMalfunctionId(connection, malfunction.id)
                    }
                }
            }
        }

        return malfunctions
    }

    @Throws(SQLException::class)
    override fun deleteMalfunctionRequest(id: Long) {
        hikariCP.connection.use { connection ->
            connection.prepareStatement("UPDATE public.malfunctions SET deleted_on = NOW() WHERE id = ?").use { ps ->
                ps.setLong(1, id)
                ps.executeUpdate()
            }

            connection.prepareStatement("UPDATE public.malfunction_photos SET deleted_on = NOW() WHERE id = ?").use { ps ->
                ps.setLong(1, id)
                ps.executeUpdate()
            }
        }
    }
}






























