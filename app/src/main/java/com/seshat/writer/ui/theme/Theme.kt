package com.seshat.writer.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(4.dp)
)

@Composable
fun SeshatTheme(
    darkTheme: Boolean = true,
    studioPalette: StudioColorPalette = ActiveStudioPaletteState.value,
    content: @Composable () -> Unit
) {
    ActiveDarkThemeState.value = darkTheme
    ActiveStudioPaletteState.value = studioPalette

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = studioPalette.darkAccent,
            onPrimary = Color.White,
            primaryContainer = studioPalette.lightAccent.copy(alpha = 0.65f),
            onPrimaryContainer = studioPalette.secondaryAccent,
            secondary = ElectricWhite,
            onSecondary = studioPalette.darkBg,
            secondaryContainer = studioPalette.darkSurface,
            onSecondaryContainer = ElectricWhite,
            tertiary = studioPalette.secondaryAccent,
            onTertiary = studioPalette.darkBg,
            background = studioPalette.darkBg,
            onBackground = RawScreenplayTextDark,
            surface = studioPalette.darkSurface,
            onSurface = RawScreenplayTextDark,
            surfaceVariant = studioPalette.darkBorder,
            onSurfaceVariant = RawScreenplayTextMuted
        )
    } else {
        lightColorScheme(
            primary = studioPalette.lightAccent,
            onPrimary = Color.White,
            primaryContainer = studioPalette.lightAccent.copy(alpha = 0.14f),
            onPrimaryContainer = studioPalette.lightAccent,
            secondary = Color(0xFF0F172A),
            onSecondary = Color.White,
            secondaryContainer = studioPalette.lightBorder,
            onSecondaryContainer = Color(0xFF0F172A),
            tertiary = studioPalette.secondaryAccent,
            onTertiary = Color.White,
            background = studioPalette.lightBg,
            onBackground = RawScreenplayTextLight,
            surface = studioPalette.lightSurface,
            onSurface = RawScreenplayTextLight,
            surfaceVariant = studioPalette.lightBorder,
            onSurfaceVariant = RawScreenplayTextMutedLight
        )
    }

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

    CompositionLocalProvider(
        LocalSeshatDarkTheme provides darkTheme,
        LocalStudioPalette provides studioPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = StudioShapes,
            typography = Typography,
            content = content
        )
    }
}
