package com.seshat.writer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.MainAppFontFamily

/**
 * Custom S E S H A T title banner matching the ScribeStar Media logo design.
 * Alternates between:
 * - Black lettering with hieroglyphic/circuit etchings and glowing white neon outline ('S', 'S', 'A')
 * - Red lettering with glowing stars and radiant scarlet neon outline ('E', 'H', 'T')
 */
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme

/**
 * Custom S E S H A T title banner matching the ScribeStar Media logo design.
 * Alternates between:
 * - Black lettering with hieroglyphic/circuit etchings ('S', 'S', 'A')
 * - Red lettering with glowing stars and radiant scarlet neon outline ('E', 'H', 'T')
 */
@Composable
fun SeshatNeonTitle(
    modifier: Modifier = Modifier,
    text: String = "SESHAT",
    fontSize: TextUnit = 44.sp,
    letterSpacing: Dp = 10.dp,
    isDark: Boolean = LocalSeshatDarkTheme.current
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(letterSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            val isBlackHieroglyph = (index % 2 == 0) // Alternates: S(black), E(red), S(black), H(red), A(black), T(red)
            SeshatNeonLetter(
                letter = char,
                isBlackHieroglyph = isBlackHieroglyph,
                isDark = isDark,
                fontSize = fontSize,
                seed = index * 1337 + char.code,
                textMeasurer = textMeasurer,
                density = density
            )
        }
    }
}

@Composable
private fun SeshatNeonLetter(
    letter: Char,
    isBlackHieroglyph: Boolean,
    isDark: Boolean,
    fontSize: TextUnit,
    seed: Int,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density
) {
    val baseStyle = remember(fontSize) {
        TextStyle(
            fontFamily = MainAppFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = fontSize
        )
    }

    val textLayout = remember(letter, baseStyle) {
        try {
            textMeasurer.measure(letter.toString(), style = baseStyle)
        } catch (_: Throwable) {
            null
        }
    }

    if (textLayout == null || textLayout.size.width <= 0 || textLayout.size.height <= 0) {
        // Fallback simple letter
        androidx.compose.material3.Text(
            text = letter.toString(),
            style = baseStyle.copy(
                color = if (isBlackHieroglyph) Color.Black else Color(0xFFFF2A3D)
            )
        )
        return
    }

    val paddingDp = remember(fontSize) { (fontSize.value * 0.14f).dp.coerceIn(8.dp, 16.dp) }
    val letterWidthDp = with(density) { textLayout.size.width.toDp() + (paddingDp * 2) }
    val letterHeightDp = with(density) { textLayout.size.height.toDp() + (paddingDp * 2) }

    Canvas(
        modifier = Modifier
            .size(letterWidthDp, letterHeightDp)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        try {
            val letterOffset = Offset(paddingDp.toPx(), paddingDp.toPx())
            val bounds = Size(textLayout.size.width.toFloat(), textLayout.size.height.toFloat())

            // 1. Draw base text mask
            drawText(
                textLayoutResult = textLayout,
                color = Color.White,
                topLeft = letterOffset
            )

            // 2. Draw interior clipped content using BlendMode.SrcIn
            if (isBlackHieroglyph) {
                drawBlackHieroglyphEtchings(bounds, letterOffset, seed, isDark)
            } else {
                drawRedStarField(bounds, letterOffset, seed)
            }

            // 3. Draw exterior glowing neon outline without measuring inside DrawScope
            val strokeWidth = 2.2.dp.toPx()
            val glowWidth = 4.8.dp.toPx()

            if (isBlackHieroglyph) {
                // Soft white neon halo
                drawText(
                    textLayoutResult = textLayout,
                    color = Color(0x66FFFFFF),
                    topLeft = letterOffset,
                    drawStyle = Stroke(width = glowWidth)
                )

                // Crisp neon electric white outline
                drawText(
                    textLayoutResult = textLayout,
                    color = Color(0xFFFFFFFF),
                    topLeft = letterOffset,
                    drawStyle = Stroke(width = strokeWidth)
                )
            } else {
                // Soft scarlet neon glow halo
                drawText(
                    textLayoutResult = textLayout,
                    color = Color(0x66FF2A3D),
                    topLeft = letterOffset,
                    drawStyle = Stroke(width = glowWidth)
                )

                // Crisp radiant neon scarlet outline
                drawText(
                    textLayoutResult = textLayout,
                    color = Color(0xFFFF2A3D),
                    topLeft = letterOffset,
                    drawStyle = Stroke(width = strokeWidth)
                )
            }
        } catch (_: Throwable) {
            // Defensive drawing fallback
        }
    }
}

/**
 * Draws the obsidian circuit board fill with hieroglyphic and cyber-etchings
 * seen in the 'SCRIBE' letters of the ScribeStar logo.
 */
private fun DrawScope.drawBlackHieroglyphEtchings(
    bounds: Size,
    offset: Offset,
    seed: Int,
    isDark: Boolean = true
) {
    // Solid pitch black background for maximum contrast
    val bgColors = listOf(Color(0xFF000000), Color(0xFF050505), Color(0xFF000000))
    drawRect(
        brush = Brush.verticalGradient(
            colors = bgColors,
            startY = offset.y,
            endY = offset.y + bounds.height
        ),
        topLeft = offset,
        size = bounds,
        blendMode = BlendMode.SrcIn
    )

    // White hieroglyphic designs and circuit etchings
    val etchColor1 = Color.White.copy(alpha = 0.85f)
    val etchColor2 = Color.White.copy(alpha = 0.95f)
    val etchColor3 = Color.White

    val w = bounds.width
    val h = bounds.height
    val ox = offset.x
    val oy = offset.y

    // Deterministic pseudo-random offsets derived from seed
    val shiftX = ((seed * 17) % 19) / 19f
    val shiftY = ((seed * 23) % 23) / 23f

    // 1. Circuit Conduits & Right Angles
    val conduitY1 = oy + h * (0.25f + shiftY * 0.15f)
    val conduitY2 = oy + h * (0.65f - shiftY * 0.15f)
    val conduitX1 = ox + w * (0.3f + shiftX * 0.2f)

    drawLine(
        color = etchColor1,
        start = Offset(ox, conduitY1),
        end = Offset(conduitX1, conduitY1),
        strokeWidth = 1.3f,
        blendMode = BlendMode.SrcIn
    )
    drawLine(
        color = etchColor1,
        start = Offset(conduitX1, conduitY1),
        end = Offset(conduitX1, conduitY2),
        strokeWidth = 1.3f,
        blendMode = BlendMode.SrcIn
    )
    drawLine(
        color = etchColor1,
        start = Offset(conduitX1, conduitY2),
        end = Offset(ox + w, conduitY2),
        strokeWidth = 1.3f,
        blendMode = BlendMode.SrcIn
    )

    // Circuit Vias (tiny pads)
    drawCircle(
        color = etchColor1,
        radius = 2.2f,
        center = Offset(conduitX1, conduitY1),
        blendMode = BlendMode.SrcIn
    )
    drawCircle(
        color = etchColor1,
        radius = 2.2f,
        center = Offset(conduitX1, conduitY2),
        blendMode = BlendMode.SrcIn
    )

    // Secondary circuit track
    val midY = oy + h * 0.48f
    drawLine(
        color = etchColor2,
        start = Offset(ox + w * 0.15f, midY),
        end = Offset(ox + w * 0.85f, midY),
        strokeWidth = 1.0f,
        blendMode = BlendMode.SrcIn
    )
    drawCircle(
        color = etchColor2,
        radius = 1.8f,
        center = Offset(ox + w * 0.5f, midY),
        blendMode = BlendMode.SrcIn
    )

    // 2. Hieroglyphic Etchings: Ankh (Key of Life)
    val ankhX = ox + w * (0.28f + shiftX * 0.35f)
    val ankhY = oy + h * 0.32f
    // Ankh oval loop
    drawCircle(
        color = etchColor3,
        radius = 3.5f,
        center = Offset(ankhX, ankhY),
        style = Stroke(width = 1.1f),
        blendMode = BlendMode.SrcIn
    )
    // Ankh horizontal bar
    drawLine(
        color = etchColor3,
        start = Offset(ankhX - 5.5f, ankhY + 4.5f),
        end = Offset(ankhX + 5.5f, ankhY + 4.5f),
        strokeWidth = 1.1f,
        blendMode = BlendMode.SrcIn
    )
    // Ankh vertical stem
    drawLine(
        color = etchColor3,
        start = Offset(ankhX, ankhY + 4.5f),
        end = Offset(ankhX, ankhY + 14f),
        strokeWidth = 1.1f,
        blendMode = BlendMode.SrcIn
    )

    // 3. Hieroglyphic Etching: Eye of Horus eyebrow & falcon markings
    val eyeX = ox + w * (0.62f - shiftX * 0.25f)
    val eyeY = oy + h * 0.72f
    // Brow arc
    val browPath = Path().apply {
        moveTo(eyeX - 8f, eyeY)
        quadraticTo(eyeX, eyeY - 4f, eyeX + 8f, eyeY)
    }
    drawPath(browPath, color = etchColor2, style = Stroke(width = 1.2f), blendMode = BlendMode.SrcIn)
    // Eye pupil
    drawCircle(color = etchColor1, radius = 1.6f, center = Offset(eyeX, eyeY + 2f), blendMode = BlendMode.SrcIn)
    // Falcon tear line
    drawLine(
        color = etchColor2,
        start = Offset(eyeX + 3f, eyeY + 3f),
        end = Offset(eyeX + 5f, eyeY + 10f),
        strokeWidth = 1.0f,
        blendMode = BlendMode.SrcIn
    )

    // 4. Hieroglyphic Djed Pillar Rungs
    val djedX = ox + w * 0.78f
    val djedY = oy + h * 0.3f
    drawLine(
        color = etchColor2,
        start = Offset(djedX, djedY),
        end = Offset(djedX, djedY + 16f),
        strokeWidth = 1.1f,
        blendMode = BlendMode.SrcIn
    )
    for (step in 0..3) {
        val rungY = djedY + (step * 3f)
        drawLine(
            color = etchColor2,
            start = Offset(djedX - 3.5f, rungY),
            end = Offset(djedX + 3.5f, rungY),
            strokeWidth = 1.0f,
            blendMode = BlendMode.SrcIn
        )
    }

    // 5. Water Wave Hieroglyphic Zigzag
    val wavePath = Path().apply {
        var wx = ox + w * 0.2f
        val wy = oy + h * 0.84f
        moveTo(wx, wy)
        for (i in 0..3) {
            lineTo(wx + 3f, wy - 3f)
            lineTo(wx + 6f, wy)
            wx += 6f
        }
    }
    drawPath(wavePath, color = etchColor3, style = Stroke(width = 1.0f), blendMode = BlendMode.SrcIn)
}

/**
 * Draws the radiant scarlet red fill with scattered glowing stars
 * seen in the 'STAR' letters of the ScribeStar logo.
 */
private fun DrawScope.drawRedStarField(bounds: Size, offset: Offset, seed: Int) {
    // Rich glowing ruby/scarlet red gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF3348),
                Color(0xFFDC2626),
                Color(0xFF991B1B)
            ),
            startY = offset.y,
            endY = offset.y + bounds.height
        ),
        topLeft = offset,
        size = bounds,
        blendMode = BlendMode.SrcIn
    )

    val w = bounds.width
    val h = bounds.height
    val ox = offset.x
    val oy = offset.y

    // Star coordinates distributed across the letter bounds
    val starPositions = listOf(
        Triple(0.24f, 0.20f, 6.0f),
        Triple(0.72f, 0.22f, 7.5f),
        Triple(0.48f, 0.38f, 8.5f),
        Triple(0.20f, 0.52f, 5.5f),
        Triple(0.78f, 0.58f, 6.5f),
        Triple(0.38f, 0.70f, 7.0f),
        Triple(0.68f, 0.82f, 5.5f),
        Triple(0.28f, 0.86f, 4.8f),
        Triple(0.85f, 0.38f, 4.2f),
        Triple(0.14f, 0.74f, 4.0f)
    )

    val starGlowColor = Color(0xFFFF8595)
    val starCoreColor = Color(0xFFFFFFFF)

    starPositions.forEachIndexed { idx, (relX, relY, radius) ->
        val shift = ((seed * (idx + 7)) % 11) / 55f - 0.1f
        val cx = ox + w * (relX + shift).coerceIn(0.1f, 0.9f)
        val cy = oy + h * (relY + shift).coerceIn(0.1f, 0.9f)

        // Draw 4-pointed diamond star
        drawDiamondStar(
            center = Offset(cx, cy),
            radius = radius,
            glowColor = starGlowColor,
            coreColor = starCoreColor
        )
    }

    // Additional tiny starlight sparkles
    val sparkles = listOf(
        Pair(0.35f, 0.12f),
        Pair(0.60f, 0.28f),
        Pair(0.55f, 0.62f),
        Pair(0.82f, 0.72f),
        Pair(0.18f, 0.35f)
    )
    sparkles.forEach { (sx, sy) ->
        val cx = ox + w * sx
        val cy = oy + h * sy
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 1.4f,
            center = Offset(cx, cy),
            blendMode = BlendMode.SrcIn
        )
    }
}

/**
 * Draws a 4-pointed diamond star with pinched inner curves and a glowing white center.
 */
private fun DrawScope.drawDiamondStar(
    center: Offset,
    radius: Float,
    glowColor: Color,
    coreColor: Color
) {
    val cx = center.x
    val cy = center.y
    val r = radius
    val inner = r * 0.22f

    val starPath = Path().apply {
        moveTo(cx, cy - r)
        quadraticTo(cx + inner, cy - inner, cx + r, cy)
        quadraticTo(cx + inner, cy + inner, cx, cy + r)
        quadraticTo(cx - inner, cy + inner, cx - r, cy)
        quadraticTo(cx - inner, cy - inner, cx, cy - r)
        close()
    }

    // Outer star petal fill
    drawPath(
        path = starPath,
        color = glowColor,
        style = Fill,
        blendMode = BlendMode.SrcIn
    )

    // Bright white center dot
    drawCircle(
        color = coreColor,
        radius = r * 0.28f,
        center = center,
        blendMode = BlendMode.SrcIn
    )
}
