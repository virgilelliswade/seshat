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
}
