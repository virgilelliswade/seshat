package com.seshat.writer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.seshat.writer.R

// Courier Prime typography reserved for screenplay script canvas formatting
val CourierPrimeFamily = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.courier_prime_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.courier_prime_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.courier_prime_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

val ScreenplayCourierFontFamily = CourierPrimeFamily
val ScreenplayFontFamily = ScreenplayCourierFontFamily

// JetBrains Mono: Precision tabular monospace for telemetry, rulers, scene numbers, and badges
val StudioMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Light),
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    Font(R.font.jetbrains_mono_bold, FontWeight.ExtraBold),
    Font(R.font.jetbrains_mono_bold, FontWeight.Black)
)

// Inter: Precision neo-grotesque UI typeface with 400/500/600 studio weight discipline
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_semibold, FontWeight.Bold),
    Font(R.font.inter_semibold, FontWeight.ExtraBold),
    Font(R.font.inter_bold, FontWeight.Black)
)

// Legacy aliases routed cleanly to Inter
val MontserratFontFamily = InterFontFamily
val PlusJakartaSansFontFamily = InterFontFamily
val RobotoFontFamily = InterFontFamily

// Main application font family for all UI, headings, dialogs, buttons, and titles
val MainAppFontFamily = InterFontFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.8).sp
    ),
    displayMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.5.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.15).sp
    ),
    titleSmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.1).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.1).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.05).sp
    ),
    bodySmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = (-0.05).sp
    ),
    labelMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )
)
