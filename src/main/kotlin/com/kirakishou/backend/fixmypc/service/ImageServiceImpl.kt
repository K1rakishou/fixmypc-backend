package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.extension.deleteOnExitScope
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.util.TextUtils
import io.reactivex.Flowable
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

    override fun uploadImage(serverHomeDirectory: String, multipartFile: MultipartFile): Flowable<MutableList<String>> {
        return resize(serverHomeDirectory, multipartFile)
                .flatMap {
                    val largeFileUploadObservable = upload(it.serverHomeDirectory, it.fileExtension, it.resizedImageLarge, it.resizedImageLargeName)
                    val mediumFileUploadObservable = upload(it.serverHomeDirectory, it.fileExtension, it.resizedImageMedium, it.resizedImageMediumName)
                    val smallFileUploadObservable = upload(it.serverHomeDirectory, it.fileExtension, it.resizedImageSmall, it.resizedImageSmallName)

                    return@flatMap Flowable.merge(largeFileUploadObservable, mediumFileUploadObservable, smallFileUploadObservable)
                }
                .toList()
                .map { uploadResponseList ->
                    val successfullyUploaded = mutableListOf<String>()

                    for (response in uploadResponseList) {
                        if (response.success) {
                            successfullyUploaded += response.uploadedImageName
                            response.tempFile.delete()
                        } else {
                            val fullPath = "$serverHomeDirectory${response.uploadedImageName}"
                            fs.delete(Path(fullPath), false)
                        }
                    }

                    return@map successfullyUploaded
                }
                .toFlowable()
    }

    private fun resize(serverHomeDirectory: String, multipartFile: MultipartFile): Flowable<ResizedImageInfo> {
        return Flowable.just(ImageParams(serverHomeDirectory, multipartFile))
                .map { (serverHomeDir, originalFile) ->
                    val tempFile = File.createTempFile("o_temp", ".tmp", tempDir)
                    val extension = TextUtils.extractExtension(multipartFile.originalFilename)

                    val resizedImage = ResizedImageInfo(
                            serverHomeDir,
                            extension,
                            File.createTempFile("l_temp", ".tmp", tempDir),
                            "${generator.generateTempFileName()}_l",
                            File.createTempFile("m_temp", ".tmp", tempDir),
                            "${generator.generateTempFileName()}_m",
                            File.createTempFile("s_temp", ".tmp", tempDir),
                            "${generator.generateTempFileName()}_s")

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

        if (imageToResize == null) {
            println("imageToResize is null!!!")
        }

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

    data class ImageParams(val serverFilePath: String,
                           val multipartFile: MultipartFile)

    data class FileToUpload(val serverHomeDirectory: String,
                            val fileExtension: String,
                            val imageFile: File,
                            val imageName: String)

    data class ResizedImageInfo(val serverHomeDirectory: String,
                                val fileExtension: String,
                                val resizedImageLarge: File,
                                val resizedImageLargeName: String,
                                val resizedImageMedium: File,
                                val resizedImageMediumName: String,
                                val resizedImageSmall: File,
                                val resizedImageSmallName: String)

    data class UploadResponse(val success: Boolean,
                              val uploadedImageName: String,
                              val tempFile: File)
}