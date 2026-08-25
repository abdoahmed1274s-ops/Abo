package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.database.DownloadEntity
import com.example.ui.components.CinematicTopBar
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.screens.ActiveDownloadsScreen
import com.example.ui.screens.DownloadsLibraryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ActionDownloaderTheme
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaDarkElevated
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "الرئيسية", Icons.Default.Movie)
    object Active : Screen("active", "جاري التحميل", Icons.Default.Download)
    object Library : Screen("library", "المكتبة", Icons.Default.VideoLibrary)
    object Settings : Screen("settings", "الإعدادات", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isRtl by viewModel.isRtl.collectAsState()
            val userMessage by viewModel.userMessage.collectAsState()
            val activeDownloads by viewModel.activeDownloads.collectAsState()
            val playingMedia by viewModel.playingMedia.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

            LaunchedEffect(userMessage) {
                userMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearUserMessage()
                }
            }

            ActionDownloaderTheme(
                darkTheme = isDarkTheme,
                isRtl = isRtl
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CinematicTopBar(
                            activeDownloadsCount = activeDownloads.size,
                            onActiveClick = { currentScreen = Screen.Active },
                            onSettingsClick = { currentScreen = Screen.Settings }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CinemaDarkElevated,
                            contentColor = TextPrimary,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            val items = listOf(
                                Screen.Home,
                                Screen.Active,
                                Screen.Library,
                                Screen.Settings
                            )

                            items.forEach { screen ->
                                val isSelected = currentScreen.route == screen.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        if (screen == Screen.Active && activeDownloads.isNotEmpty()) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(
                                                        containerColor = CinemaRed,
                                                        contentColor = Color.White
                                                    ) {
                                                        Text("${activeDownloads.size}")
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.title
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CinemaDarkBg,
                                        selectedTextColor = CinemaGold,
                                        indicatorColor = CinemaGold,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_item_${screen.route}")
                                )
                            }
                        }
                    },
                    containerColor = CinemaDarkBg
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.Home -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToActive = { currentScreen = Screen.Active },
                                onNavigateToLibrary = { currentScreen = Screen.Library },
                                onShareEntity = { shareMediaFile(it) }
                            )
                            Screen.Active -> ActiveDownloadsScreen(
                                viewModel = viewModel,
                                onNavigateToHome = { currentScreen = Screen.Home }
                            )
                            Screen.Library -> DownloadsLibraryScreen(
                                viewModel = viewModel,
                                onShareEntity = { shareMediaFile(it) }
                            )
                            Screen.Settings -> SettingsScreen(
                                viewModel = viewModel
                            )
                        }

                        // Built-in Player Modal
                        if (playingMedia != null) {
                            VideoPlayerDialog(
                                mediaEntity = playingMedia,
                                onDismiss = { viewModel.closePlayer() },
                                onShare = {
                                    playingMedia?.let { shareMediaFile(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        extractAndAnalyzeUrl(sharedText)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val dataUri = intent.data
                if (dataUri != null) {
                    extractAndAnalyzeUrl(dataUri.toString())
                }
            }
        }
    }

    private fun extractAndAnalyzeUrl(text: String) {
        // Find URL in shared text
        val urlRegex = Regex("""(https?://[^\s]+)""")
        val match = urlRegex.find(text)
        val targetUrl = match?.value ?: text.trim()
        if (targetUrl.isNotBlank()) {
            viewModel.pasteFromClipboard(targetUrl)
            Toast.makeText(this, "تم استقبال الرابط: $targetUrl", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareMediaFile(entity: DownloadEntity) {
        try {
            val file = File(entity.filePath)
            if (!file.exists()) {
                Toast.makeText(this, "الملف غير موجود في الذاكرة", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = entity.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, entity.title)
                putExtra(Intent.EXTRA_TEXT, "تم التنزيل عبر Action Downloader: ${entity.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "مشاركة الملف بواسطة"))
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر مشاركة الملف: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
