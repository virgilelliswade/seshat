package com.seshat.writer

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.titlepage.TitlePageActivity
import com.seshat.writer.ui.FoldablePosture
import com.seshat.writer.ui.FoldableWindowState
import com.seshat.writer.ui.LocalFoldableWindowState
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
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val screenWidthDp = configuration.screenWidthDp
            val screenHeightDp = configuration.screenHeightDp

            val foldableState by produceState(
                initialValue = computeFallbackFoldableState(screenWidthDp, screenHeightDp),
                key1 = screenWidthDp,
                key2 = screenHeightDp
            ) {
                try {
                    WindowInfoTracker.getOrCreate(this@MainActivity)
                        .windowLayoutInfo(this@MainActivity)
                        .collect { layoutInfo ->
                            val foldingFeature = layoutInfo.displayFeatures
                                .filterIsInstance<FoldingFeature>()
                                .firstOrNull()
                            val aspect = if (screenHeightDp > 0) screenWidthDp.toFloat() / screenHeightDp.toFloat() else 1f
                            val isSquareInner = screenWidthDp >= 600 && aspect in 0.78f..1.28f
                            val isCover = screenWidthDp < 500 && (if (screenWidthDp > 0) screenHeightDp.toFloat() / screenWidthDp.toFloat() > 1.85f else false)

                            if (foldingFeature == null) {
                                value = FoldableWindowState(
                                    posture = if (isSquareInner) FoldablePosture.BOOK_UNFOLDED else FoldablePosture.NORMAL,
                                    isFoldableDevice = isSquareInner || isCover,
                                    isVerticalHinge = isSquareInner,
                                    isHorizontalHinge = false,
                                    hingeSizeDp = 0,
                                    hingeSplitRatio = 0.5f,
                                    screenWidthDp = screenWidthDp,
                                    screenHeightDp = screenHeightDp,
                                    isCoverScreen = isCover,
                                    isSquareInnerScreen = isSquareInner
                                )
                            } else {
                                val bounds = foldingFeature.bounds
                                val isVert = foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL
                                val isHoriz = foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
                                val isHalfOpened = foldingFeature.state == FoldingFeature.State.HALF_OPENED

                                val hingePx = if (isVert) bounds.width() else bounds.height()
                                val hingeDp = with(density) { hingePx.toDp().value.toInt() }.coerceAtLeast(0)

                                val totalPx = with(density) {
                                    if (isVert) screenWidthDp.dp.toPx() else screenHeightDp.dp.toPx()
                                }.coerceAtLeast(1f)
                                val hingeCenterPx = if (isVert) bounds.exactCenterX() else bounds.exactCenterY()
                                val splitRatio = (hingeCenterPx / totalPx).coerceIn(0.30f, 0.70f)

                                val posture = when {
                                    isHalfOpened && isHoriz -> FoldablePosture.TABLETOP_FLEX
                                    isHalfOpened && isVert -> FoldablePosture.BOOK_HALF_OPENED
                                    screenWidthDp >= 600 -> FoldablePosture.BOOK_UNFOLDED
                                    else -> FoldablePosture.NORMAL
                                }

                                value = FoldableWindowState(
                                    posture = posture,
                                    isFoldableDevice = true,
                                    isVerticalHinge = isVert,
                                    isHorizontalHinge = isHoriz,
                                    hingeSizeDp = hingeDp,
                                    hingeSplitRatio = splitRatio,
                                    screenWidthDp = screenWidthDp,
                                    screenHeightDp = screenHeightDp,
                                    isCoverScreen = isCover,
                                    isSquareInnerScreen = isSquareInner
                                )
                            }
                        }
                } catch (_: Throwable) {
                    value = computeFallbackFoldableState(screenWidthDp, screenHeightDp)
                }
            }

            CompositionLocalProvider(LocalFoldableWindowState provides foldableState) {
                SeshatTheme(darkTheme = isDark, studioPalette = uiState.studioPalette) {
                    ScreenplayEditorScreen(
                        viewModel = screenplayViewModel,
                        onOpenTitlePage = { openTitlePageWindow() }
                    )
                }
            }
        }
    }

    private fun computeFallbackFoldableState(screenWidthDp: Int, screenHeightDp: Int): FoldableWindowState {
        val aspect = if (screenHeightDp > 0) screenWidthDp.toFloat() / screenHeightDp.toFloat() else 1f
        val isSquareInner = screenWidthDp >= 600 && aspect in 0.78f..1.28f
        val isCover = screenWidthDp < 500 && (if (screenWidthDp > 0) screenHeightDp.toFloat() / screenWidthDp.toFloat() > 1.85f else false)
        return FoldableWindowState(
            posture = if (isSquareInner) FoldablePosture.BOOK_UNFOLDED else FoldablePosture.NORMAL,
            isFoldableDevice = isSquareInner,
            isVerticalHinge = isSquareInner,
            isHorizontalHinge = false,
            hingeSizeDp = 0,
            hingeSplitRatio = 0.5f,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            isCoverScreen = isCover,
            isSquareInnerScreen = isSquareInner
        )
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
