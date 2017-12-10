package com.kirakishou.backend.fixmypc.log

import com.kirakishou.backend.fixmypc.util.ServerUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

open class FileLogImpl(printLog: Boolean) : FileLog {
    private val MSECS_IN_MINUTE = (1000 * 60).toLong()
    private val MSECS_IN_HOUR = MSECS_IN_MINUTE * 60
    private val MSECS_IN_DAY = MSECS_IN_HOUR * 24
    private val defaultFormat: String = "yyyy-MM-dd HH:mm:ss"
    private val LOG_DUMP_TIME: Long = MSECS_IN_MINUTE * 10
    private val logQueue = LinkedBlockingQueue<String>()
    private val semaphore = Semaphore(0)

    private var thread: Thread? = null
    private var lastTimeDump: Long = 0

    var printLogInConsole = AtomicBoolean(printLog)

    init {
        try {
            thread = Thread {
                this.dumpLogToFile()
            }

            thread!!.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun now(): Long {
        return ServerUtils.getTimeFast()
    }

    fun longToFormatedTime(time: Long, format: String): String {
        val dateFormat = SimpleDateFormat(format)
        val date = Date()
        date.time = time
        return dateFormat.format(date)
    }

    fun getFormattedTime(): String {
        return longToFormatedTime(now(), defaultFormat)
    }

    private fun dumpLogToFile() {
        lastTimeDump = now()

        var osw: OutputStreamWriter? = null
        var lastTimeDateCheck: Long = 0
        var dayOfWeek = -1
        val calendar = Calendar.getInstance()

        try {
            while (true) {
                val now = now()

                if (now - lastTimeDateCheck > MSECS_IN_HOUR) {
                    lastTimeDateCheck = now
                    calendar.time = Date()

                    if (dayOfWeek != calendar.get(Calendar.DAY_OF_MONTH)) {
                        dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH)

                        val currentRelativePath = Paths.get("")
                        val currentPath = currentRelativePath.toAbsolutePath().toString()

                        val folderNameFormat = SimpleDateFormat("dd_MM_yyyy")
                        val folderName = folderNameFormat.format(now)

                        val fileNameFormat = SimpleDateFormat("HH_mm_ss")
                        val fileName = fileNameFormat.format(now)

                        val fullPath = File(currentPath + "/logs/" + folderName)
                        if (!fullPath.exists()) {
                            fullPath.mkdirs()
                        }

                        val file = File(fullPath, fileName + ".txt")
                        if (!file.exists()) {
                            file.createNewFile()
                        }

                        val fos = FileOutputStream(file)

                        if (osw != null) {
                            osw.close()
                        }

                        osw = OutputStreamWriter(fos)
                    }
                }

                try {
                    semaphore.acquire()
                } catch (e: InterruptedException) {
                    println("dumpLogToFile() Interrupted")
                    break
                }

                val deltaTime = now - lastTimeDump

                synchronized(logQueue) {
                    if (deltaTime < LOG_DUMP_TIME) {
                        return@synchronized
                    }

                    lastTimeDump = now

                    try {
                        while (logQueue.peek() != null) {
                            osw!!.write(logQueue.poll())
                        }

                        osw!!.flush()

                    } catch (e: IOException) {
                        println("dumpLogToFile() exception" + e.message)
                        return@synchronized
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            try {
                if (osw != null) {
                    osw.flush()
                    osw.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun e(message: String) {
        val sdf2 = SimpleDateFormat("dd:MM:yyyy HH:mm:ss")
        val str = "EXCEPTION: [" + sdf2.format(ServerUtils.getTimeFast()) + "] " + message + "\n"

        synchronized(logQueue) {
            logQueue.add(str)
        }

        semaphore.release()

        if (!printLogInConsole.get()) {
            return
        }

        System.err.println(getFormattedTime() + ": " + message)
    }

    override fun e(exception: Throwable) {
        val sdf2 = SimpleDateFormat("dd:MM:yyyy HH:mm:ss")
        val str = StringBuilder("EXCEPTION: [" + sdf2.format(ServerUtils.getTimeFast()) + "] " + exception + "\n")
        val ste = exception.stackTrace
        for (e in ste) {
            str.append(e.toString())
            str.append("\r\n")
        }

        synchronized(logQueue) {
            logQueue.add(str.toString())
        }

        semaphore.release()

        if (!printLogInConsole.get()) {
            return
        }

        //exception.printStackTrace();
        System.err.println(str)
    }

    override fun d(message: String) {
        val sdf2 = SimpleDateFormat("dd:MM:yyyy HH:mm:ss")
        val str = "DEBUG: [" + sdf2.format(ServerUtils.getTimeFast()) + "] " + message + "\n"

        synchronized(logQueue) {
            logQueue.add(str)
        }

        semaphore.release()

        if (!printLogInConsole.get()) {
            return
        }

        println(getFormattedTime() + ": " + message)
    }

    override fun w(message: String) {
        val sdf2 = SimpleDateFormat("dd:MM:yyyy HH:mm:ss")
        val str = "WARNING: [" + sdf2.format(ServerUtils.getTimeFast()) + "] " + message + "\n"

        synchronized(logQueue) {
            logQueue.add(str)
        }

        semaphore.release()

        if (!printLogInConsole.get()) {
            return
        }

        System.err.println(getFormattedTime() + ": " + message)
    }
}