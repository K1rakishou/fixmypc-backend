package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.extension.transactional
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.model.Constant
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.entity.Malfunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

@Repository
class MalfunctionRepositoryImpl : MalfunctionRepository {

    @Autowired
    lateinit var hikariCP: DataSource

    @Autowired
    lateinit var log: FileLog

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

                        val images = arrayListOf<String>()

                        connection.prepareStatement("SELECT image_name FROM public.malfunction_photos WHERE malfunction_id = ? " +
                                "AND deleted_on IS NULL LIMIT ${Constant.MALFUNCTION_MAX_IMAGES_PER_REQUEST}").use { ps2 ->

                            ps2.setLong(1, malfunction.get().id)

                            ps2.executeQuery().use { rs2 ->
                                while (rs2.next()) {
                                    images.add(rs2.getString("image_name"))
                                }
                            }

                            malfunction.get().imageNamesList = images
                        }
                    }
                }
            }
        }

        return malfunction
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





























