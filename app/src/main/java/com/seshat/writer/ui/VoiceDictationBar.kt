package com.seshat.writer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ai.AiConfig
import com.seshat.writer.model.BlockType
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted

@Composable
fun VoiceDictationBar(
    isListening: Boolean,
    isFormatting: Boolean,
    transcribedText: String,
    audioRmsLevel: Float,
    aiConfig: AiConfig,
    errorMessage: String? = null,
    onFormatAndInsert: () -> Unit,
    onInsertAsType: (BlockType) -> Unit,
    onStopListening: () -> Unit,
    onStartListening: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for recording status dot
    val infiniteTransition = rememberInfiniteTransition(label = "dictation_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, ElectrumGold.copy(alpha = 0.65f), RoundedCornerShape(16.dp)),
        color = ObsidianBlack.copy(alpha = 0.95f),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Status, Audio Waveform & AI Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing recording indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) Color(0xFFEF5350).copy(alpha = pulseAlpha)
                                else if (isFormatting) ElectrumGold.copy(alpha = pulseAlpha)
                                else Color.Gray
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isFormatting -> "✨ FORMATTING WITH GEMINI..."
                            isListening -> "🎙️ LIVE SCRIPT DICTATION"
                            else -> "🎙️ DICTATION PAUSED"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isListening) Color(0xFFEF5350) else ElectrumGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Audio level waveform bars
                    if (isListening) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.height(16.dp)
                        ) {
                            val barFactors = listOf(0.4f, 0.7f, 1.0f, 0.85f, 0.5f)
                            for (factor in barFactors) {
                                val barHeight = (4.dp + (14.dp * audioRmsLevel * factor)).coerceIn(3.dp, 16.dp)
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(ElectrumGold)
                                )
                            }
                        }
                    }
                }

                // AI Model Tag & Close button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val modelLabel = when {
                        !aiConfig.isEnabled -> "Offline STT"
                        aiConfig.modelName.contains("3.8") -> "Gemini 3.8 Flash"
                        aiConfig.modelName.contains("4") -> "Gemini 4"
                        else -> aiConfig.modelName
                    }

                    Surface(
                        color = ObsidianSurface,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.border(0.5.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = "⚡ $modelLabel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = LightGold,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dictation",
                            tint = ScreenplayTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Transcription Display Area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp)),
                color = ObsidianSurface.copy(alpha = 0.85f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (isFormatting) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ElectrumGold,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Gemini is structuring dialogue, character cues, and action...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = LightGold,
                                    fontFamily = ScreenplayCourierFontFamily
                                )
                            )
                        }
                    } else if (transcribedText.isNotBlank()) {
                        Text(
                            text = transcribedText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ScreenplayTextDark,
                                fontFamily = ScreenplayCourierFontFamily,
                                lineHeight = 20.sp
                            )
                        )
                    } else {
                        Text(
                            text = if (isListening) "Speak your scene... (e.g. 'Interior coffee shop day. Marcus says where is she?')" else "Microphone ready. Press Start or Googlebook Mic key to speak.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScreenplayTextMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontFamily = ScreenplayCourierFontFamily
                            )
                        )
                    }
                }
            }

            // Error display if any
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ $errorMessage",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFEF5350),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions & Quick Insert Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Quick Format Direct Type Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Quick:",
                        style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                    )

                    Surface(
                        color = ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = transcribedText.isNotBlank()) {
                                onInsertAsType(BlockType.ACTION)
                            }
                    ) {
                        Text(
                            text = "Action",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (transcribedText.isNotBlank()) LightGold else ScreenplayTextMuted,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = transcribedText.isNotBlank()) {
                                onInsertAsType(BlockType.DIALOGUE)
                            }
                    ) {
                        Text(
                            text = "Dialogue",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (transcribedText.isNotBlank()) LightGold else ScreenplayTextMuted,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = transcribedText.isNotBlank()) {
                                onInsertAsType(BlockType.SCENE_HEADING)
                            }
                    ) {
                        Text(
                            text = "Slugline",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (transcribedText.isNotBlank()) LightGold else ScreenplayTextMuted,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Right: Primary Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isListening) {
                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onStopListening() }
                        ) {
                            Text(
                                text = "⏹️ Pause Mic",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ScreenplayTextDark,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onStartListening() }
                        ) {
                            Text(
                                text = "🎙️ Resume Mic",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectrumGold,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Format and Insert Button (Gemini AI Powered)
                    Surface(
                        color = if (transcribedText.isNotBlank() && !isFormatting) ElectrumGold else ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = transcribedText.isNotBlank() && !isFormatting) {
                                onFormatAndInsert()
                            }
                    ) {
                        Text(
                            text = "✨ Format & Insert",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (transcribedText.isNotBlank() && !isFormatting) ObsidianBlack else ScreenplayTextMuted,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Keyboard shortcut hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Googlebook Mic Key • Ctrl+D • Esc to dismiss",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
