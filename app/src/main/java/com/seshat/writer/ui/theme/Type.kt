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

// Montserrat geometric typeface family (retained for backward compatibility)
val MontserratFontFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold),
    Font(R.font.montserrat_black, FontWeight.Black)
)

// Plus Jakarta Sans: Kinetic Modern Geometric typeface family for sleek, contemporary legibility
val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.Black)
)

// Modern, thin, sleek geometric typeface family: Roboto (System Sans-Serif)
val RobotoFontFamily = FontFamily.SansSerif

// Main application font family for all UI, headings, dialogs, buttons, and titles
val MainAppFontFamily = RobotoFontFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 54.sp,
        lineHeight = 60.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 42.sp,
        lineHeight = 48.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MainAppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
