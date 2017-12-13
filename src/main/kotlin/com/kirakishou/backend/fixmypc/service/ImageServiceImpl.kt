package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.extension.getFileExtension
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.util.TextUtils
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.async
import net.coobird.thumbnailator.Thumbnails
import org.apache.commons.io.IOUtils
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import java.awt.Dimension
import java.io.File
import java.io.InputStream
import javax.annotation.PostConstruct
import javax.imageio.ImageIO

class ImageServiceImpl(
        private val fileLog: FileLog,
        private val fs: FileSystem,
        private val hadoopThreadPool: ThreadPoolDispatcher
) : ImageService {

    private val tempDir = File("D:\\projects\\data\\fixmypc\\tmp")

    @PostConstruct
    fun init() {
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
    }

    override suspend fun uploadImage(serverHomeDirectory: String, imageFile: File, originalImageName: String, newImageName: String): Deferred<Boolean> {
        return async(hadoopThreadPool) {
            var uploadedImagesList: List<UploadResponse>? = null

            try {
                val rii = resize(serverHomeDirectory, imageFile, originalImageName, newImageName)
                val largeFileUploadResponse = upload(serverHomeDirectory, rii.fileExtension, rii.resizedImageLarge, rii.resizedImageLargeName)
                val mediumFileUploadResponse = upload(serverHomeDirectory, rii.fileExtension, rii.resizedImageMedium, rii.resizedImageMediumName)
                val smallFileUploadResponse = upload(serverHomeDirectory, rii.fileExtension, rii.resizedImageSmall, rii.resizedImageSmallName)

                val uploadedImagesDeferredList = listOf(largeFileUploadResponse, mediumFileUploadResponse, smallFileUploadResponse)
                uploadedImagesList = uploadedImagesDeferredList.map { it.await() }

                val hasBadResponses = uploadedImagesList.any { !it.success }
                if (hasBadResponses) {
                    return@async false
                }
            } catch (error: Throwable) {
                fileLog.e(error)
                return@async false
            } finally {
                if (uploadedImagesList != null) {
                    uploadedImagesList.forEach { it.tempFile.delete() }
                }
            }

            return@async true
        }
    }

    override suspend fun serveImage(userId: Long, imageType: Int, imageName: String, imageSize: String): InputStream {
        return async(hadoopThreadPool) {
            val size = when (imageSize) {
                "large" -> "l"
                "medium" -> "m"
                "small" -> "s"
                else -> "m"
            }

            val folderName = when (imageType) {
                0 -> "damage_claim"
                1 -> "profile"
                else -> {
                    fileLog.e("Bad image type: $imageType")
                    throw BadImageTypeException()
                }
            }

            val extension = imageName.getFileExtension()
            if (extension.isEmpty()) {
                fileLog.e("Bad file extension")
                throw BadFileNameException()
            }

            val imageNameWithoutExtension = imageName.substring(0, imageName.length - extension.length - 1)
            val fullPathToImage = Path("${fs.homeDirectory}/img/$folderName/$userId/${imageNameWithoutExtension}_$size.$extension")

            if (!fs.exists(fullPathToImage)) {
                fileLog.e("File does not exist")
                throw FileNotFoundException()
            }

            return@async fs.open(fullPathToImage).wrappedStream
        }.await()
    }

    override suspend fun deleteImage(serverHomeDirectory: String, imageName: String): Deferred<Boolean> {
        return async(hadoopThreadPool) {
            try {
                val extension = imageName.getFileExtension()
                if (extension.isEmpty()) {
                    fileLog.e("Bad file extension")
                    throw BadFileNameException()
                }

                val imageNameWithoutExtension = imageName.substring(0, imageName.length - extension.length - 1)
                val smallImage = "$serverHomeDirectory/${imageNameWithoutExtension}_s.$extension"
                val mediumImage = "$serverHomeDirectory/${imageNameWithoutExtension}_m.$extension"
                val largeImage = "$serverHomeDirectory/${imageNameWithoutExtension}_l.$extension"

                fs.delete(Path(smallImage), false)
                fs.delete(Path(mediumImage), false)
                fs.delete(Path(largeImage), false)

                return@async true
            } catch (error: Throwable) {
                fileLog.e(error)
                return@async false
            }
        }
    }

    private suspend fun resize(serverHomeDirectory: String, imageFile: File, originalImageName: String, newImageName: String): ResizedImageInfo {
        val extension = TextUtils.extractExtension(originalImageName)

        val resizedImage = ResizedImageInfo(
                serverHomeDirectory,
                extension,
                "$newImageName.$extension",
                File.createTempFile("l_temp", ".tmp", tempDir),
                "${newImageName}_l",
                File.createTempFile("m_temp", ".tmp", tempDir),
                "${newImageName}_m",
                File.createTempFile("s_temp", ".tmp", tempDir),
                "${newImageName}_s")

        //save large version of the image
        resizeAndSaveImageOnDisk(imageFile, Dimension(2560, 2560), resizedImage.resizedImageLarge, extension)

        //save medium version of the image
        resizeAndSaveImageOnDisk(imageFile, Dimension(1536, 1536), resizedImage.resizedImageMedium, extension)

        //save small version of the image
        resizeAndSaveImageOnDisk(imageFile, Dimension(512, 512), resizedImage.resizedImageSmall, extension)

        return resizedImage
    }

    private suspend fun upload(serverHomeDirectory: String, fileExtension: String, imageFile: File, imageName: String): Deferred<UploadResponse> {
        return async(hadoopThreadPool) {
            val fileNameWithExtension = "$imageName.$fileExtension"
            fs.mkdirs(Path(serverHomeDirectory))

            val fullPath = "$serverHomeDirectory$fileNameWithExtension"
            val inputStream = imageFile.inputStream()
            val outputStream = fs.create(Path(fullPath))

            try {
                IOUtils.copyLarge(inputStream, outputStream)
            } finally {
                inputStream.close()
                outputStream.close()
            }

            return@async UploadResponse(true, fileNameWithExtension, imageFile)
        }
    }

    private fun resizeAndSaveImageOnDisk(originalImageFile: File, newMaxSize: Dimension, resizedImageFile: File, extension: String) {
        val imageToResize = ImageIO.read(originalImageFile)

        //original image size should be bigger than the new size, otherwise we don't need to resize image, just copy it
        if (imageToResize.width > newMaxSize.width || imageToResize.height > newMaxSize.height) {
            val resizedImage = Thumbnails.of(imageToResize)
                    .useExifOrientation(true)
                    .size(newMaxSize.width, newMaxSize.height)
                    .asBufferedImage()

            ImageIO.write(resizedImage, extension, resizedImageFile)

        } else {
            originalImageFile.copyTo(resizedImageFile, true)
        }
    }

    private data class ResizedImageInfo(val serverHomeDirectory: String,
                                        val fileExtension: String,
                                        val originalImageName: String,
                                        val resizedImageLarge: File,
                                        val resizedImageLargeName: String,
                                        val resizedImageMedium: File,
                                        val resizedImageMediumName: String,
                                        val resizedImageSmall: File,
                                        val resizedImageSmallName: String)

    private data class UploadResponse(val success: Boolean,
                                      val uploadedImageName: String,
                                      val tempFile: File)

    class BadFileNameException : Exception()
    class BadImageTypeException : Exception()
    class FileNotFoundException : Exception()

}