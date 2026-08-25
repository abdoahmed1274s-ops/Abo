package com.example.repository

import android.content.Context
import com.example.database.AppDatabase
import com.example.database.DownloadDao
import com.example.database.DownloadEntity
import com.example.downloader.DownloadEngine
import com.example.extractor.UrlInspector
import com.example.model.DownloadStatus
import com.example.model.MediaInfo
import com.example.model.VideoFormatOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DownloadRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val downloadDao: DownloadDao = database.downloadDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadEngine = DownloadEngine(context, downloadDao, scope)
    private val urlInspector = UrlInspector()

    fun getAllDownloads(): Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    fun getActiveDownloads(): Flow<List<DownloadEntity>> = downloadDao.getActiveDownloads()

    fun getCompletedDownloads(): Flow<List<DownloadEntity>> = downloadDao.getCompletedDownloads()

    suspend fun inspectUrl(url: String): Result<MediaInfo> {
        return urlInspector.inspectUrl(url)
    }

    suspend fun enqueueDownload(
        mediaInfo: MediaInfo,
        selectedFormat: VideoFormatOption
    ): Long = withContext(Dispatchers.IO) {
        val downloadDir = DownloadEngine.getDownloadDirectory(context)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val sanitizedTitle = mediaInfo.title
            .replace(Regex("[^a-zA-Z0-9_\\-\\s\u0600-\u06FF]"), "")
            .trim()
            .take(40)
            .ifBlank { "Action_Video" }

        val fileName = "${sanitizedTitle}_${selectedFormat.id}_$timeStamp.${selectedFormat.formatExtension}"
        val targetFile = File(downloadDir, fileName)

        val entity = DownloadEntity(
            url = selectedFormat.downloadUrl,
            title = mediaInfo.title,
            fileName = fileName,
            filePath = targetFile.absolutePath,
            mimeType = selectedFormat.mimeType,
            thumbnailUrl = mediaInfo.thumbnailUrl,
            totalBytes = selectedFormat.estimatedBytes,
            downloadedBytes = 0L,
            speedBps = 0L,
            status = DownloadStatus.QUEUED,
            quality = selectedFormat.qualityLabel,
            sourceDomain = mediaInfo.sourceDomain,
            createdAt = System.currentTimeMillis()
        )

        val id = downloadDao.insert(entity)
        downloadEngine.startDownload(id)
        id
    }

    fun pauseDownload(id: Long) {
        downloadEngine.pauseDownload(id)
    }

    fun resumeDownload(id: Long) {
        downloadEngine.resumeDownload(id)
    }

    fun cancelDownload(id: Long) {
        downloadEngine.cancelDownload(id, deleteFile = true)
    }

    suspend fun deleteDownload(id: Long, deleteFileFromStorage: Boolean = true) = withContext(Dispatchers.IO) {
        val entity = downloadDao.getDownloadById(id)
        if (entity != null) {
            downloadEngine.cancelDownload(id, deleteFile = false)
            if (deleteFileFromStorage) {
                try {
                    val file = File(entity.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (_: Exception) {}
            }
            downloadDao.deleteById(id)
        }
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        downloadDao.clearAll()
    }
}
