package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.model.entity.ProfilePhoto
import org.springframework.beans.factory.annotation.Autowired
import javax.sql.DataSource

class ProfilePhotoDaoImpl : ProfilePhotoDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    private val TABLE_NAME = "public.profile_photos"

    override fun saveOne(profilePhoto: ProfilePhoto): Either<Throwable, Boolean> {
        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatement("INSERT INTO $TABLE_NAME (user_id, photo_folder, photo_name) VALUES (?, ?, ?)").use { ps ->
                    ps.setLong(1, profilePhoto.userId)
                    ps.setString(2, profilePhoto.photoFolder)
                    ps.setString(3, profilePhoto.photoName)

                    ps.executeUpdate()
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(true)
    }

    override fun findOne(userId: Long): Either<Throwable, Fickle<ProfilePhoto>> {
        var profilePhoto = Fickle.empty<ProfilePhoto>()

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT photo_folder, photo_name FROM $TABLE_NAME WHERE user_id = ? LIMIT 1").use { ps ->
                    ps.setLong(1, userId)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            profilePhoto = Fickle.of(ProfilePhoto(
                                    userId,
                                    rs.getString("photo_folder"),
                                    rs.getString("photo_name")))
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        return Either.Value(profilePhoto)
    }
}