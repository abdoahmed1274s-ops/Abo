package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaDarkOutline
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaPrimaryOn
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinematicTopBar(
    activeDownloadsCount: Int = 0,
    onActiveClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = CinemaDarkBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Elegant Dark Logo badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CinemaGold)
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Action Downloader Logo",
                    tint = CinemaPrimaryOn,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Title
            Text(
                text = "أكشن داونلودر",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = CinemaGold,
                modifier = Modifier.weight(1f)
            )

            // Active Downloads indicator with Badge
            BadgedBox(
                badge = {
                    if (activeDownloadsCount > 0) {
                        Badge(
                            containerColor = CinemaRed,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("active_downloads_badge")
                        ) {
                            Text(
                                text = "$activeDownloadsCount",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = onActiveClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = CinemaDarkOutline,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .testTag("topbar_active_downloads_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Active Downloads",
                        tint = if (activeDownloadsCount > 0) CinemaGold else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings button
            IconButton(
                onClick = onSettingsClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = CinemaDarkOutline,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .testTag("topbar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
