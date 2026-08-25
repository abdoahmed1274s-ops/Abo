package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.downloader.DownloadEngine
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaDarkCard
import com.example.ui.theme.CinemaDarkElevated
import com.example.ui.theme.CinemaDarkOutline
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldDark
import com.example.ui.theme.CinemaGreen
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isRtl by viewModel.isRtl.collectAsState()
    val isWifiOnly by viewModel.isWifiOnly.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val defaultQuality by viewModel.defaultQuality.collectAsState()

    var showQualityDropdown by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val downloadFolder = remember {
        DownloadEngine.getDownloadDirectory(context).absolutePath
    }

    val qualityOptions = listOf(
        "1080p" to "1080p Full HD (أعلى جودة)",
        "720p" to "720p HD (متوسطة متوازنة)",
        "480p" to "480p SD (توفير البيانات)",
        "360p" to "360p (سريع ومنخفض)",
        "audio_mp3" to "صوت فقط MP3"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Header
        Text(
            text = "الإعدادات والخيارات",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = CinemaGold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Storage Location Card
            item {
                SettingsCard(
                    icon = Icons.Default.Folder,
                    iconTint = CinemaCyan,
                    title = "مجلد حفظ التنزيلات",
                    subtitle = downloadFolder
                )
            }

            // Default Quality Selector Card
            item {
                Box {
                    SettingsCard(
                        icon = Icons.Default.HighQuality,
                        iconTint = CinemaGold,
                        title = "الجودة الافتراضية للتحميل",
                        subtitle = qualityOptions.find { it.first == defaultQuality }?.second ?: defaultQuality,
                        onClick = { showQualityDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showQualityDropdown,
                        onDismissRequest = { showQualityDropdown = false },
                        modifier = Modifier
                            .background(CinemaDarkElevated)
                            .border(1.dp, CinemaDarkOutline, RoundedCornerShape(8.dp))
                    ) {
                        qualityOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (defaultQuality == key) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = CinemaGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = label,
                                            color = if (defaultQuality == key) CinemaGold else TextPrimary,
                                            fontWeight = if (defaultQuality == key) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.updateDefaultQuality(key)
                                    showQualityDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Wi-Fi Only Switch
            item {
                SettingsSwitchCard(
                    icon = Icons.Default.Wifi,
                    iconTint = CinemaCyan,
                    title = "التحميل عبر Wi-Fi فقط",
                    subtitle = "تجنب استهلاك باقة الإنترنت الخلوي أثناء التنزيل",
                    checked = isWifiOnly,
                    onCheckedChange = { viewModel.toggleWifiOnly(it) }
                )
            }

            // Notifications Switch
            item {
                SettingsSwitchCard(
                    icon = Icons.Default.Notifications,
                    iconTint = CinemaGold,
                    title = "إشعارات التنزيل",
                    subtitle = "تنبيه عند بدء واكتمال التحميلات",
                    checked = isNotificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
            }

            // Dark Theme Switch
            item {
                SettingsSwitchCard(
                    icon = Icons.Default.Brightness4,
                    iconTint = CinemaGold,
                    title = "المظهر السينمائي الداكن",
                    subtitle = "الوضع الليلي الاحترافي عالي التباين",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme(it) }
                )
            }

            // RTL Interface Switch
            item {
                SettingsSwitchCard(
                    icon = Icons.Default.Language,
                    iconTint = CinemaCyan,
                    title = "الواجهة باللغة العربية (RTL)",
                    subtitle = "محاذاة كاملة للغة العربية من اليمين لليسار",
                    checked = isRtl,
                    onCheckedChange = { viewModel.toggleRtl(it) }
                )
            }

            // Clear History Button
            item {
                SettingsCard(
                    icon = Icons.Default.DeleteSweep,
                    iconTint = CinemaRed,
                    title = "مسح سجل التنزيلات",
                    subtitle = "حذف السجلات المحفوظة في قاعدة البيانات",
                    onClick = { showClearHistoryDialog = true }
                )
            }

            // Fair Use & Legal Disclaimer Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaDarkCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CinemaDarkOutline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = CinemaGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إخلاء المسؤولية والاستخدام العادل",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "أكشن داونلودر مصمم لتحميل الوسائط المصرح بها قانونياً أو التابعة للملك العام (Public Domain / Creative Commons) أو التي يمتلك المستخدم ترخيصاً رسمياً لتنزيلها. لا يقوم التطبيق باختراق أي حماية رقمية (DRM) أو تجاوز جدران الدفع والاشتراكات. تقع المسؤولية الكاملة على عاتق المستخدم في احترام حقوق الملكية الفكرية.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            // About Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaDarkCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CinemaDarkOutline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CinemaGold)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = CinemaDarkBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Action Downloader",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "الإصدار 1.0.0 (Cinematic Release)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "• بدون تسجيل حساب أو جمع بيانات\n• تخزين محلي آمن 100% على جهازك\n• دعم استئناف التحميل المتقطع (HTTP Range)",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = CinemaDarkCard,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = CinemaRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "مسح سجل التنزيلات",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "هل ترغب في مسح سجل التنزيلات من التطبيق؟ لن يؤثر ذلك على الملفات المحفوظة فعلياً في ذاكرة جهازك.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaRed,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("مسح السجل")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearHistoryDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaDarkCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CinemaDarkOutline, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CinemaDarkElevated)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaDarkCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CinemaDarkOutline, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CinemaDarkElevated)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CinemaGold,
                    checkedTrackColor = CinemaDarkElevated,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = CinemaDarkElevated
                )
            )
        }
    }
}
