package com.seshat.writer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import kotlinx.coroutines.launch

@Composable
fun InlineAiPolishDialog(
    targetBlock: ScreenplayBlock,
    speakerName: String? = null,
    sceneContext: String = "",
    onApplyReplacement: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val config = remember { AiPreferences.load(context) }

    val isDialogue = targetBlock.type == BlockType.DIALOGUE
    val effectiveSpeaker = speakerName ?: "CHARACTER"

    var selectedStyle by remember {
        mutableStateOf(if (isDialogue) "Deepen Subtext" else "Active Verbs & Visual Punch")
    }
    var suggestedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var copyNotice by remember { mutableStateOf(false) }

    fun runGeneration(style: String) {
        if (!config.isConfigured) {
            errorMessage = "AI is not configured. Please enter your API key in Studio Settings."
            return
        }
        selectedStyle = style
        isLoading = true
        errorMessage = null
        copyNotice = false

        scope.launch {
            val result = if (isDialogue) {
                AiClient.deepenSubtext(config, effectiveSpeaker, targetBlock.text, sceneContext)
            } else {
                AiClient.punchUpAction(config, targetBlock.text, style, sceneContext)
            }
            isLoading = false
            result.fold(
                onSuccess = { suggestion ->
                    suggestedText = suggestion
                },
                onFailure = { err ->
                    errorMessage = err.message ?: "Failed to generate rewrite."
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (suggestedText.isBlank()) {
            runGeneration(selectedStyle)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                color = ObsidianBlack
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianSurface)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val headerTitle = if (isDialogue) {
                                "✨ SCRIPT DOCTOR: DIALOGUE & SUBTEXT ($effectiveSpeaker)"
                            } else {
                                "✨ SCRIPT DOCTOR: ACTION LINE PUNCH-UP"
                            }
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = ElectrumGold
                                )
                            )
                            Text(
                                text = "Compare original block vs. AI dramaturgical suggestion and accept diff.",
                                style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ScreenplayTextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = ObsidianSurfaceVariant)

                    // Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        // Style Presets Pills
                        Text(
                            text = "SELECT POLISH TECHNIQUE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ScreenplayTextMuted
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val presets = if (isDialogue) {
                            listOf(
                                "Deepen Subtext",
                                "Cut On-The-Nose",
                                "Sharpen Cadence",
                                "Deflective Irony"
                            )
                        } else {
                            listOf(
                                "Active Verbs & Visual Punch",
                                "Tighten (2-Lines Max)",
                                "Cut Unfilmable Thoughts",
                                "Visceral Tension"
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (style in presets) {
                                val isSelected = selectedStyle == style
                                Surface(
                                    color = if (isSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { runGeneration(style) }
                                        .border(
                                            1.dp,
                                            if (isSelected) ElectrumGold else ObsidianSurfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = style,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) ElectrumGold else ScreenplayTextDark,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Side-by-Side or Stacked Diff Comparison
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Original Box
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ORIGINAL BLOCK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = ScreenplayTextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = ObsidianSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = targetBlock.text.ifBlank { "(Empty block)" },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            color = ScreenplayTextDark,
                                            lineHeight = 20.sp
                                        ),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

                            // AI Suggestion Box
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI REWRITE ($selectedStyle)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = LightGold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = ObsidianSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, ElectrumGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (isLoading) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = ElectrumGold,
                                                    strokeWidth = 2.dp
                                                )
                                                Text(
                                                    text = "Polishing with $selectedStyle...",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = ElectrumGold)
                                                )
                                            }
                                        } else if (errorMessage != null) {
                                            Text(
                                                text = "Error: $errorMessage",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE57373))
                                            )
                                        } else {
                                            Text(
                                                text = suggestedText.ifBlank { "(No output)" },
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = ElectrumGold,
                                                    fontWeight = FontWeight.SemiBold,
                                                    lineHeight = 20.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (copyNotice) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✅ Suggestion copied to clipboard!",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4CAF50))
                            )
                        }
                    }

                    HorizontalDivider(color = ObsidianSurfaceVariant)

                    // Footer Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianSurface)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Copy button
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = suggestedText.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(suggestedText))
                                        copyNotice = true
                                    }
                                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "📋 COPY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ScreenplayTextDark
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }

                            // Try Another button
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = !isLoading) {
                                        runGeneration(selectedStyle)
                                    }
                                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "🔄 TRY ANOTHER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ScreenplayTextDark
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Discard button
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onDismiss() }
                                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "DISCARD",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ScreenplayTextMuted
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }

                            // Accept & Apply Button
                            Surface(
                                color = ElectrumGold,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = suggestedText.isNotBlank() && !isLoading) {
                                        onApplyReplacement(suggestedText)
                                        onDismiss()
                                    }
                            ) {
                                Text(
                                    text = "⚡ ACCEPT & REPLACE IN SCRIPT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianBlack
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
