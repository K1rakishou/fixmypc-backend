package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.manager.FileServersManagerImpl
import com.kirakishou.backend.fixmypc.model.Fickle
import com.kirakishou.backend.fixmypc.model.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.FileServerErrorCode
import com.kirakishou.backend.fixmypc.model.FileServerInfo
import com.kirakishou.backend.fixmypc.model.net.request.MalfunctionRequest
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class MalfunctionRequestServiceTest {

    @InjectMocks
    val service = MalfunctionRequestServiceImpl()

    @Mock
    lateinit var log: FileLog

    @Mock
    lateinit var fileServerManager: FileServersManager

    @Mock
    lateinit var distributedImageServerService: DistributedImageServerService

    @Mock
    lateinit var tempFileService: TempFilesService

    /*@Mock
    lateinit var fileServerRequestsExecutorService: ExecutorService*/

    lateinit var tooBigImage: BufferedImage
    lateinit var normalImage: BufferedImage

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)

        ReflectionTestUtils.setField(service, "maxFileSize", 5242880)
        ReflectionTestUtils.setField(service, "maxRequestSize", 20971520)
        ReflectionTestUtils.setField(service, "maxImagesPerRequest", 4)
        ReflectionTestUtils.setField(service, "FILE_SERVER_REQUEST_TIMEOUT", 1L)
        ReflectionTestUtils.setField(service, "fileServerHosts", arrayOf("127.0.0.1:9119", "127.0.0.1:9119"))

        tooBigImage = generateImage(2500, 2500)
        normalImage = generateImage(1000, 1000)

        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true, true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true, true))
        fileServerManager.init(fileServerInfoList, -1)
    }

    fun generateImage(width: Int, height: Int): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val a = (Math.random() * 256).toInt() //alpha
                val r = (Math.random() * 256).toInt() //red
                val g = (Math.random() * 256).toInt() //green
                val b = (Math.random() * 256).toInt() //blue

                val p = a shl 24 or (r shl 16) or (g shl 8) or b //pixel

                img.setRGB(x, y, p)
            }
        }

        return img
    }

    fun getBufferedImageBytes(bi: BufferedImage): ByteArray {
        val baos = ByteArrayOutputStream()
        ImageIO.write(bi, "jpg", baos)
        baos.flush()
        val imageInByte = baos.toByteArray()
        baos.close()

        return imageInByte
    }

    @Test
    fun shouldUploadImageIsOk() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val host = "127.0.0.1"
        val tempFile = "tempfile"
        val uploadingFiles = arrayOf<MultipartFile>(MockMultipartFile("test", origFileName,
                MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(1)).thenReturn(listOf(FileServersManagerImpl.ServerWithId(0, FileServerInfo(host))))
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[0])).thenReturn(tempFile)

        Mockito.`when`(distributedImageServerService.storeImage(0, host, tempFile, origFileName,
                0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        val response = service.handleNewMalfunctionRequest(uploadingFiles, 0,
                MalfunctionRequest(0, "test")
        ).blockingGet()

        assertEquals(true, response is MalfunctionRequestService.Result.Ok)
    }

    @Test
    fun shouldUploadIfAllImagesAreOk() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val host = "127.0.0.1"
        val tempFile = "tempfile"
        val uploadingFiles = arrayOf<MultipartFile>(
                MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test2", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test3", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test4", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        val fourServers = listOf(
                FileServersManagerImpl.ServerWithId(0, FileServerInfo(host)),
                FileServersManagerImpl.ServerWithId(1, FileServerInfo(host)),
                FileServersManagerImpl.ServerWithId(2, FileServerInfo(host)),
                FileServersManagerImpl.ServerWithId(3, FileServerInfo(host)))

        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(4)).thenReturn(fourServers)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[0])).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[1])).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[2])).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[3])).thenReturn(tempFile)

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[0].id,
                fourServers[0].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[1].id,
                fourServers[1].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[2].id,
                fourServers[2].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[3].id,
                fourServers[3].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        val response = service.handleNewMalfunctionRequest(uploadingFiles, 0, MalfunctionRequest(0, "test"))
                .blockingGet()

        assertEquals(true, response is MalfunctionRequestService.Result.Ok)
    }

    @Test
    fun shouldNotUploadIfOneOfTheImagesSizeExceedsMaxSizeValue() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val uploadingFiles = arrayOf<MultipartFile>(
                MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(tooBigImage)),
                MockMultipartFile("test2", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test3", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test4", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        val response = service.handleNewMalfunctionRequest(uploadingFiles, 0, MalfunctionRequest(0, "test")).
                blockingGet()

        assertEquals(true, response is MalfunctionRequestService.Result.FileSizeExceeded)
    }

    @Test
    fun shouldNotUploadIfImagesCountMoreThatImagesPerRequestValue() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val uploadingFiles = arrayOf<MultipartFile>(
                MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test2", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test3", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test4", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test5", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        val response = service.handleNewMalfunctionRequest(uploadingFiles, 0, MalfunctionRequest(0, "test"))
                .blockingGet()

        assertEquals(true, response is MalfunctionRequestService.Result.ImagesCountExceeded)
    }

    @Test
    fun shouldNotUploadInThereAreNoWorkingFileServers() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val uploadingFiles = arrayOf<MultipartFile>(MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))
        val response = service.handleNewMalfunctionRequest(uploadingFiles, 0, MalfunctionRequest(0, "test"))
                .blockingGet()

        assertEquals(true, response is MalfunctionRequestService.Result.AllFileServersAreNotWorking)
    }

    @Test
    fun shouldNotUploadIfThereAreNoFiles() {
        val uploadingFiles = arrayOf<MultipartFile>()

        val response = service.handleNewMalfunctionRequest(uploadingFiles, 0, MalfunctionRequest(0, "test"))
                .blockingGet()

        assertEquals(true, response is MalfunctionRequestService.Result.NoFilesToUpload)
    }

    @Test
    fun shouldReuploadFileToAnotherServerIfTimeoutHappened() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val host = "127.0.0.1"
        val tempFile = "tempfile"
        val uploadingFiles = arrayOf<MultipartFile>(
                MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test2", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test3", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test4", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        val fourServers = listOf(
                FileServersManagerImpl.ServerWithId(0, FileServerInfo(host)),
                FileServersManagerImpl.ServerWithId(1, FileServerInfo(host)),
                FileServersManagerImpl.ServerWithId(2, FileServerInfo(host)),
                FileServersManagerImpl.ServerWithId(3, FileServerInfo(host)))

        val fifthServer = FileServersManagerImpl.ServerWithId(4, FileServerInfo(host))

        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(4)).thenReturn(fourServers)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[0])).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[1])).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[2])).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(uploadingFiles[3])).thenReturn(tempFile)

        Mockito.`when`(fileServerManager.getServer()).thenReturn(Fickle.of(fifthServer))

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[0].id,
                fourServers[0].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[1].id,
                fourServers[1].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.REQUEST_TIMEOUT.value, emptyList()))
                .delay(1100, TimeUnit.MILLISECONDS)
        )

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[2].id,
                fourServers[2].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        Mockito.`when`(distributedImageServerService.storeImage(
                fourServers[3].id,
                fourServers[3].fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        Mockito.`when`(distributedImageServerService.storeImage(
                fifthServer.id,
                fifthServer.fileServerInfo.host,
                tempFile,
                origFileName, 0, 0L, FileServerAnswer::class.java)
        ).thenReturn(Flowable.just(FileServerAnswer(FileServerErrorCode.OK.value, emptyList())))

        val response = service.handleNewMalfunctionRequest(uploadingFiles, 0, MalfunctionRequest(0, "test"))
                .blockingGet()

        assertEquals(true, response is MalfunctionRequestService.Result.Ok)
    }
}




























