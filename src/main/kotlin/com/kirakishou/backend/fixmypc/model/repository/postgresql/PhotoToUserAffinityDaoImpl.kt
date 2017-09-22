package com.kirakishou.backend.fixmypc.model.repository.postgresql

import com.kirakishou.backend.fixmypc.core.Either
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.extension.prepareStatementScrollable
import com.kirakishou.backend.fixmypc.model.dto.PhotoInfoDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class PhotoToUserAffinityDaoImpl : PhotoToUserAffinityDao {

    @Autowired
    private lateinit var hikariCP: DataSource

    override fun getOne(imageName: String): Either<Throwable, Fickle<PhotoInfoDTO>> {
        val photoInfo = PhotoInfoDTO()
        var damageClaimId: Long = -1

        try {
            hikariCP.connection.use { connection ->
                connection.prepareStatementScrollable("SELECT damage_claim_id, photo_type FROM public.damage_claims_photos " +
                        "WHERE photo_name = ? AND deleted_on IS NULL").use { ps ->
                    ps.setString(1, imageName)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            damageClaimId = rs.getLong("damage_claim_id")
                            photoInfo.imageType = rs.getInt("photo_type")
                        }
                    }
                }

                if (damageClaimId == -1L) {
                    return Either.Value(Fickle.empty())
                }

                connection.prepareStatementScrollable("SELECT owner_id, folder_name FROM public.damage_claims WHERE id = ? AND deleted_on IS NULL").use { ps ->
                    ps.setLong(1, damageClaimId)

                    ps.executeQuery().use { rs ->
                        if (rs.first()) {
                            photoInfo.ownerId = rs.getLong("owner_id")
                            photoInfo.folderName = rs.getString("folder_name")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Either.Error(e)
        }

        if (photoInfo.ownerId == -1L || photoInfo.folderName.isEmpty() || photoInfo.imageType == -1) {
            return Either.Value(Fickle.empty())
        }

        return Either.Value(Fickle.of(photoInfo))
    }
}