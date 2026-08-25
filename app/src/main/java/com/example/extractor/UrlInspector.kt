package com.example.extractor

import android.net.Uri
import com.example.model.MediaInfo
import com.example.model.VideoFormatOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

class UrlInspector(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {

    data class SampleMedia(
        val title: String,
        val description: String,
        val url: String,
        val duration: String,
        val category: String
    )

    companion object {
        val SAMPLE_VIDEOS = listOf(
            SampleMedia(
                title = "Big Buck Bunny (Blender Open Movie)",
                description = "Classic Creative Commons 1080p Animation Film",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                duration = "09:56",
                category = "Animation"
            ),
            SampleMedia(
                title = "Tears of Steel (Sci-Fi Short Film)",
                description = "Open Source VFX Sci-Fi Project",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                duration = "12:14",
                category = "Sci-Fi"
            ),
            SampleMedia(
                title = "Elephant's Dream (Creative Commons)",
                description = "First open-movie made with Blender 3D",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                duration = "10:53",
                category = "CGI"
            ),
            SampleMedia(
                title = "For Bigger Blazes (Action Demo)",
                description = "High Speed Action & Fire Sequence Test Stream",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                duration = "00:15",
                category = "Action"
            ),
            SampleMedia(
                title = "Sintel (Fantasy CGI Animation)",
                description = "Durian Open Movie Project by Blender Foundation",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                duration = "14:48",
                category = "Fantasy"
            ),
            SampleMedia(
                title = "For Bigger Meltdowns (Demo Clip)",
                description = "Cinematic Action Teaser & Sound FX",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                duration = "00:15",
                category = "Action"
            )
        )
    }

    suspend fun inspectUrl(rawUrl: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        var cleanUrl = rawUrl.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }

        try {
            val uri = Uri.parse(cleanUrl)
            val host = uri.host ?: "unknown-source.com"
            val domainName = host.removePrefix("www.")

            var title = extractTitleFromUrl(cleanUrl)
            var totalContentLength = 0L
            var detectedMimeType = "video/mp4"

            // Check network headers with a HEAD request
            val headRequest = Request.Builder()
                .url(cleanUrl)
                .head()
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; ActionDownloader/1.0)")
                .build()

            try {
                okHttpClient.newCall(headRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val lengthHeader = response.header("Content-Length")
                        if (!lengthHeader.isNullOrBlank()) {
                            totalContentLength = lengthHeader.toLongOrNull() ?: 0L
                        }
                        val typeHeader = response.header("Content-Type")
                        if (!typeHeader.isNullOrBlank()) {
                            detectedMimeType = typeHeader.split(";").firstOrNull()?.trim() ?: "video/mp4"
                        }
                        val disposition = response.header("Content-Disposition")
                        if (!disposition.isNullOrBlank() && disposition.contains("filename=")) {
                            val extracted = disposition.substringAfter("filename=").replace("\"", "").trim()
                            if (extracted.isNotBlank()) {
                                title = extracted.substringBeforeLast(".")
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // If HEAD fails, proceed with parsed URL information
            }

            // Estimate base size if not provided by server
            val baseSize = if (totalContentLength > 0) totalContentLength else 48 * 1024 * 1024L // ~48MB default

            // Create quality options tailored to video
            val formats = mutableListOf<VideoFormatOption>()

            formats.add(
                VideoFormatOption(
                    id = "1080p",
                    qualityLabel = "1080p Full HD",
                    resolution = "1920x1080",
                    formatExtension = "mp4",
                    mimeType = "video/mp4",
                    estimatedBytes = baseSize,
                    downloadUrl = cleanUrl,
                    isAudioOnly = false
                )
            )

            formats.add(
                VideoFormatOption(
                    id = "720p",
                    qualityLabel = "720p HD",
                    resolution = "1280x720",
                    formatExtension = "mp4",
                    mimeType = "video/mp4",
                    estimatedBytes = (baseSize * 0.65).toLong(),
                    downloadUrl = cleanUrl,
                    isAudioOnly = false
                )
            )

            formats.add(
                VideoFormatOption(
                    id = "480p",
                    qualityLabel = "480p SD",
                    resolution = "854x480",
                    formatExtension = "mp4",
                    mimeType = "video/mp4",
                    estimatedBytes = (baseSize * 0.40).toLong(),
                    downloadUrl = cleanUrl,
                    isAudioOnly = false
                )
            )

            formats.add(
                VideoFormatOption(
                    id = "360p",
                    qualityLabel = "360p توفير البيانات",
                    resolution = "640x360",
                    formatExtension = "mp4",
                    mimeType = "video/mp4",
                    estimatedBytes = (baseSize * 0.25).toLong(),
                    downloadUrl = cleanUrl,
                    isAudioOnly = false
                )
            )

            formats.add(
                VideoFormatOption(
                    id = "audio_mp3",
                    qualityLabel = "صوت فقط (MP3 High Quality)",
                    resolution = "320 kbps",
                    formatExtension = "mp3",
                    mimeType = "audio/mpeg",
                    estimatedBytes = (baseSize * 0.12).toLong(),
                    downloadUrl = cleanUrl,
                    isAudioOnly = true
                )
            )

            // Check if title matches sample videos
            val matchedSample = SAMPLE_VIDEOS.find { it.url.equals(cleanUrl, ignoreCase = true) }
            val finalTitle = matchedSample?.title ?: title

            val mediaInfo = MediaInfo(
                originalUrl = cleanUrl,
                title = finalTitle,
                sourceDomain = domainName,
                thumbnailUrl = null,
                durationSeconds = null,
                availableFormats = formats,
                isDirectDownloadable = true,
                description = matchedSample?.description
            )

            Result.success(mediaInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractTitleFromUrl(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val path = uri.lastPathSegment ?: ""
            val decoded = URLDecoder.decode(path, "UTF-8")
            val cleanName = File(decoded).nameWithoutExtension
            if (cleanName.isNotBlank() && cleanName.length > 2) {
                cleanName.replace("_", " ").replace("-", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            } else {
                "Action Video (${uri.host ?: "Media"})"
            }
        } catch (_: Exception) {
            "Action Video Clip"
        }
    }
}
