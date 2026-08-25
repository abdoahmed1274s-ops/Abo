package com.example.model

data class MediaInfo(
    val originalUrl: String,
    val title: String,
    val sourceDomain: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Long? = null,
    val availableFormats: List<VideoFormatOption> = emptyList(),
    val isDirectDownloadable: Boolean = true,
    val description: String? = null
)
