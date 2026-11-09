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
 * ScribeStar Crimson is the default palette, joined by Swiss Cobalt, Warm Atelier,
 * Nocturne Emerald, Astral Amethyst, and Monolith Titanium.
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
        subtitle = "Precision Graphite & Laser Crimson (Default)",
        icon = "🔴",
        darkAccent = Color(0xFFE11D48),
        lightAccent = Color(0xFFBE123C),
        secondaryAccent = Color(0xFFF59E0B),
        darkBg = Color(0xFF0B0D13),
        darkSurface = Color(0xFF131620),
        darkBorder = Color(0xFF232838),
        lightBg = Color(0xFFF1F5F9),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFCBD5E1)
    ),
    SWISS_COBALT(
        displayName = "Swiss Cobalt",
        subtitle = "Bauhaus Grid & International Cobalt Blue",
        icon = "🔵",
        darkAccent = Color(0xFF3B82F6),
        lightAccent = Color(0xFF1D4ED8),
        secondaryAccent = Color(0xFF0EA5E9),
        darkBg = Color(0xFF09090B),
        darkSurface = Color(0xFF121216),
        darkBorder = Color(0xFF27272A),
        lightBg = Color(0xFFF8FAFC),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFD4D4D8)
    ),
    WARM_ATELIER(
        displayName = "Warm Atelier",
        subtitle = "Nordic Charcoal, Stone & Champagne Brass",
        icon = "🟡",
        darkAccent = Color(0xFFD97706),
        lightAccent = Color(0xFFB45309),
        secondaryAccent = Color(0xFF10B981),
        darkBg = Color(0xFF10100E),
        darkSurface = Color(0xFF181815),
        darkBorder = Color(0xFF2A2925),
        lightBg = Color(0xFFF5F4F0),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFD6D3CD),
        lightDesk = Color(0xFFD6D3CD)
    ),
    NOCTURNE_EMERALD(
        displayName = "Nocturne Emerald",
        subtitle = "Abyssal Pine & Surgical Jade",
        icon = "🟢",
        darkAccent = Color(0xFF10B981),
        lightAccent = Color(0xFF047857),
        secondaryAccent = Color(0xFF14B8A6),
        darkBg = Color(0xFF080D0B),
        darkSurface = Color(0xFF101915),
        darkBorder = Color(0xFF1E2F28),
        lightBg = Color(0xFFF0FDF4),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFCBD5E1)
    ),
    ASTRAL_VIOLET(
        displayName = "Astral Amethyst",
        subtitle = "Deep Space Void & Electric Violet",
        icon = "🟣",
        darkAccent = Color(0xFF8B5CF6),
        lightAccent = Color(0xFF6D28D9),
        secondaryAccent = Color(0xFFEC4899),
        darkBg = Color(0xFF0A0912),
        darkSurface = Color(0xFF131120),
        darkBorder = Color(0xFF25203B),
        lightBg = Color(0xFFF5F3FF),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFDDD6FE),
        lightDesk = Color(0xFFCBD5E1)
    ),
    MONOLITH_TITANIUM(
        displayName = "Monolith Titanium",
        subtitle = "Anodized Gunmetal & Ice Cyan",
        icon = "⚪",
        darkAccent = Color(0xFF38BDF8),
        lightAccent = Color(0xFF0284C7),
        secondaryAccent = Color(0xFF94A3B8),
        darkBg = Color(0xFF090B0E),
        darkSurface = Color(0xFF12161C),
        darkBorder = Color(0xFF212832),
        lightBg = Color(0xFFF1F5F9),
        lightSurface = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFCBD5E1),
        lightDesk = Color(0xFFCBD5E1)
    )
}

val ActiveStudioPaletteState = mutableStateOf(StudioColorPalette.SCRIBESTAR_CRIMSON)
val ActiveDarkThemeState = mutableStateOf(true)

object ThemePreferences {
    private const val PREFS_NAME = "seshat_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_STUDIO_PALETTE = "studio_palette"

    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        return try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }

    fun getStudioPalette(context: Context): StudioColorPalette {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val palStr = prefs.getString(KEY_STUDIO_PALETTE, StudioColorPalette.SCRIBESTAR_CRIMSON.name)
            ?: StudioColorPalette.SCRIBESTAR_CRIMSON.name
        val resolved = try {
            StudioColorPalette.valueOf(palStr)
        } catch (e: Exception) {
            StudioColorPalette.SCRIBESTAR_CRIMSON
        }
        ActiveStudioPaletteState.value = resolved
        return resolved
    }

    fun setStudioPalette(context: Context, palette: StudioColorPalette) {
        ActiveStudioPaletteState.value = palette
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STUDIO_PALETTE, palette.name)
            .apply()
    }
}
