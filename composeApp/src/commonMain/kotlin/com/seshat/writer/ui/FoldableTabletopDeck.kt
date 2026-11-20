package com.seshat.writer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.model.BlockType
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextMuted

/**
 * Ergonomic lower-half Writer's Deck shown automatically when a foldable phone
 * (Pixel 11 Pro Fold, Galaxy Z Fold 8) is half-folded horizontally into Tabletop / Flex Mode.
 */
@Composable
fun FoldableTabletopDeck(
    uiState: ScreenplayUiState,
    onSelectBlockType: (BlockType) -> Unit,
    onToggleDictation: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onToggleCorkboard: () -> Unit,
    onSaveProject: () -> Unit,
    onToggleKeyboard: () -> Unit,
    onOpenWorldTree: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val deckBg = if (isDark) ObsidianBlack else Color(0xFFF1F5F9)
    val surfaceBg = if (isDark) ObsidianSurface else Color.White
    val borderColor = if (isDark) ObsidianSurfaceVariant else Color(0xFFCBD5E1)
    val goldAccent = if (isDark) ElectrumGold else Color(0xFFB45309)
    val primaryText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)

    val focusedBlock = uiState.blocks.firstOrNull { it.id == uiState.focusedBlockId }
    val activeType = focusedBlock?.type ?: BlockType.ACTION
    val wordCount = if (uiState.projectFormat == ProjectFormat.NOVEL) {
        uiState.novelManuscript.totalWordCount()
    } else {
        uiState.blocks.sumOf { b ->
            b.text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
        }
    }
    val displayPages = if (uiState.projectFormat == ProjectFormat.NOVEL) {
        uiState.novelManuscript.estimatedPages()
    } else {
        uiState.pageCount
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(deckBg)
            .border(BorderStroke(1.dp, goldAccent.copy(alpha = 0.45f)), RoundedCornerShape(0.dp))
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Hinge-Aligned Gold Telemetry Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = goldAccent.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, goldAccent),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = "💻 FLEX WRITER'S DECK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${displayPages}p • $wordCount w",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = ScreenplayCourierFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = mutedText,
                        fontSize = 10.5.sp
                    )
                )
            }
        }

        // 2. Quick-Tap Screenplay Element Pills (Shown for Screenplay / TV / Stage / Comic)
        if (uiState.projectFormat != ProjectFormat.NOVEL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val elementKeys = listOf(
                    BlockType.SCENE_HEADING to "SCENE",
                    BlockType.ACTION to "ACTION",
                    BlockType.CHARACTER to "CHAR",
                    BlockType.DIALOGUE to "DIALOGUE",
                    BlockType.PARENTHETICAL to "PAREN",
                    BlockType.TRANSITION to "TRANS"
                )
                elementKeys.forEach { (type, label) ->
                    val isSelected = activeType == type
                    Surface(
                        color = if (isSelected) goldAccent else surfaceBg,
                        border = BorderStroke(1.dp, if (isSelected) goldAccent else borderColor),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(0.dp))
                            .clickable { onSelectBlockType(type) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = ScreenplayCourierFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else primaryText,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 3. Studio Quick-Action Control Deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isDictating = uiState.isDictating
            Surface(
                color = if (isDictating) Color(0xFFDC2626) else goldAccent.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, if (isDictating) Color(0xFFEF4444) else goldAccent),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(0.dp))
                    .clickable(onClick = onToggleDictation)
            ) {
                Text(
                    text = if (isDictating) "🎙 STOP DICTATION" else "🎙 Dictate",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isDictating) Color.White else goldAccent,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            DeckActionButton(
                label = "📖 Story Bible",
                bg = surfaceBg,
                border = borderColor,
                textColor = primaryText,
                onClick = onOpenStoryBible
            )
            DeckActionButton(
                label = if (uiState.isCorkboardOpen) "📝 Editor" else "🗂 Corkboard",
                bg = surfaceBg,
                border = borderColor,
                textColor = primaryText,
                onClick = onToggleCorkboard
            )
            if (onOpenWorldTree != null) {
                DeckActionButton(
                    label = "🪐 3D Hub",
                    bg = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF),
                    border = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                    textColor = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA),
                    onClick = onOpenWorldTree
                )
            }
            DeckActionButton(
                label = "💾 Save",
                bg = surfaceBg,
                border = borderColor,
                textColor = primaryText,
                onClick = onSaveProject
            )
            DeckActionButton(
                label = "⌨️ Keyboard",
                bg = surfaceBg,
                border = borderColor,
                textColor = primaryText,
                onClick = onToggleKeyboard
            )
        }

        // 4. Compact Active Context Footer
        val activeScene = uiState.blocks
            .takeWhile { it.id != uiState.focusedBlockId }
            .lastOrNull { it.type == BlockType.SCENE_HEADING }?.text
            ?: uiState.blocks.firstOrNull { it.type == BlockType.SCENE_HEADING }?.text
        if (!activeScene.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceBg.copy(alpha = 0.65f))
                    .border(BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ACTIVE SCENE: $activeScene",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = ScreenplayCourierFontFamily,
                        color = mutedText,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DeckActionButton(
    label: String,
    bg: Color,
    border: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = bg,
        border = BorderStroke(1.dp, border),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MainAppFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}
