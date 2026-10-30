package com.seshat.writer.firstdraft

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import java.util.Locale

/**
 * Full-screen airlock modal triggered when 48 hours elapse without meeting the survival quota.
 * Locks all project views (including Read-Only Bibles) until the user types 500 net-new words
 * into a raw scratchpad within 25 minutes of active session time.
 * Closing or minimizing the app resets the 500-word counter to 0.
 */
@Composable
fun PenaltySprintModal(
    state: FirstDraftState,
    onUpdateScratchpad: (String) -> Unit,
    onUseFreezeToken: () -> Unit,
    onOpenDecayReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentWords = FirstDraftController.countWords(state.penaltyScratchpadText)
    val progress = (currentWords / 500f).coerceIn(0f, 1f)
    val totalSec = (state.penaltySprintRemainingMs / 1000L).coerceAtLeast(0L)
    val min = totalSec / 60L
    val sec = totalSec % 60L
    val timeFormatted = String.format(Locale.US, "%02d:%02d", min, sec)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF5070508),
                        Color(0xFA180408),
                        Color(0xF5070508)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 820.dp)
                .fillMaxWidth()
                .neonBloom(
                    color = ScribeStarNeonRed,
                    cornerRadius = 24.dp,
                    glowRadius = 18.dp,
                    strokeWidth = 2.dp,
                    outerAlpha = 0.45f,
                    midAlpha = 0.75f
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0B0D14))
                .border(BorderStroke(2.dp, ScribeStarNeonRed), RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Surface(
                color = ScribeStarNeonRed.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ScribeStarNeonRed)
            ) {
                Text(
                    text = "🚨 48-HOUR QUOTA FAILED — DRAFT DECAYED 🚨",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = MainAppFontFamily,
                        color = ScribeStarNeonRed,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MANDATORY PENALTY SPRINT",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "All project views & Read-Only Bibles are locked. Type 500 net-new words within 25:00 of active session time to purge Decay. Minimizing or closing the window resets your word counter to 0.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFF161B26),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.7f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "⏱️ SPRINT CLOCK: ",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = timeFormatted,
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = ScribeStarNeonRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }

                Surface(
                    color = Color(0xFF161B26),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "🔥 NET-NEW WORDS: ",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "$currentWords / 500",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = ScribeStarNeonRed,
                trackColor = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(280.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF06080C))
                    .border(BorderStroke(1.2.dp, Color(0xFF334155)), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                if (state.penaltyScratchpadText.isEmpty()) {
                    Text(
                        text = "Start typing raw forward momentum here... Do not stop. Do not minimize the window. Reach 500 words to unlock your draft.",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF64748B),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    )
                }
                BasicTextField(
                    value = state.penaltyScratchpadText,
                    onValueChange = onUpdateScratchpad,
                    textStyle = TextStyle(
                        fontFamily = MainAppFontFamily,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    ),
                    cursorBrush = SolidColor(ScribeStarNeonRed),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFF28080E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ScribeStarNeonRed),
                    modifier = Modifier.clickable { onOpenDecayReceipt() }
                ) {
                    Text(
                        text = "📤 View Mandatory Decay Receipt",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                if (state.xpBalance >= XpShopItem.FREEZE_TOKEN.costXp) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = Color(0xFF0F2942),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier.clickable { onUseFreezeToken() }
                    ) {
                        Text(
                            text = "❄️ Spend 10,000 XP (Freeze Token)",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
