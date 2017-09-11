package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.TestUtils
import com.kirakishou.backend.fixmypc.core.Fickle
import com.kirakishou.backend.fixmypc.core.FileServerErrorCode
import com.kirakishou.backend.fixmypc.log.FileLog
import com.kirakishou.backend.fixmypc.manager.FileServersManager
import com.kirakishou.backend.fixmypc.manager.FileServersManagerImpl
import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswer
import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswerWrapper
import com.kirakishou.backend.fixmypc.model.entity.FileServerInfo
import com.kirakishou.backend.fixmypc.model.net.request.CreateDamageClaimRequest
import com.kirakishou.backend.fixmypc.model.repository.postgresql.DamageClaimDao
import com.kirakishou.backend.fixmypc.service.malfunction.CreateDamageClaimService
import com.kirakishou.backend.fixmypc.service.malfunction.CreateDamageClaimServiceImpl
import io.reactivex.Flowable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.After
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
import java.sql.SQLException
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class CreateDamageClaimRequestServiceTest {

    @InjectMocks
    val service = CreateDamageClaimServiceImpl()

    @Mock
    lateinit var log: FileLog

    @Mock
    lateinit var fileServerManager: FileServersManager

    @Mock
    lateinit var fileServerService: FileServerService

    @Mock
    lateinit var tempFileService: TempFilesService

    @Mock
    lateinit var generator: Generator

    @Mock
    lateinit var damageClaimDao: DamageClaimDao

    lateinit var bigImage: BufferedImage
    lateinit var normalImage: BufferedImage

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        ReflectionTestUtils.setField(service, "maxFileSize", 5242880)
        ReflectionTestUtils.setField(service, "maxRequestSize", 20971520)
        ReflectionTestUtils.setField(service, "FILE_SERVER_REQUEST_TIMEOUT", 1L)
        ReflectionTestUtils.setField(service, "fileServerHosts", arrayOf("127.0.0.1:9119", "127.0.0.1:9119"))

        bigImage = generateImage(2500, 2500)
        normalImage = generateImage(1000, 1000)

        val fileServerInfoList = arrayListOf<FileServerInfo>()
        fileServerInfoList.add(FileServerInfo("127.0.0.1", true))
        fileServerInfoList.add(FileServerInfo("127.0.0.2", true))
        fileServerInfoList.add(FileServerInfo("127.0.0.3", true))
        fileServerInfoList.add(FileServerInfo("127.0.0.4", true))
        fileServerManager.init(fileServerInfoList, -1)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    private fun generateImage(width: Int, height: Int): BufferedImage {
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

    private fun getBufferedImageBytes(bi: BufferedImage): ByteArray {
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
        val malfunctionRequestId = "34563467"
        val sessionId = "1234567"
        val uploadingFiles = arrayOf<MultipartFile>(MockMultipartFile("test", origFileName,
                MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        Mockito.`when`(generator.generateMalfunctionRequestId()).thenReturn(malfunctionRequestId)
        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(1)).thenReturn(listOf(FileServersManagerImpl.ServerWithId(0, FileServerInfo(host))))
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.Ok)
    }

    @Test
    fun shouldUploadIfAllImagesAreOk() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val host = "127.0.0.1"
        val tempFile = "tempfile"
        val malfunctionRequestId = "34563467"
        val sessionId = "1234567"

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

        Mockito.`when`(generator.generateMalfunctionRequestId()).thenReturn(malfunctionRequestId)
        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(4)).thenReturn(fourServers)
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.Ok)
    }

    @Test
    fun shouldNotUploadIfOneOfTheImagesSizeExceedsMaxSizeValue() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val sessionId = "1234567"
        val uploadingFiles = arrayOf<MultipartFile>(
                MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(bigImage)),
                MockMultipartFile("test2", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test3", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test4", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.FileSizeExceeded)
    }

    @Test
    fun shouldNotUploadIfImagesCountMoreThatImagesPerRequestValue() {
        val origFileName = "1234567890-234236-236-236-236.jpg"

        val sessionId = "1234567"
        val uploadingFiles = arrayOf<MultipartFile>(
                MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test2", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test3", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test4", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)),
                MockMultipartFile("test5", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.ImagesCountExceeded)
    }

    @Test
    fun shouldNotUploadInThereAreNoWorkingFileServers() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val sessionId = "1234567"
        val uploadingFiles = arrayOf<MultipartFile>(MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(false)

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.AllFileServersAreNotWorking)
    }

    @Test
    fun shouldNotUploadIfThereAreNoFiles() {
        val uploadingFiles = arrayOf<MultipartFile>()
        val sessionId = "1234567"

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.NoFilesToUpload)
    }

    @Test
    fun shouldReuploadFileToAnotherServerIfTimeoutHappened() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val host = "127.0.0.1"
        val tempFile = "tempfile"
        val malfunctionRequestId = "34563467"
        val sessionId = "1234567"
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

        Mockito.`when`(generator.generateMalfunctionRequestId()).thenReturn(malfunctionRequestId)
        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(4)).thenReturn(fourServers)
        Mockito.`when`(fileServerManager.getServer()).thenReturn(Fickle.of(fifthServer))

        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.REQUEST_TIMEOUT.value, emptyList()), ""))
                                .delay(1100, TimeUnit.MILLISECONDS))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "")))

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.Ok)
    }

    @Test
    fun shouldReturnErrorCodeIfThereNoWorkingFileServersWhileTryingToReuploadSomeImages() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val host = "127.0.0.1"
        val tempFile = "tempfile"
        val malfunctionRequestId = "34563467"
        val sessionId = "1234567"
        val file = MockMultipartFile("test", origFileName, MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage))

        val fourServers = listOf(
                FileServersManagerImpl.ServerWithId(0, FileServerInfo(host)),
                FileServersManagerImpl.ServerWithId(1, FileServerInfo(host, false)),
                FileServersManagerImpl.ServerWithId(2, FileServerInfo(host, false)),
                FileServersManagerImpl.ServerWithId(3, FileServerInfo(host, false)))

        Mockito.`when`(generator.generateMalfunctionRequestId()).thenReturn(malfunctionRequestId)
        Mockito.`when`(tempFileService.fromMultipartFile(file)).thenReturn(tempFile)
        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(1)).thenReturn(arrayListOf(fourServers[0]))

        Mockito.`when`(fileServerManager.getServer())
                .thenReturn(Fickle.of(fourServers[1]))
                .thenReturn(Fickle.empty())

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.REQUEST_TIMEOUT.value, emptyList()), ""))
                        .delay(1100, TimeUnit.MILLISECONDS))

        Mockito.`when`(fileServerService.saveDamageClaimImage(
                Mockito.anyInt(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                TestUtils.anyObject(),
                Mockito.anyInt(),
                Mockito.anyLong(),
                TestUtils.anyObject()))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.REQUEST_TIMEOUT.value, emptyList()), ""))
                        .delay(1100, TimeUnit.MILLISECONDS))

        val response = service.createDamageClaim(arrayOf(file), 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.AllFileServersAreNotWorking)
    }

    @Test
    fun shouldReturnDatabaseErrorWhenFailedToSaveRequestToDatabase() {
        val origFileName = "1234567890-234236-236-236-236.jpg"
        val host = "127.0.0.1"
        val tempFile = "tempfile"
        val malfunctionRequestId = "34563467"
        val sessionId = "1234567"
        val uploadingFiles = arrayOf<MultipartFile>(MockMultipartFile("test", origFileName,
                MediaType.IMAGE_JPEG_VALUE, getBufferedImageBytes(normalImage)))

        Mockito.`when`(generator.generateMalfunctionRequestId()).thenReturn(malfunctionRequestId)
        Mockito.`when`(fileServerManager.isAtLeastOneServerAlive()).thenReturn(true)
        Mockito.`when`(fileServerManager.getServers(1)).thenReturn(listOf(FileServersManagerImpl.ServerWithId(0, FileServerInfo(host))))
        Mockito.`when`(tempFileService.fromMultipartFile(TestUtils.anyObject())).thenReturn(tempFile)
        Mockito.`when`(damageClaimDao.saveOne(TestUtils.anyObject())).thenThrow(SQLException("DB is ded"))

        Mockito.`when`(fileServerService.saveDamageClaimImage(0, host, tempFile, origFileName, 0, 0L, malfunctionRequestId))
                .thenReturn(Flowable.just(FileServerAnswerWrapper(FileServerAnswer(FileServerErrorCode.OK.value, emptyList()), "n0_i45435346")))

        val response = service.createDamageClaim(uploadingFiles, 0, CreateDamageClaimRequest(0, "test", 0.0, 0.0), sessionId)
                .blockingGet()

        assertEquals(true, response is CreateDamageClaimService.Post.Result.DatabaseError)
    }
}




























