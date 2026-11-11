package com.seshat.writer.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ai.AiConfig
import com.seshat.writer.ai.SmartDictationParser
import com.seshat.writer.model.BlockType
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.floatingGlassIsland
import com.seshat.writer.ui.theme.opticalGlassBrush
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder

@Composable
fun VoiceDictationBar(
    isListening: Boolean,
    isFormatting: Boolean,
    transcribedText: String,
    audioRmsLevel: Float,
    aiConfig: AiConfig,
    isAutoInsert: Boolean = true,
    activeCharacters: List<String> = emptyList(),
    currentBlockType: BlockType = BlockType.ACTION,
    errorMessage: String? = null,
    onTextChange: (String) -> Unit = {},
    onToggleAutoInsert: () -> Unit = {},
    onFormatAndInsert: () -> Unit,
    onInsertAsType: (BlockType) -> Unit,
    onStopListening: () -> Unit,
    onStartListening: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }

    val liveParseResult = remember(transcribedText, activeCharacters, currentBlockType) {
        if (transcribedText.isBlank()) null
        else SmartDictationParser.parseDetailed(transcribedText, activeCharacters, currentBlockType)
    }
    val previewSummary = remember(liveParseResult) {
        liveParseResult?.let { SmartDictationParser.formatPreviewTag(it) }.orEmpty()
    }

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

    Box(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .floatingGlassIsland(
                isDark = isDark,
                elevation = 18.dp,
                accentColor = ScribeStarNeonRed,
                isHighlighted = isListening || isFormatting
            )
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(0.dp))
                            .background(
                                if (isListening) Color(0xFFEF4444).copy(alpha = pulseAlpha)
                                else if (isFormatting) ScribeStarNeonRed.copy(alpha = pulseAlpha)
                                else ScreenplayTextMuted
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isFormatting -> "✨ SMART FORMATTING..."
                            isListening -> "🎙️ SMART SCRIPT DICTATION (LIVE)"
                            else -> "🎙️ SMART DICTATION READY"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = if (isListening) Color(0xFFEF4444) else ScribeStarNeonRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Live Audio Waveform Bars
                    if (isListening) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.height(16.dp)
                        ) {
                            val barFactors = listOf(0.4f, 0.7f, 1.0f, 0.85f, 0.5f)
                            for (factor in barFactors) {
                                val barHeight = (3.dp + (13.dp * audioRmsLevel.coerceAtLeast(0.12f) * factor)).coerceIn(3.dp, 16.dp)
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(0.dp))
                                        .background(ScribeStarNeonRed)
                                )
                            }
                        }
                    }
                }

                // Auto-Insert Toggle + AI Model Tag + Close button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(if (isAutoInsert) 4.dp else 1.dp, RoundedCornerShape(0.dp))
                            .clip(RoundedCornerShape(0.dp))
                            .background(opticalPillBrush(isLit = isAutoInsert, isDark = isDark))
                            .border(prismaticGlassBorder(width = 1.dp, isLit = isAutoInsert, isDark = isDark), RoundedCornerShape(0.dp))
                            .clickable(onClick = onToggleAutoInsert)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isAutoInsert) "🔁 Auto-Insert: ON" else "Auto-Insert: OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontSize = 10.sp,
                                color = if (isAutoInsert && !isDark) Color.White else if (isAutoInsert) Color.White else ScreenplayTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    val modelLabel = when {
                        !aiConfig.isEnabled || !aiConfig.isConfigured -> "Smart Offline"
                        aiConfig.modelName.contains("3.8") -> "Gemini 3.8"
                        aiConfig.modelName.contains("2.5") -> "Gemini 2.5"
                        else -> aiConfig.modelName
                    }

                    Surface(
                        color = ObsidianSurface,
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.dp, ObsidianSurfaceVariant)
                    ) {
                        Text(
                            text = "⚡ $modelLabel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontSize = 10.sp,
                                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A),
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

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

            // Editable Live Transcription Display (Works with Android SpeechRecognizer, Gboard Voice, macOS Fn-Fn Dictation, or Mic Audio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(0.dp))
                    .background(ObsidianBlack.copy(alpha = if (isDark) 0.85f else 0.96f))
                    .border(BorderStroke(1.dp, ObsidianSurfaceVariant), RoundedCornerShape(0.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (isFormatting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = ScribeStarNeonRed,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Formatting speech into Fountain screenplay blocks...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScreenplayTextDark,
                                fontFamily = ScreenplayCourierFontFamily
                            )
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (transcribedText.isEmpty()) {
                            Text(
                                text = "Speak or dictate (e.g. 'Interior, bedroom, night' · 'Next line action: Elena enters' · 'Marcus says hello')",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ScreenplayTextMuted,
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = ScreenplayCourierFontFamily
                                )
                            )
                        }
                        BasicTextField(
                            value = transcribedText,
                            onValueChange = onTextChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = ScreenplayTextDark,
                                fontFamily = ScreenplayCourierFontFamily,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(ScribeStarNeonRed),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { onFormatAndInsert() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
                }
            }

            // Live Smart Parse Preview Strip
            if (previewSummary.isNotBlank() && !isFormatting) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(0.dp))
                        .background(ScribeStarNeonRed.copy(alpha = if (isDark) 0.14f else 0.08f))
                        .border(BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.45f)), RoundedCornerShape(0.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "⚡ SMART PARSE → $previewSummary",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = ScreenplayCourierFontFamily,
                            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Error display if any
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ $errorMessage",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFEF4444),
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
                // Left: Quick Force-Element Override Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Force:",
                        style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                    )

                    listOf(
                        "Action" to BlockType.ACTION,
                        "Dialogue" to BlockType.DIALOGUE,
                        "Slugline" to BlockType.SCENE_HEADING
                    ).forEach { (label, type) ->
                        Box(
                            modifier = Modifier
                                .shadow(3.dp, RoundedCornerShape(0.dp))
                                .clip(RoundedCornerShape(0.dp))
                                .background(opticalGlassBrush(glassOpacity = if (isDark) 0.85f else 0.92f))
                                .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), RoundedCornerShape(0.dp))
                                .clickable(enabled = !isFormatting) { onInsertAsType(type) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Right: Mic Control + Smart Insert CTA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(0.dp))
                            .clip(RoundedCornerShape(0.dp))
                            .background(opticalGlassBrush(glassOpacity = if (isDark) 0.85f else 0.92f))
                            .border(prismaticGlassBorder(width = 1.dp, isLit = isListening, isDark = isDark), RoundedCornerShape(0.dp))
                            .clickable { if (isListening) onStopListening() else onStartListening() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isListening) "⏹️ Stop Mic" else "🎙️ Resume Mic",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Smart Format and Insert Button
                    val canInsert = (transcribedText.isNotBlank() || isListening) && !isFormatting
                    Box(
                        modifier = Modifier
                            .shadow(if (canInsert) 7.dp else 2.dp, RoundedCornerShape(0.dp))
                            .clip(RoundedCornerShape(0.dp))
                            .background(opticalPillBrush(isPrimary = canInsert, isDark = isDark))
                            .border(prismaticGlassBorder(width = 1.dp, isLit = canInsert, isDark = isDark), RoundedCornerShape(0.dp))
                            .clickable(enabled = canInsert) { onFormatAndInsert() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✨ Smart Insert",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (canInsert) Color.White else ScreenplayTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Voice command & keyboard hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Voice commands: 'Interior, bedroom, night' · 'Next line action' · 'Character Marcus' · 'Stop dictation'",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mac ⌘D / Fn×2 • Esc",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 9.5.sp
                    )
                )
            }
        }
    }
}
