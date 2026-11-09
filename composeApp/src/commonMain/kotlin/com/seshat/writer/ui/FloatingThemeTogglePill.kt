package com.seshat.writer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Light/Dark Mode and Studio Theme Palette selection have been moved into
 * StudioSettingsDialog ("⚙️ Settings") for a clean, distraction-free studio interface.
 */
@Composable
fun FloatingThemeTogglePill(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    // Intentionally empty: Light/Dark mode and Studio Color Palettes are managed in Settings.
}
