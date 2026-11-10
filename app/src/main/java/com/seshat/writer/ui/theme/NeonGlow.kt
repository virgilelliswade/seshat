package com.seshat.writer.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Concept C ("Floating Glass Islands") Elevation Shadow + Optical Glass Rim + Specular Top Catch-Light.
 * Every floating object casts a multi-layered drop shadow and catches a 1px top specular highlight
 * while maintaining strict 0.dp hard-cornered geometry.
 */
fun Modifier.neonBloom(
    color: Color = ScribeStarNeonRed,
    cornerRadius: Dp = 0.dp,
    glowRadius: Dp = 8.dp,
    strokeWidth: Dp = 1.dp,
    outerAlpha: Float = 0.14f,
    midAlpha: Float = 0.24f,
    coreAlpha: Float = 0.85f
): Modifier = this.drawBehind {
    try {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@drawBehind
        val isDark = ActiveDarkThemeState.value

        // 1. Multi-layered spatial drop-shadow beneath every floating element
        val shadowSpread = glowRadius.toPx().coerceIn(4.dp.toPx(), 18.dp.toPx())
        val baseShadowAlpha = if (isDark) 0.42f else 0.14f
        for (i in 3 downTo 1) {
            val factor = i / 3f
            val pad = shadowSpread * factor * 0.55f
            val yShift = (2.5f + i * 2.0f).dp.toPx()
            drawRect(
                color = Color.Black.copy(alpha = baseShadowAlpha * (1.1f - factor * 0.3f)),
                topLeft = Offset(-pad * 0.35f, yShift),
                size = Size(w + pad * 0.7f, h + pad * 0.4f)
            )
        }

        // 2. Subtle ambient theme-tinted halo when hovered or highlighted
        if (glowRadius > 6.dp && outerAlpha > 0.05f) {
            val haloPad = (glowRadius.toPx() * 0.35f).coerceAtMost(10.dp.toPx())
            drawRect(
                color = color.copy(alpha = (outerAlpha * 0.28f).coerceIn(0f, 0.22f)),
                topLeft = Offset(-haloPad, -haloPad * 0.5f),
                size = Size(w + haloPad * 2f, h + haloPad)
            )
        }

        // 3. Crisp 0.dp outer frame + 1.dp top specular glass catch-light
        if (coreAlpha >= 0.45f) {
            drawRoundRect(
                color = color.copy(alpha = (coreAlpha * 0.55f).coerceIn(0.18f, 0.85f)),
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius.Zero,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        val specularAlpha = if (isDark) 0.28f else 0.90f
        drawLine(
            color = Color.White.copy(alpha = specularAlpha),
            start = Offset(1.dp.toPx(), 0.5.dp.toPx()),
            end = Offset((w - 1.dp.toPx()).coerceAtLeast(1f), 0.5.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    } catch (_: Throwable) {
        // Defensive: never let graphics drawing crash the UI
    }
}

/**
 * Reusable Concept C Floating Glass Island modifier for Top Bars, Side Binders, Bottom HUDs,
 * Act Columns, and Story Bible Panes. Casts an elevated drop shadow with a 0.dp optical glass frame.
 */
fun Modifier.floatingGlassIsland(
    isDark: Boolean = ActiveDarkThemeState.value,
    elevation: Dp = 14.dp,
    accentColor: Color = ScribeStarNeonRed,
    isHighlighted: Boolean = false
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(0.dp),
        ambientColor = Color.Black.copy(alpha = if (isDark) 0.75f else 0.22f),
        spotColor = Color.Black.copy(alpha = if (isDark) 0.85f else 0.28f)
    )
    .neonBloom(
        color = if (isHighlighted) accentColor else (if (isDark) Color.White else Color(0xFF64748B)),
        cornerRadius = 0.dp,
        glowRadius = if (isHighlighted) 14.dp else 8.dp,
        coreAlpha = if (isHighlighted) 0.90f else 0.35f
    )
    .clip(RoundedCornerShape(0.dp))
    .background(opticalGlassBrush(glassOpacity = if (isDark) 0.84f else 0.90f))
    .border(
        prismaticGlassBorder(
            width = 1.dp,
            isLit = isHighlighted,
            accentColor = accentColor,
            isDark = isDark
        ),
        shape = RoundedCornerShape(0.dp)
    )

/**
 * Smoked Crystal (Dark) / Frosted Quartz (Light) floating glass surface brush.
 */
fun opticalGlassBrush(
    glassOpacity: Float = 0.84f
): Brush {
    val palette = ActiveStudioPaletteState.value
    val isDark = ActiveDarkThemeState.value
    return if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF1A2232).copy(alpha = 0.28f),
                palette.darkSurface.copy(alpha = glassOpacity),
                palette.darkBg.copy(alpha = (glassOpacity + 0.08f).coerceAtMost(0.96f))
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.94f),
                palette.lightSurface.copy(alpha = 0.86f),
                palette.lightBg.copy(alpha = 0.80f)
            )
        )
    }
}

/**
 * Specular top-lit optical glass border brush adapting to the active Studio Palette.
 */
fun prismaticBevelBrush(
    isLit: Boolean = false,
    accentColor: Color = ScribeStarNeonRed,
    isHighlighted: Boolean = isLit,
    highlightColor: Color = accentColor
): Brush {
    val palette = ActiveStudioPaletteState.value
    val isDark = ActiveDarkThemeState.value
    val activeAccent = if (isDark) palette.darkAccent else palette.lightAccent
    val resolvedAccent = if (highlightColor == ScribeStarNeonRed) activeAccent else highlightColor
    return if (isHighlighted) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.55f else 0.85f),
                resolvedAccent,
                resolvedAccent.copy(alpha = 0.75f)
            )
        )
    } else if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.30f),
                palette.darkBorder.copy(alpha = 0.95f),
                palette.darkBorder.copy(alpha = 0.75f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White,
                palette.lightBorder,
                palette.lightBorder
            )
        )
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
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (isDark) 0.50f else 0.80f),
                    resolvedAccent,
                    resolvedAccent.copy(alpha = 0.85f)
                )
            )
        } else if (isDark) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.28f),
                    palette.darkBorder,
                    palette.darkBorder.copy(alpha = 0.80f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White,
                    palette.lightBorder,
                    palette.lightBorder
                )
            )
        }
    )
}

/**
 * Concept C Hard-Cornered Optical Glass Fill for interactive buttons, tabs, and action controls.
 * Works seamlessly across all 7 themes including Obsidian & Ivory (Black & White).
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
        isPrimary || isHighlighted -> if (isDark) {
            // Smoked translucent glass lit by the theme accent (keeps white text 100% legible even on White/Monochrome theme!)
            Brush.verticalGradient(
                listOf(
                    resolvedAccent.copy(alpha = if (isPrimary) 0.36f else 0.28f),
                    resolvedAccent.copy(alpha = if (isPrimary) 0.16f else 0.10f)
                )
            )
        } else {
            // In Light Mode, primary/highlighted uses crisp dark or theme accent fill
            Brush.verticalGradient(
                listOf(
                    resolvedAccent.copy(alpha = 0.92f),
                    resolvedAccent
                )
            )
        }
        isAccent -> Brush.verticalGradient(
            listOf(
                resolvedAccent.copy(alpha = 0.22f),
                resolvedAccent.copy(alpha = 0.08f)
            )
        )
        else -> if (isDark) {
            // Rest state: Smoked glass button with subtle top sheen
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.09f),
                    palette.darkSurface.copy(alpha = 0.82f)
                )
            )
        } else {
            // Rest state: Frosted quartz button with crisp white top sheen
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.96f),
                    palette.lightBg.copy(alpha = 0.85f)
                )
            )
        }
    }
}

/**
 * Concept C Ambient Studio Backdrop Brush:
 * Provides subtle atmospheric depth behind the floating glass islands.
 */
fun ambientWorkspaceBrush(
    isDark: Boolean = ActiveDarkThemeState.value
): Brush {
    val palette = ActiveStudioPaletteState.value
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                palette.darkBg,
                palette.darkAccent.copy(alpha = 0.07f),
                palette.darkSurface.copy(alpha = 0.92f),
                palette.darkBg
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                palette.lightDesk,
                palette.lightBg,
                palette.lightDesk.copy(alpha = 0.85f)
            )
        )
    }
}

/**
 * Smoked Crystal / Frosted Quartz card brush for floating cards and modals.
 */
fun smokedGlassBrush(
    baseColor: Color = RawObsidianSurface,
    glassOpacity: Float = 0.86f
): Brush {
    val palette = ActiveStudioPaletteState.value
    val isDark = ActiveDarkThemeState.value
    return if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.07f),
                palette.darkSurface.copy(alpha = glassOpacity),
                palette.darkBg.copy(alpha = (glassOpacity + 0.06f).coerceAtMost(0.95f))
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.96f),
                palette.lightSurface.copy(alpha = 0.90f),
                palette.lightBg.copy(alpha = 0.85f)
            )
        )
    }
}
