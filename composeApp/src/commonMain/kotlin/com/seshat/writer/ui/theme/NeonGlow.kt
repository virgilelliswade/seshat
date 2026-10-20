package com.seshat.writer.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders an authentic multi-pass radiant neon bloom glow around rounded rectangular elements.
 * Emulates true physical neon gas emission bleeding softly into surrounding dark space:
 * 1. Wide diffuse outer aura (8-14dp spread, soft alpha)
 * 2. Medium vibrant bloom (4-6dp spread, medium alpha)
 * 3. Solid saturated neon core tube (1.8-2dp stroke)
 * 4. High-intensity center filament spark (0.8dp stroke with white mix)
 */
fun Modifier.neonBloom(
    color: Color = ScribeStarNeonRed,
    cornerRadius: Dp = 18.dp,
    glowRadius: Dp = 10.dp,
    strokeWidth: Dp = 1.8.dp,
    outerAlpha: Float = 0.16f,
    midAlpha: Float = 0.38f,
    coreAlpha: Float = 0.95f
): Modifier = this.drawBehind {
    try {
        val cr = cornerRadius.toPx().coerceAtLeast(0f)
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f) return@drawBehind

        // Pass 1: Wide diffuse outer aura
        val outerSpread = glowRadius.toPx().coerceAtLeast(0f)
        if (outerSpread > 0f) {
            val r1 = (cr + outerSpread / 2f).coerceAtLeast(0f)
            drawRoundRect(
                color = color.copy(alpha = outerAlpha),
                topLeft = Offset(-outerSpread / 2f, -outerSpread / 2f),
                size = Size(w + outerSpread, h + outerSpread),
                cornerRadius = CornerRadius(r1, r1),
                style = Stroke(width = outerSpread)
            )
        }

        // Pass 2: Mid vibrant bloom
        val midSpread = (glowRadius.toPx() * 0.45f).coerceAtLeast(2f)
        val r2 = (cr + midSpread / 2f).coerceAtLeast(0f)
        drawRoundRect(
            color = color.copy(alpha = midAlpha),
            topLeft = Offset(-midSpread / 2f, -midSpread / 2f),
            size = Size(w + midSpread, h + midSpread),
            cornerRadius = CornerRadius(r2, r2),
            style = Stroke(width = midSpread)
        )

        // Pass 3: Saturated core neon tube
        drawRoundRect(
            color = color.copy(alpha = coreAlpha),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(cr, cr),
            style = Stroke(width = strokeWidth.toPx().coerceAtLeast(1f))
        )

        // Pass 4: Inner hot filament spark (gives that true incandescent electric look)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.40f),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(cr, cr),
            style = Stroke(width = (strokeWidth.toPx() * 0.4f).coerceAtLeast(0.8f))
        )
    } catch (_: Throwable) {
        // Defensive: never let graphics drawing crash the UI
    }
}

/**
 * True Optical Glass: Neutral liquid crystal refraction with crisp specular rim,
 * subtle cyan/prismatic chromatic dispersion band, and neutral ambient depth.
 * Mode-agnostic: adapts naturally to both dark, twilight, and daylight environments.
 */
fun opticalGlassBrush(
    glassOpacity: Float = 0.82f
): Brush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.32f),                            // Specular light reflection rim
    0.03f to Color(0x3538BDF8),                                         // Prismatic cyan dispersion band
    0.10f to Color(0x241E293B).copy(alpha = glassOpacity * 0.85f),      // Liquid crystal body
    0.65f to Color(0x180F172A).copy(alpha = glassOpacity),              // Deep neutral optical depth
    1.0f to Color(0x28020617).copy(alpha = glassOpacity * 0.95f)       // Caustic bottom shelf
)

/**
 * Prismatic 45° bevel border brush simulating cut crystal dispersion:
 * Specular white gleam on top/left, subtle ice-cyan transition, and ScribeStar scarlet caustics on bottom/right.
 */
fun prismaticBevelBrush(
    isLit: Boolean = false,
    accentColor: Color = ScribeStarNeonRed
): Brush = Brush.linearGradient(
    0.0f to Color.White.copy(alpha = if (isLit) 0.92f else 0.65f),      // Top-left specular catch
    0.25f to Color(0x997DD3FC),                                         // Ice-cyan prismatic dispersion
    0.50f to Color.White.copy(alpha = if (isLit) 0.60f else 0.35f),     // Mid specular sheen
    0.80f to accentColor.copy(alpha = if (isLit) 1.0f else 0.65f),      // ScribeStar neon scarlet caustic
    1.0f to Color(0xFFFF5C6E).copy(alpha = if (isLit) 0.95f else 0.50f) // Ruby/coral flare
)

fun prismaticGlassBorder(
    width: Dp = 1.dp,
    isLit: Boolean = false,
    accentColor: Color = ScribeStarNeonRed,
    isDark: Boolean = true
): androidx.compose.foundation.BorderStroke = androidx.compose.foundation.BorderStroke(
    width = if (isLit) (width * 1.3f).coerceAtLeast(1.2.dp) else width,
    brush = if (isDark) {
        prismaticBevelBrush(isLit = isLit, accentColor = accentColor)
    } else {
        if (isLit) Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C)))
        else Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8).copy(alpha = 0.8f)))
    }
)

/**
 * Liquid crystal glass lens for interactive pills, chips, and buttons.
 */
fun opticalPillBrush(
    isLit: Boolean = false,
    isPrimary: Boolean = false,
    isAccent: Boolean = false,
    accentColor: Color = ScribeStarNeonRed,
    isDark: Boolean = true
): Brush = when {
    isLit -> Brush.verticalGradient(
        if (isDark) listOf(
            Color(0x905C1322),
            Color(0xA0380814),
            Color(0x8024050D)
        ) else listOf(
            Color(0xFFE11D48),
            Color(0xFFBE123C),
            Color(0xFF9F1239)
        )
    )
    isPrimary -> Brush.verticalGradient(
        if (isDark) listOf(
            Color(0x855C1322),
            Color(0x95380814)
        ) else listOf(
            Color(0xFFF43F5E),
            Color(0xFFE11D48)
        )
    )
    isAccent -> Brush.verticalGradient(
        if (isDark) listOf(
            Color(0x405C1322),
            Color(0x5024050D)
        ) else listOf(
            Color(0x30F43F5E),
            Color(0x40E11D48)
        )
    )
    else -> if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.24f), // Specular pill top rim
                Color(0x281E293B),               // Liquid acrylic refraction
                Color(0x380F172A)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.96f),
                Color(0xFFF8FAFC).copy(alpha = 0.92f),
                Color(0xFFF1F5F9).copy(alpha = 0.94f)
            )
        )
    }
}

/**
 * Atmospheric ambient workstation background brush behind optical glass panels.
 * Emulates the twilight horizon / caustic studio environment from the concept mockups.
 */
fun ambientWorkspaceBrush(
    isDark: Boolean = true
): Brush = if (isDark) {
    Brush.radialGradient(
        colors = listOf(
            Color(0xDF1E283D), // Soft ambient twilight bloom in upper-center (~87% opacity)
            Color(0xEA0F1522), // Ambient slate diffusion (~92% opacity)
            Color(0xF4060910)  // Deep midnight horizon at borders (~96% opacity)
        )
    )
} else {
    Brush.radialGradient(
        colors = listOf(
            Color(0xE6F1F5F9), // Daylight porcelain bloom (~90% opacity)
            Color(0xF0E2E8F0), // Cool studio slate (~94% opacity)
            Color(0xF8CBD5E1)  // Clean desk border (~97% opacity)
        )
    )
}

/**
 * Smoked obsidian glass gradient with beveled top rim specular highlight.
 */
fun smokedGlassBrush(
    baseColor: Color = Color(0xFF070A0F),
    glassOpacity: Float = 0.88f
): Brush = Brush.verticalGradient(
    0.0f to Color.White.copy(alpha = 0.28f),
    0.03f to Color(0x2538BDF8),
    0.08f to Color(0xFF0F1522).copy(alpha = glassOpacity),
    0.60f to baseColor.copy(alpha = glassOpacity),
    1.0f to Color(0xFF020408).copy(alpha = glassOpacity * 0.98f)
)
