package com.seshat.writer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.StudioMonoFontFamily

/**
 * Joined Segmented Studio Formatting Strip:
 * Groups [ B | I | U | S ] and [ A- | 12pt | A+ ] into unified 1px-bordered segmented strips
 * with internal hairline dividers instead of separate floating boxes.
 */
@Composable
fun EditorFormattingToolbarRow(
    formattingController: EditorFormattingController,
    currentFontSizePt: Int,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onResetFontSize: () -> Unit = {},
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val stripBg = ObsidianBlack
    val stripBorder = ObsidianSurfaceVariant
    val textColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val mutedTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val btnPaddingH = if (isCompact) 8.dp else 10.dp
    val btnPaddingV = if (isCompact) 4.dp else 5.dp
    val fontSize = if (isCompact) 11.sp else 11.5.sp
    val dividerHeight = if (isCompact) 20.dp else 24.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // Unified Typography Strip: [ B | I | U | S ]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(0.dp))
                .background(stripBg)
                .border(BorderStroke(1.dp, stripBorder), RoundedCornerShape(0.dp))
        ) {
            // [ B ] Bold
            Box(
                modifier = Modifier
                    .clickable { formattingController.toggleBold() }
                    .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = fontSize,
                        color = textColor
                    )
                )
            }

            Box(modifier = Modifier.width(1.dp).height(dividerHeight).background(stripBorder))

            // [ I ] Italic
            Box(
                modifier = Modifier
                    .clickable { formattingController.toggleItalic() }
                    .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "I",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        color = textColor
                    )
                )
            }

            Box(modifier = Modifier.width(1.dp).height(dividerHeight).background(stripBorder))

            // [ U ] Underline
            Box(
                modifier = Modifier
                    .clickable { formattingController.toggleUnderline() }
                    .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        color = textColor
                    )
                )
            }

            Box(modifier = Modifier.width(1.dp).height(dividerHeight).background(stripBorder))

            // [ S ] Strikethrough
            Box(
                modifier = Modifier
                    .clickable { formattingController.toggleStrikethrough() }
                    .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        textDecoration = TextDecoration.LineThrough,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        color = textColor
                    )
                )
            }
        }

        // Unified Font Scale Strip: [ A- | 12pt | A+ ]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(0.dp))
                .background(stripBg)
                .border(BorderStroke(1.dp, stripBorder), RoundedCornerShape(0.dp))
        ) {
            // [ A- ] Decrease Font Size
            Box(
                modifier = Modifier
                    .clickable { onDecreaseFontSize() }
                    .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A-",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSize,
                        color = textColor
                    )
                )
            }

            Box(modifier = Modifier.width(1.dp).height(dividerHeight).background(stripBorder))

            // [ 12pt ] Monospace Readout (Click to reset)
            Box(
                modifier = Modifier
                    .clickable { onResetFontSize() }
                    .padding(horizontal = 8.dp, vertical = btnPaddingV),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${currentFontSizePt}pt",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = StudioMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (fontSize.value - 0.5f).sp,
                        color = mutedTextColor
                    )
                )
            }

            Box(modifier = Modifier.width(1.dp).height(dividerHeight).background(stripBorder))

            // [ A+ ] Increase Font Size
            Box(
                modifier = Modifier
                    .clickable { onIncreaseFontSize() }
                    .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A+",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSize,
                        color = textColor
                    )
                )
            }
        }
    }
}
