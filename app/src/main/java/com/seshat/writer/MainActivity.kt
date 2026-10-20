package com.seshat.writer

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.titlepage.TitlePageActivity
import com.seshat.writer.ui.ScreenplayEditorScreen
import com.seshat.writer.ui.theme.SeshatTheme
import com.seshat.writer.ui.theme.ThemeMode

import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.ScribeStarNeonRed

class MainActivity : ComponentActivity() {

    private val screenplayViewModel: ScreenplayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("SeshatCrash", "FATAL UNCAUGHT EXCEPTION on thread ${thread.name}", throwable)
            try {
                val logText = "TIME: ${System.currentTimeMillis()}\nTHREAD: ${thread.name}\n\n${throwable.stackTraceToString()}\n"
                try {
                    val crashDoc = File("/storage/emulated/10/Documents/Writing App/crash.log")
                    crashDoc.writeText(logText)
                } catch (_: Throwable) {}
                try {
                    val crashFile = File(getExternalFilesDir(null), "crash.log")
                    crashFile.writeText(logText)
                } catch (_: Throwable) {}
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()
        setContent {
            val uiState by screenplayViewModel.uiState.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemDark
            }
            SeshatTheme(darkTheme = isDark) {
                ScreenplayEditorScreen(
                    viewModel = screenplayViewModel,
                    onOpenTitlePage = { openTitlePageWindow() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        screenplayViewModel.syncTitlePage()
    }

    private fun openTitlePageWindow() {
        val intent = Intent(this, TitlePageActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }
        val options = ActivityOptions.makeBasic()
        startActivity(intent, options.toBundle())
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOICE_ASSIST) {
            screenplayViewModel.toggleDictation(this)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
