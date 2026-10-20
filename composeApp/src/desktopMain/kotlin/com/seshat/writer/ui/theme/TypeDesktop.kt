package com.seshat.writer.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual val MainAppFontFamily: FontFamily = try {
    FontFamily(
        Font("font/roboto_thin.ttf", FontWeight.Thin),
        Font("font/roboto_light.ttf", FontWeight.Light),
        Font("font/roboto_regular.ttf", FontWeight.Normal),
        Font("font/roboto_medium.ttf", FontWeight.Medium),
        Font("font/roboto_bold.ttf", FontWeight.Bold)
    )
} catch (_: Throwable) {
    FontFamily.SansSerif
}
