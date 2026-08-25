package com.example.model

data class VideoFormatOption(
    val id: String,
    val qualityLabel: String,       // e.g. "1080p Full HD", "720p HD", "480p SD", "360p", "صوت MP3"
    val resolution: String,         // e.g. "1920x1080", "1280x720", "854x480"
    val formatExtension: String,    // e.g. "mp4", "mp3", "webm"
    val mimeType: String,           // e.g. "video/mp4", "audio/mpeg"
    val estimatedBytes: Long,       // Approximate or exact size
    val downloadUrl: String,        // Target stream or direct URL
    val isAudioOnly: Boolean = false
)
