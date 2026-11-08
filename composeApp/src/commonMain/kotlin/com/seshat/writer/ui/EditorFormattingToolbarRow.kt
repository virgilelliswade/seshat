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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed

/**
 * Compact, tactile formatting capsule providing 1-tap Bold, Italic, Underline,
 * and Font Size step controls (A- / A+) for mobile touchscreens and desktop.
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
    val pillBg = if (isDark) Color(0xFF131A26) else Color(0xFFF1F5F9)
    val pillBorder = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val textColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)
    val btnPaddingH = if (isCompact) 8.dp else 10.dp
    val btnPaddingV = if (isCompact) 4.dp else 6.dp
    val fontSize = if (isCompact) 11.5.sp else 12.sp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        // [ B ] Bold Button
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg)
                .border(BorderStroke(1.dp, pillBorder), RoundedCornerShape(10.dp))
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

        // [ I ] Italic Button
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg)
                .border(BorderStroke(1.dp, pillBorder), RoundedCornerShape(10.dp))
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

        // [ U ] Underline Button
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg)
                .border(BorderStroke(1.dp, pillBorder), RoundedCornerShape(10.dp))
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

        // Divider
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .width(1.dp)
                .height(if (isCompact) 16.dp else 20.dp)
                .background(pillBorder)
        )

        // [ A- ] Decrease Font Size
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg)
                .border(BorderStroke(1.dp, pillBorder), RoundedCornerShape(10.dp))
                .clickable { onDecreaseFontSize() }
                .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A-",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    color = textColor
                )
            )
        }

        // Font Size Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) ScribeStarNeonRed.copy(alpha = 0.18f) else Color(0xFFFEF2F2))
                .border(BorderStroke(0.8.dp, ScribeStarNeonRed.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                .clickable { onResetFontSize() }
                .padding(horizontal = 6.dp, vertical = btnPaddingV),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${currentFontSizePt}pt",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (fontSize.value - 1f).sp,
                    color = if (isDark) ScribeStarNeonRed else Color(0xFF991B1B)
                )
            )
        }

        // [ A+ ] Increase Font Size
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg)
                .border(BorderStroke(1.dp, pillBorder), RoundedCornerShape(10.dp))
                .clickable { onIncreaseFontSize() }
                .padding(horizontal = btnPaddingH, vertical = btnPaddingV),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A+",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    color = textColor
                )
            )
        }
    }
}
