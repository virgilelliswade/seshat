package com.seshat.writer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.model.BlockType
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.StudioMonoFontFamily

/**
 * Final Draft 13 / Scrivener 3 style docked top margin ruler bar.
 * Aligns with the 720dp physical manuscript sheet below it and displays
 * inch graduations and active element tab/margin stops.
 */
@Composable
fun ScreenplayMarginRulerBar(
    activeBlockType: BlockType? = null,
    isNovelMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val rulerBg = if (isDark) ObsidianSurface else androidx.compose.ui.graphics.Color(0xFFEDF2F7)
    val tickColor = ScreenplayTextMuted.copy(alpha = 0.55f)
    val accentColor = ScribeStarNeonRed

    // Determine active left/right margin stops in inches (out of 8.5" page width)
    val (leftStopInches, rightStopInches) = if (isNovelMode) {
        1.0f to 7.5f
    } else {
        when (activeBlockType) {
            BlockType.CHARACTER -> 3.7f to 7.2f
            BlockType.PARENTHETICAL -> 3.1f to 5.6f
            BlockType.DIALOGUE, BlockType.COMIC_BALLOON -> 2.5f to 6.0f
            BlockType.TRANSITION -> 5.5f to 7.5f
            else -> 1.5f to 7.5f
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .shadow(6.dp, RoundedCornerShape(0.dp))
            .height(24.dp)
            .background(rulerBg)
            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                val w = size.width
                val h = size.height
                if (w <= 0f) return@Canvas

                // Draw eighth-inch and half-inch ticks across 0" .. 8.5"
                val totalInches = 8.5f
                val step = 0.25f
                var inch = 0f
                while (inch <= totalInches) {
                    val x = (inch / totalInches) * w
                    val isWhole = (inch % 1.0f) == 0f
                    val isHalf = (inch % 0.5f) == 0f
                    val tickHeight = when {
                        isWhole -> 7.dp.toPx()
                        isHalf -> 5.dp.toPx()
                        else -> 3.dp.toPx()
                    }
                    drawLine(
                        color = tickColor,
                        start = Offset(x, h - tickHeight),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    inch += step
                }

                // Draw active left and right margin stop triangles at top of ruler
                val leftX = (leftStopInches / totalInches) * w
                val rightX = (rightStopInches / totalInches) * w
                val triHalf = 4.dp.toPx()
                val triH = 5.5.dp.toPx()

                val leftTriangle = Path().apply {
                    moveTo(leftX - triHalf, 2.dp.toPx())
                    lineTo(leftX + triHalf, 2.dp.toPx())
                    lineTo(leftX, 2.dp.toPx() + triH)
                    close()
                }
                drawPath(leftTriangle, color = accentColor)

                val rightTriangle = Path().apply {
                    moveTo(rightX - triHalf, 2.dp.toPx())
                    lineTo(rightX + triHalf, 2.dp.toPx())
                    lineTo(rightX, 2.dp.toPx() + triH)
                    close()
                }
                drawPath(rightTriangle, color = accentColor.copy(alpha = 0.75f))
            }

            // Key screenplay margin labels
            val labels = if (isNovelMode) {
                listOf(1.0f to "1.0\"", 2.5f to "2.5\"", 4.25f to "4.25\"", 6.0f to "6.0\"", 7.5f to "7.5\"")
            } else {
                listOf(1.5f to "1.5\"", 2.5f to "2.5\"", 3.7f to "3.7\"", 5.0f to "5.0\"", 6.0f to "6.0\"", 7.5f to "7.5\"")
            }
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                labels.forEach { (stopInch, labelText) ->
                    val isActiveStop = kotlin.math.abs(stopInch - leftStopInches) < 0.15f
                    Text(
                        text = labelText,
                        fontFamily = StudioMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = if (isActiveStop) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActiveStop) accentColor else ScreenplayTextMuted
                    )
                }
            }
        }

        HorizontalDivider(
            color = ObsidianSurfaceVariant,
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
