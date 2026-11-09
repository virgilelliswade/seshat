package com.seshat.writer.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern Architectural Precision Border & Subtle Focus Highlight.
 * Replaces legacy rounded neon gas blooms with crisp, squared-off studio geometry.
 */
fun Modifier.neonBloom(
    color: Color = ScribeStarNeonRed,
    cornerRadius: Dp = 0.dp,
    glowRadius: Dp = 0.dp,
    strokeWidth: Dp = 1.dp,
    outerAlpha: Float = 0.08f,
    midAlpha: Float = 0.18f,
    coreAlpha: Float = 0.85f
): Modifier = this.drawBehind {
    try {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@drawBehind

        // Crisp squared-off architectural frame (0f corner radius)
        if (outerAlpha > 0.25f) {
            drawRoundRect(
                color = color.copy(alpha = (outerAlpha * 0.35f).coerceIn(0f, 0.25f)),
                topLeft = Offset(-1.dp.toPx(), -1.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(w + 2.dp.toPx(), h + 2.dp.toPx()),
                cornerRadius = CornerRadius.Zero,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        drawRoundRect(
            color = color.copy(alpha = coreAlpha.coerceIn(0f, 1f)),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius.Zero,
            style = Stroke(width = strokeWidth.toPx().coerceAtLeast(1f))
        )
    } catch (_: Throwable) {
        // Defensive: never let graphics drawing crash the UI
    }
}

/**
 * Clean matte studio surface brush.
 */
fun opticalGlassBrush(
    glassOpacity: Float = 0.94f
): Brush {
    val palette = ActiveStudioPaletteState.value
    val isDark = ActiveDarkThemeState.value
    return if (isDark) {
        Brush.verticalGradient(
            listOf(
                palette.darkSurface.copy(alpha = glassOpacity),
                palette.darkBg.copy(alpha = glassOpacity)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                palette.lightSurface,
                palette.lightBg
            )
        )
    }
}

/**
 * Precision hairline border brush adapting to the active Studio Palette.
 */
fun prismaticBevelBrush(
    isLit: Boolean = false,
    accentColor: Color = ScribeStarNeonRed,
    isHighlighted: Boolean = isLit,
    highlightColor: Color = accentColor
): Brush {
    val palette = ActiveStudioPaletteState.value
    val isDark = ActiveDarkThemeState.value
    return if (isHighlighted) {
        Brush.linearGradient(listOf(highlightColor, highlightColor.copy(alpha = 0.85f)))
    } else if (isDark) {
        Brush.linearGradient(listOf(palette.darkBorder, palette.darkBorder.copy(alpha = 0.85f)))
    } else {
        Brush.linearGradient(listOf(palette.lightBorder, palette.lightBorder))
    }
}

fun prismaticGlassBorder(
    width: Dp = 1.dp,
    isLit: Boolean = false,
    accentColor: Color = ScribeStarNeonRed,
    isHighlighted: Boolean = isLit,
    highlightColor: Color = accentColor,
    isDark: Boolean = ActiveDarkThemeState.value
): androidx.compose.foundation.BorderStroke {
    val palette = ActiveStudioPaletteState.value
    val activeAccent = if (isDark) palette.darkAccent else palette.lightAccent
    val resolvedAccent = if (highlightColor == ScribeStarNeonRed) activeAccent else highlightColor
    return androidx.compose.foundation.BorderStroke(
        width = width,
        brush = if (isHighlighted) {
            Brush.linearGradient(listOf(resolvedAccent, resolvedAccent))
        } else if (isDark) {
            Brush.linearGradient(listOf(palette.darkBorder, palette.darkBorder))
        } else {
            Brush.linearGradient(listOf(palette.lightBorder, palette.lightBorder))
        }
    )
}

/**
 * Matte architectural studio fill for interactive buttons, tabs, and action controls.
 */
fun opticalPillBrush(
    isLit: Boolean = false,
    isPrimary: Boolean = false,
    isAccent: Boolean = false,
    accentColor: Color = ScribeStarNeonRed,
    isHighlighted: Boolean = isLit,
    highlightColor: Color = accentColor,
    isDark: Boolean = ActiveDarkThemeState.value
): Brush {
    val palette = ActiveStudioPaletteState.value
    val activeAccent = if (isDark) palette.darkAccent else palette.lightAccent
    val resolvedAccent = if (highlightColor == ScribeStarNeonRed) activeAccent else highlightColor
    return when {
        isPrimary -> Brush.verticalGradient(
            listOf(resolvedAccent, resolvedAccent)
        )
        isHighlighted -> if (isDark) {
            Brush.verticalGradient(
                listOf(
                    resolvedAccent.copy(alpha = 0.24f),
                    palette.darkSurface
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    resolvedAccent,
                    resolvedAccent
                )
            )
        }
        isAccent -> Brush.verticalGradient(
            listOf(
                resolvedAccent.copy(alpha = 0.16f),
                resolvedAccent.copy(alpha = 0.12f)
            )
        )
        else -> if (isDark) {
            Brush.verticalGradient(
                listOf(
                    palette.darkSurface,
                    palette.darkSurface
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    palette.lightSurface,
                    palette.lightSurface
                )
            )
        }
    }
}

/**
 * Clean architectural studio background brush.
 */
fun ambientWorkspaceBrush(
    isDark: Boolean = ActiveDarkThemeState.value
): Brush {
    val palette = ActiveStudioPaletteState.value
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                palette.darkBg,
                palette.darkSurface.copy(alpha = 0.95f),
                palette.darkBg
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                palette.lightBg,
                palette.lightDesk.copy(alpha = 0.65f),
                palette.lightBg
            )
        )
    }
}

/**
 * Matte studio card brush.
 */
fun smokedGlassBrush(
    baseColor: Color = RawObsidianSurface,
    glassOpacity: Float = 0.96f
): Brush {
    val palette = ActiveStudioPaletteState.value
    val isDark = ActiveDarkThemeState.value
    return if (isDark) {
        Brush.verticalGradient(
            listOf(
                palette.darkSurface.copy(alpha = glassOpacity),
                palette.darkBg.copy(alpha = glassOpacity)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                palette.lightSurface,
                palette.lightBg
            )
        )
    }
}
