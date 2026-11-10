package com.seshat.writer.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual val CourierPrimeFamily: FontFamily = try {
    FontFamily(
        Font("font/courier_prime_regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("font/courier_prime_bold.ttf", FontWeight.Bold, FontStyle.Normal),
        Font("font/courier_prime_italic.ttf", FontWeight.Normal, FontStyle.Italic),
        Font("font/courier_prime_bold_italic.ttf", FontWeight.Bold, FontStyle.Italic)
    )
} catch (_: Throwable) {
    FontFamily.Monospace
}

actual val StudioMonoFontFamily: FontFamily = try {
    FontFamily(
        Font("font/jetbrains_mono_regular.ttf", FontWeight.Light),
        Font("font/jetbrains_mono_regular.ttf", FontWeight.Normal),
        Font("font/jetbrains_mono_medium.ttf", FontWeight.Medium),
        Font("font/jetbrains_mono_semibold.ttf", FontWeight.SemiBold),
        Font("font/jetbrains_mono_bold.ttf", FontWeight.Bold),
        Font("font/jetbrains_mono_bold.ttf", FontWeight.ExtraBold),
        Font("font/jetbrains_mono_bold.ttf", FontWeight.Black)
    )
} catch (_: Throwable) {
    FontFamily.Monospace
}

actual val MainAppFontFamily: FontFamily = try {
    FontFamily(
        Font("font/inter_regular.ttf", FontWeight.Light),
        Font("font/inter_regular.ttf", FontWeight.Normal),
        Font("font/inter_medium.ttf", FontWeight.Medium),
        Font("font/inter_semibold.ttf", FontWeight.SemiBold),
        Font("font/inter_semibold.ttf", FontWeight.Bold),
        Font("font/inter_semibold.ttf", FontWeight.ExtraBold),
        Font("font/inter_bold.ttf", FontWeight.Black)
    )
} catch (_: Throwable) {
    FontFamily.SansSerif
}
