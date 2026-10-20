package com.seshat.writer.titlepage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import com.seshat.writer.ui.theme.SeshatTheme
import com.seshat.writer.ui.theme.ThemeMode
import com.seshat.writer.ui.theme.ThemePreferences

/**
 * Independent desktop multi-window activity for the Screenplay Title Page Creator.
 * Supports concurrent freeform windowing on ChromeOS / Googlebook desktop mode.
 */
class TitlePageActivity : ComponentActivity() {

    private val titlePageViewModel: TitlePageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode = ThemePreferences.getThemeMode(this)
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            SeshatTheme(darkTheme = isDark) {
                TitlePageScreen(
                    viewModel = titlePageViewModel,
                    onCloseWindow = { finish() }
                )
            }
        }
    }
}
