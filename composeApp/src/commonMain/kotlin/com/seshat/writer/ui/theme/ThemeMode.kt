package com.seshat.writer.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val displayName: String, val icon: String) {
    DARK("Dark Mode", "🌙"),
    LIGHT("Light Mode", "☀️"),
    SYSTEM("System Default", "⚙️")
}

/**
 * Curated studio color palettes for Seshat.
 * ScribeStar Crimson is the default palette, joined by Obsidian & Ivory (Black & White),
 * Swiss Cobalt, Warm Atelier, Nocturne Emerald, Astral Amethyst, and Monolith Titanium.
 */
enum class StudioColorPalette(
    val displayName: String,
    val subtitle: String,
    val icon: String,
    val darkAccent: Color,
    val lightAccent: Color,
    val secondaryAccent: Color,
    val darkBg: Color,
    val darkSurface: Color,
    val darkBorder: Color,
    val lightBg: Color,
    val lightSurface: Color,
    val lightBorder: Color,
    val lightDesk: Color
) {
    SCRIBESTAR_CRIMSON(
        displayName = "ScribeStar Crimson",
        subtitle = "Smoked Obsidian Glass & Laser Crimson (Default)",
        icon = "🔴",
        darkAccent = Color(0xFFFF1E42),
        lightAccent = Color(0xFFBE123C),
        secondaryAccent = Color(0xFFF59E0B),
        darkBg = Color(0xFF06080D),
        darkSurface = Color(0xFF101521),
        darkBorder = Color(0xFF263044),
        lightBg = Color(0xFFF1F5F9),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFDCE4F0)
    ),
    MONOCHROME_STARK(
        displayName = "Obsidian & Ivory",
        subtitle = "Pure Black & White Monochrome Glass",
        icon = "◐",
        darkAccent = Color(0xFFF8FAFC),
        lightAccent = Color(0xFF0F172A),
        secondaryAccent = Color(0xFF94A3B8),
        darkBg = Color(0xFF050507),
        darkSurface = Color(0xFF111216),
        darkBorder = Color(0xFF2E3038),
        lightBg = Color(0xFFF8FAFC),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFE2E8F0)
    ),
    SWISS_COBALT(
        displayName = "Swiss Cobalt",
        subtitle = "Bauhaus Grid & International Cobalt Blue",
        icon = "🔵",
        darkAccent = Color(0xFF3B82F6),
        lightAccent = Color(0xFF1D4ED8),
        secondaryAccent = Color(0xFF0EA5E9),
        darkBg = Color(0xFF06080E),
        darkSurface = Color(0xFF101522),
        darkBorder = Color(0xFF242F46),
        lightBg = Color(0xFFF8FAFC),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFDCE4F0)
    ),
    WARM_ATELIER(
        displayName = "Warm Atelier",
        subtitle = "Smoked Onyx & Champagne Gold",
        icon = "🟡",
        darkAccent = Color(0xFFF59E0B),
        lightAccent = Color(0xFFB45309),
        secondaryAccent = Color(0xFF10B981),
        darkBg = Color(0xFF0A0908),
        darkSurface = Color(0xFF171512),
        darkBorder = Color(0xFF302B24),
        lightBg = Color(0xFFF5F4F0),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFD6D3CD),
        lightDesk = Color(0xFFE5E2DC)
    ),
    NOCTURNE_EMERALD(
        displayName = "Nocturne Emerald",
        subtitle = "Abyssal Matrix & Surgical Jade",
        icon = "🟢",
        darkAccent = Color(0xFF10B981),
        lightAccent = Color(0xFF047857),
        secondaryAccent = Color(0xFF14B8A6),
        darkBg = Color(0xFF050A08),
        darkSurface = Color(0xFF0E1915),
        darkBorder = Color(0xFF1F352C),
        lightBg = Color(0xFFF0FDF4),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFDCECE3)
    ),
    ASTRAL_VIOLET(
        displayName = "Astral Amethyst",
        subtitle = "Deep Space Glass & Electric Violet",
        icon = "🟣",
        darkAccent = Color(0xFFA855F7),
        lightAccent = Color(0xFF6D28D9),
        secondaryAccent = Color(0xFFEC4899),
        darkBg = Color(0xFF08060E),
        darkSurface = Color(0xFF141022),
        darkBorder = Color(0xFF2B2244),
        lightBg = Color(0xFFF5F3FF),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFDDD6FE),
        lightDesk = Color(0xFFE4E0F5)
    ),
    MONOLITH_TITANIUM(
        displayName = "Cyber Cyan",
        subtitle = "Anodized Gunmetal & Ice Cyan",
        icon = "⚪",
        darkAccent = Color(0xFF06B6D4),
        lightAccent = Color(0xFF0284C7),
        secondaryAccent = Color(0xFF94A3B8),
        darkBg = Color(0xFF05080C),
        darkSurface = Color(0xFF0F1620),
        darkBorder = Color(0xFF213042),
        lightBg = Color(0xFFF1F5F9),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFDCE6F0)
    )
}

val ActiveStudioPaletteState = mutableStateOf(StudioColorPalette.SCRIBESTAR_CRIMSON)
val ActiveDarkThemeState = mutableStateOf(true)

object ThemePreferences {
    private const val PREFS_NAME = "seshat_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_STUDIO_PALETTE = "studio_palette"

    fun getThemeMode(context: Context? = null): ThemeMode {
        val modeStr = com.seshat.writer.platform.AppPlatform.storage.getString(KEY_THEME_MODE, ThemeMode.DARK.name)
        return try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(context: Context? = null, mode: ThemeMode) {
        com.seshat.writer.platform.AppPlatform.storage.putString(KEY_THEME_MODE, mode.name)
    }

    fun getStudioPalette(context: Context? = null): StudioColorPalette {
        val palStr = com.seshat.writer.platform.AppPlatform.storage.getString(KEY_STUDIO_PALETTE, StudioColorPalette.SCRIBESTAR_CRIMSON.name)
        val resolved = try {
            StudioColorPalette.valueOf(palStr)
        } catch (e: Exception) {
            StudioColorPalette.SCRIBESTAR_CRIMSON
        }
        ActiveStudioPaletteState.value = resolved
        return resolved
    }

    fun setStudioPalette(context: Context? = null, palette: StudioColorPalette) {
        ActiveStudioPaletteState.value = palette
        com.seshat.writer.platform.AppPlatform.storage.putString(KEY_STUDIO_PALETTE, palette.name)
    }
}
