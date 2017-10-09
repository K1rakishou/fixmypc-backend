package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.extension.deleteOnExitScope
import com.kirakishou.backend.fixmypc.extension.getFileExtension
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Flowables
import net.coobird.thumbnailator.Thumbnails
import org.apache.commons.io.IOUtils
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.awt.Dimension
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

@Component
class ImageServiceImpl : ImageService {

    private val tempDir = File("D:\\img\\tmp")

    @Autowired
    private lateinit var generator: Generator

    @Autowired
    private lateinit var log: FileLog

    @Autowired
    private lateinit var fs: FileSystem

    override fun serveImage(userId: Long, imageType: Int, imageNameParam: String, imageSizeParam: String): Single<ImageService.Get.Result> {
        return Single.just(ImageToServeParams(imageNameParam, imageSizeParam))
                .map { (imageName, imageSize) ->
                    val size = when (imageSize) {
                        "large" -> "l"
                        "medium" -> "m"
                        "small" -> "s"
                        else -> "m"
                    }

                    val folderName = when (imageType) {
                        0 -> "damage_claim"
                        1 -> "profile"
                        else -> return@map ImageService.Get.Result.BadFileName()
                    }

                    val extension = imageName.getFileExtension()
                    if (extension.isEmpty()) {
                        return@map ImageService.Get.Result.BadFileName()
                    }

                    val imageNameWithoutExtension = imageName.substring(0, imageName.length - extension.length - 1)
                    val fullPathToImage = Path("${fs.homeDirectory}/img/$folderName/$userId/${imageNameWithoutExtension}_$size.$extension")

                    if (!fs.exists(fullPathToImage)) {
                        return@map ImageService.Get.Result.NotFound()
                    }

                    return@map ImageService.Get.Result.Ok(fs.open(fullPathToImage).wrappedStream)
                }
    }

    override fun uploadImage(serverHomeDirectory: String, multipartFile: MultipartFile): Flowable<ImageService.Post.Result> {
        return resize(serverHomeDirectory, multipartFile)
                .flatMap {
                    val largeFileUploadObservable = upload(it.serverHomeDirectory, it.fileExtension, it.resizedImageLarge, it.resizedImageLargeName)
                    val mediumFileUploadObservable = upload(it.serverHomeDirectory, it.fileExtension, it.resizedImageMedium, it.resizedImageMediumName)
                    val smallFileUploadObservable = upload(it.serverHomeDirectory, it.fileExtension, it.resizedImageSmall, it.resizedImageSmallName)
                    val mergedStream = Flowable.merge(largeFileUploadObservable, mediumFileUploadObservable, smallFileUploadObservable)
                            .toList()
                            .toFlowable()

                    return@flatMap Flowables.zip(mergedStream, Flowable.just(it.originalImageName))
                }
                .map { (uploadedImagesList, originalImageName) ->
                    var isSuccess = true

                    for (response in uploadedImagesList) {
                        if (response.success) {
                            response.tempFile.delete()
                        } else {
                            isSuccess = false
                        }
                    }

                    if (!isSuccess) {
                        return@map ImageService.Post.Result.CouldNotUploadImage()
                    }

                    return@map ImageService.Post.Result.Ok(originalImageName)
                }
    }

    private fun resize(serverHomeDirectory: String, multipartFile: MultipartFile): Flowable<ResizedImageInfo> {
        return Flowable.just(ImageToUploadParams(serverHomeDirectory, multipartFile))
                .map { (serverHomeDir, originalFile) ->
                    val tempFile = File.createTempFile("o_temp", ".tmp", tempDir)
                    val extension = TextUtils.extractExtension(multipartFile.originalFilename)
                    val imageName = generator.generateTempFileName()

                    val resizedImage = ResizedImageInfo(
                            serverHomeDir,
                            extension,
                            "$imageName.$extension",
                            File.createTempFile("l_temp", ".tmp", tempDir),
                            "${imageName}_l",
                            File.createTempFile("m_temp", ".tmp", tempDir),
                            "${imageName}_m",
                            File.createTempFile("s_temp", ".tmp", tempDir),
                            "${imageName}_s")

                    tempFile.deleteOnExitScope { tempFileContainer ->
                        //copy original image
                        originalFile.transferTo(tempFileContainer)

                        //save large version of the image
                        resizeAndSaveImageOnDisk(tempFileContainer, Dimension(2560, 2560), resizedImage.resizedImageLarge, extension)

                        //save medium version of the image
                        resizeAndSaveImageOnDisk(tempFileContainer, Dimension(1536, 1536), resizedImage.resizedImageMedium, extension)

                        //save small version of the image
                        resizeAndSaveImageOnDisk(tempFileContainer, Dimension(512, 512), resizedImage.resizedImageSmall, extension)
                    }

                    return@map resizedImage
                }
    }

    private fun upload(serverHomeDirectoryParam: String, fileExtensionParam: String, imageFileParam: File, imageNameParam: String): Flowable<UploadResponse> {
        return Flowable.just(FileToUpload(serverHomeDirectoryParam, fileExtensionParam, imageFileParam, imageNameParam))
                .map { (serverHomeDirectory, fileExtension, imageFile, imageName) ->
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

                    return@map UploadResponse(true, fileNameWithExtension, imageFile)
                }
                .timeout(Constant.HADOOP_TIMEOUT, TimeUnit.SECONDS)
                .onErrorReturn { UploadResponse(false, "", imageFileParam) }
    }

    @Throws(Exception::class)
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

    private data class ImageToServeParams(val imageName: String,
                                  val imageSize: String)

    private data class ImageToUploadParams(val serverFilePath: String,
                                   val multipartFile: MultipartFile)

    private data class FileToUpload(val serverHomeDirectory: String,
                            val fileExtension: String,
                            val imageFile: File,
                            val imageName: String)

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
}