package com.seshat.writer.ui.theme

import android.content.Context

enum class ThemeMode(val displayName: String, val icon: String) {
    DARK("Dark Mode", "🌙"),
    LIGHT("Light Mode", "☀️"),
    SYSTEM("System Default", "⚙️")
}

object ThemePreferences {
    private const val PREFS_NAME = "seshat_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

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

}

