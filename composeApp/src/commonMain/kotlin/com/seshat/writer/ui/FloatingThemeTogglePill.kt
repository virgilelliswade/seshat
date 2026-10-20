package com.seshat.writer.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder

/**
 * Universal floating optical glass theme toggle pill.
 * Features red prismatic rim, smooth scale/glow animation, and crisp high-contrast
 * text in both Dark Mode and Light Mode.
 */
@Composable
fun FloatingThemeTogglePill(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 12.dp else 4.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ThemePillGlow"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.50f else 0.12f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ThemePillOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.80f else 0.25f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ThemePillMidAlpha"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = modifier
            .shadow(
                elevation = if (isLit) 12.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isLit) ScribeStarNeonRed else Color.Black.copy(alpha = 0.5f)
            )
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 20.dp,
                glowRadius = glowRadius,
                strokeWidth = if (isLit) 1.6.dp else 1.dp,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isLit) 1.0f else 0.75f
            )
            .clip(RoundedCornerShape(20.dp))
            .background(opticalPillBrush(isLit = isLit, isDark = isDark))
            .border(
                prismaticGlassBorder(
                    width = if (isLit) 1.6.dp else 1.dp,
                    isLit = isLit,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggleTheme
            )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isCompact) 10.dp else 14.dp,
                vertical = 7.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDark) "🌙" else "☀️",
                fontSize = if (isCompact) 14.sp else 13.sp
            )
            if (!isCompact) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDark) "Dark Mode" else "Light Mode",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = if (isDark) Color.White else (if (isLit) Color.White else Color(0xFF0F172A)),
                        fontWeight = if (isLit) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
