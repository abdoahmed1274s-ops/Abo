package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val ElegantDarkColorScheme = darkColorScheme(
    primary = CinemaGold, // #D1E4FF
    onPrimary = CinemaPrimaryOn, // #003258
    primaryContainer = CinemaGoldDark, // #004A77
    onPrimaryContainer = CinemaGold, // #D1E4FF
    secondary = CinemaCyan,
    onSecondary = CinemaPrimaryOn,
    secondaryContainer = CinemaDarkElevated,
    onSecondaryContainer = CinemaGold,
    tertiary = CinemaRed,
    onTertiary = Color.White,
    tertiaryContainer = CinemaRedContainer,
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = CinemaDarkBg, // #1A1C1E
    onBackground = TextPrimary, // #E2E2E6
    surface = CinemaDarkSurface, // #2E3033
    onSurface = TextPrimary,
    surfaceVariant = CinemaDarkElevated, // #282A2D
    onSurfaceVariant = TextSecondary, // #C2C7CF
    outline = CinemaDarkOutline, // #42474E
    outlineVariant = CinemaDarkOutlineVariant,
    error = CinemaRed,
    onError = Color.White
)

private val ElegantLightColorScheme = lightColorScheme(
    primary = Color(0xFF00639A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D32),
    secondary = Color(0xFF51606F),
    onSecondary = Color.White,
    background = Color(0xFFFCFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF1F4F9),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF72777F)
)

@Composable
fun ActionDownloaderTheme(
    darkTheme: Boolean = true,
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
