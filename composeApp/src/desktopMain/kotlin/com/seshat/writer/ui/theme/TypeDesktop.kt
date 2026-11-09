package com.seshat.writer.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual val MainAppFontFamily: FontFamily = try {
    FontFamily(
        Font("font/plus_jakarta_sans_regular.ttf", FontWeight.Light),
        Font("font/plus_jakarta_sans_regular.ttf", FontWeight.Normal),
        Font("font/plus_jakarta_sans_medium.ttf", FontWeight.Medium),
        Font("font/plus_jakarta_sans_semibold.ttf", FontWeight.SemiBold),
        Font("font/plus_jakarta_sans_bold.ttf", FontWeight.Bold),
        Font("font/plus_jakarta_sans_extrabold.ttf", FontWeight.ExtraBold),
        Font("font/plus_jakarta_sans_extrabold.ttf", FontWeight.Black)
    )
} catch (_: Throwable) {
    FontFamily.SansSerif
}
