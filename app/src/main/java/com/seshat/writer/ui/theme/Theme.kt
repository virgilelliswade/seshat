package com.seshat.writer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ScribeStarNeonRed,
    onPrimary = RawObsidianBlack,
    primaryContainer = RubyCoreDark,
    onPrimaryContainer = NeonCoralLight,
    secondary = ElectricWhite,
    onSecondary = RawObsidianBlack,
    secondaryContainer = CircuitSlate,
    onSecondaryContainer = ElectricWhite,
    tertiary = CircuitCyan,
    onTertiary = RawObsidianBlack,
    background = RawObsidianBlack,
    onBackground = RawScreenplayTextDark,
    surface = RawObsidianSurface,
    onSurface = RawScreenplayTextDark,
    surfaceVariant = RawObsidianSurfaceVariant,
    onSurfaceVariant = RawScreenplayTextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = ScribeStarCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFF881337),
    secondary = Color(0xFF0F172A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = RawLightBackground,
    onBackground = RawScreenplayTextLight,
    surface = RawLightSurface,
    onSurface = RawScreenplayTextLight,
    surfaceVariant = RawLightSurfaceVariant,
    onSurfaceVariant = RawScreenplayTextMutedLight
)

@Composable
fun SeshatTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalSeshatDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
