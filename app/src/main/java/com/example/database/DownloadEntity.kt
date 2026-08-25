package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val thumbnailUrl: String?,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedBps: Long = 0,
    val status: DownloadStatus,
    val quality: String,
    val sourceDomain: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)
