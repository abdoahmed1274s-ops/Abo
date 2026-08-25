package com.example.downloader

import android.content.Context
import android.os.Environment
import com.example.database.DownloadDao
import com.example.database.DownloadEntity
import com.example.model.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadEngine(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val scope: CoroutineScope
) {
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Map of downloadId to active Coroutine Job
    private val activeJobs = ConcurrentHashMap<Long, Job>()

    // Moving average speed tracker
    private val speedMap = ConcurrentHashMap<Long, Long>()

    fun startDownload(downloadId: Long) {
        // Cancel existing job if running
        activeJobs[downloadId]?.cancel()

        val job = scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(downloadId) ?: return@launch
            downloadFile(entity)
        }
        activeJobs[downloadId] = job
    }

    fun pauseDownload(downloadId: Long) {
        val job = activeJobs.remove(downloadId)
        job?.cancel()
        scope.launch(Dispatchers.IO) {
            downloadDao.updateStatus(downloadId, DownloadStatus.PAUSED, null)
        }
    }

    fun resumeDownload(downloadId: Long) {
        startDownload(downloadId)
    }

    fun cancelDownload(downloadId: Long, deleteFile: Boolean = true) {
        val job = activeJobs.remove(downloadId)
        job?.cancel()
        scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(downloadId)
            if (entity != null && deleteFile) {
                try {
                    val file = File(entity.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (_: Exception) {}
            }
            downloadDao.updateStatus(downloadId, DownloadStatus.CANCELLED, null)
        }
    }

    fun isRunning(downloadId: Long): Boolean {
        return activeJobs[downloadId]?.isActive == true
    }

    private suspend fun downloadFile(entity: DownloadEntity) {
        val downloadId = entity.id
        val targetFile = File(entity.filePath)

        // Ensure parent directories exist
        targetFile.parentFile?.mkdirs()

        val existingBytes = if (targetFile.exists()) targetFile.length() else 0L

        downloadDao.updateStatus(downloadId, DownloadStatus.DOWNLOADING, null)

        val requestBuilder = Request.Builder()
            .url(entity.url)
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile; ActionDownloader/1.0)")

        // Support Range header for resuming partial downloads
        if (existingBytes > 0) {
            requestBuilder.header("Range", "bytes=$existingBytes-")
        }

        val request = requestBuilder.build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful && response.code != 206 && response.code != 200) {
                downloadDao.updateStatus(downloadId, DownloadStatus.FAILED, "HTTP ${response.code}: ${response.message}")
                activeJobs.remove(downloadId)
                return
            }

            val body = response.body
            if (body == null) {
                downloadDao.updateStatus(downloadId, DownloadStatus.FAILED, "استجابة الخادم فارغة")
                activeJobs.remove(downloadId)
                return
            }

            val isPartial = response.code == 206
            val contentLength = body.contentLength()
            val totalBytes = if (isPartial) {
                existingBytes + contentLength
            } else if (contentLength > 0) {
                contentLength
            } else {
                entity.totalBytes.coerceAtLeast(1024 * 1024L)
            }

            var currentDownloaded = if (isPartial) existingBytes else 0L
            val inputStream: InputStream = body.byteStream()
            val randomAccessFile = RandomAccessFile(targetFile, "rw")

            if (isPartial) {
                randomAccessFile.seek(existingBytes)
            } else {
                randomAccessFile.setLength(0)
                randomAccessFile.seek(0)
            }

            val buffer = ByteArray(32 * 1024) // 32KB buffer
            var bytesRead: Int
            var lastUpdateTime = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L
            var currentSpeedBps = 0L

            inputStream.use { input ->
                randomAccessFile.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        if (!scope.isActive) {
                            throw CancellationException("Download job cancelled or paused")
                        }

                        output.write(buffer, 0, bytesRead)
                        currentDownloaded += bytesRead
                        bytesSinceLastUpdate += bytesRead

                        val now = System.currentTimeMillis()
                        val deltaMs = now - lastUpdateTime
                        if (deltaMs >= 400) { // Update progress and speed every 400ms
                            currentSpeedBps = (bytesSinceLastUpdate * 1000L) / deltaMs.coerceAtLeast(1L)
                            speedMap[downloadId] = currentSpeedBps
                            bytesSinceLastUpdate = 0L
                            lastUpdateTime = now

                            downloadDao.updateProgress(
                                id = downloadId,
                                downloaded = currentDownloaded,
                                total = totalBytes,
                                speed = currentSpeedBps,
                                status = DownloadStatus.DOWNLOADING
                            )
                        }
                    }
                }
            }

            // Download completed successfully
            speedMap.remove(downloadId)
            activeJobs.remove(downloadId)
            downloadDao.updateProgress(
                id = downloadId,
                downloaded = currentDownloaded,
                total = currentDownloaded,
                speed = 0L,
                status = DownloadStatus.COMPLETED
            )
            downloadDao.markCompleted(downloadId, System.currentTimeMillis())

        } catch (e: CancellationException) {
            // Cancelled or paused
            activeJobs.remove(downloadId)
        } catch (e: Exception) {
            activeJobs.remove(downloadId)
            speedMap.remove(downloadId)
            downloadDao.updateStatus(downloadId, DownloadStatus.FAILED, e.localizedMessage ?: "حدث خطأ أثناء التحميل")
        }
    }

    companion object {
        fun getDownloadDirectory(context: Context): File {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "ActionDownloads")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(java.util.Locale.US, "%.2f GB", gb)
                mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(java.util.Locale.US, "%.1f KB", kb)
                else -> "$bytes B"
            }
        }

        fun formatSpeed(bytesPerSec: Long): String {
            if (bytesPerSec <= 0) return "0 KB/s"
            val kb = bytesPerSec / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format(java.util.Locale.US, "%.2f MB/s", mb)
                else -> String.format(java.util.Locale.US, "%.0f KB/s", kb)
            }
        }

        fun formatDuration(seconds: Long): String {
            if (seconds <= 0) return "--:--"
            val hrs = seconds / 3600
            val mins = (seconds % 3600) / 60
            val secs = seconds % 60
            return if (hrs > 0) {
                String.format(java.util.Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
            } else {
                String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
            }
        }

        fun calculateEtaSeconds(downloaded: Long, total: Long, speedBps: Long): Long {
            if (speedBps <= 0 || total <= downloaded) return 0L
            val remainingBytes = total - downloaded
            return remainingBytes / speedBps
        }
    }
}
