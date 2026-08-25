package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.DownloadEntity
import com.example.downloader.DownloadEngine
import com.example.model.DownloadStatus
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaDarkCard
import com.example.ui.theme.CinemaDarkElevated
import com.example.ui.theme.CinemaDarkOutline
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldDark
import com.example.ui.theme.CinemaGreen
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.CinemaRedContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActiveDownloadCard(
    download: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBytes = download.totalBytes.coerceAtLeast(1L)
    val downloadedBytes = download.downloadedBytes.coerceAtMost(totalBytes)
    val progress = (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val percentage = (progress * 100).toInt()

    val etaSeconds = DownloadEngine.calculateEtaSeconds(
        downloaded = downloadedBytes,
        total = totalBytes,
        speedBps = download.speedBps
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaDarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CinemaDarkOutline, RoundedCornerShape(16.dp))
            .testTag("active_download_card_${download.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Clapperboard Icon, Title, and Cancel button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CinemaDarkElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CinemaGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = download.quality,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = CinemaGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = download.sourceDomain,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("cancel_download_btn_${download.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Download",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when (download.status) {
                    DownloadStatus.PAUSED -> CinemaCyan
                    DownloadStatus.FAILED -> CinemaRed
                    DownloadStatus.COMPLETED -> CinemaGreen
                    else -> CinemaGold
                },
                trackColor = CinemaDarkElevated
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row: Downloaded/Total, Percentage, Speed, ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${DownloadEngine.formatBytes(downloadedBytes)} / ${DownloadEngine.formatBytes(totalBytes)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (download.status) {
                        DownloadStatus.PAUSED -> CinemaCyan
                        DownloadStatus.FAILED -> CinemaRed
                        DownloadStatus.COMPLETED -> CinemaGreen
                        else -> CinemaGold
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Live Speed, Status, and Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed & ETA
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (download.status == DownloadStatus.DOWNLOADING) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CinemaCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = DownloadEngine.formatSpeed(download.speedBps),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CinemaCyan
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = DownloadEngine.formatDuration(etaSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    } else {
                        val (statusText, statusColor) = when (download.status) {
                            DownloadStatus.PAUSED -> "متوقف مؤقتاً" to CinemaCyan
                            DownloadStatus.FAILED -> (download.errorMessage ?: "فشل التحميل") to CinemaRed
                            DownloadStatus.QUEUED -> "في الانتظار..." to CinemaGold
                            DownloadStatus.COMPLETED -> "اكتمل التحميل" to CinemaGreen
                            DownloadStatus.CANCELLED -> "تم الإلغاء" to TextMuted
                            else -> "جاري التحميل" to CinemaGold
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = statusColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action button (Pause / Resume / Retry)
                Row {
                    when (download.status) {
                        DownloadStatus.DOWNLOADING -> {
                            FilledTonalButton(
                                onClick = onPause,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CinemaDarkElevated,
                                    contentColor = CinemaGold
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("pause_btn_${download.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "إيقاف",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            FilledTonalButton(
                                onClick = onResume,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CinemaGold,
                                    contentColor = CinemaDarkCard
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("resume_btn_${download.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "استئناف",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        DownloadStatus.FAILED -> {
                            FilledTonalButton(
                                onClick = onRetry,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CinemaRedContainer,
                                    contentColor = CinemaRed
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("retry_btn_${download.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "إعادة المحاولة",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
