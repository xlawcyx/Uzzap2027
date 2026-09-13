package com.example.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

internal val LightForestColorScheme = lightColorScheme(
    primary = UzzapOrange,
    onPrimary = Color.White,
    primaryContainer = UzzapOrangeContainer,
    onPrimaryContainer = UzzapOrangeDark,
    secondary = ForestLeaf,
    onSecondary = TextPrimary,
    secondaryContainer = UzzapCyanContainer,
    onSecondaryContainer = ForestGreenDark,
    tertiary = UzzapOrangeDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3F7EF),
    onTertiaryContainer = ForestGreenDark,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = Color(0xFFD9E4D5)
)

internal val DarkForestColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF173522),
    onPrimaryContainer = Color(0xFFD9F0D2),
    secondary = ForestLeaf,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF203624),
    onSecondaryContainer = Color(0xFFD9F0D2),
    tertiary = Color(0xFFA4CF8B),
    onTertiary = TextPrimary,
    tertiaryContainer = Color(0xFF263C2A),
    onTertiaryContainer = Color(0xFFE2F3DC),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF34483A)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Edge stretching can paint scrollable content over fixed app chrome. Keep scrolling
    // bounded so headers, navigation, and composers remain visually anchored.
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkForestColorScheme else LightForestColorScheme,
            typography = Typography,
            content = content
        )
    }
}
