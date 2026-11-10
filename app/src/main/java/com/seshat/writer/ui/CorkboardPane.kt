package com.seshat.writer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.corkboard.CorkboardAct
import com.seshat.writer.corkboard.SceneCard
import com.seshat.writer.firstdraft.FirstDraftTextMetrics
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.DeskBackground
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.StudioIconType
import com.seshat.writer.ui.theme.StudioMonoFontFamily
import com.seshat.writer.ui.theme.StudioMonoIcon

private val CARD_PALETTE = listOf(
    "#F59E0B", // Electrum Gold
    "#3B82F6", // Sapphire Blue
    "#10B981", // Emerald Green
    "#EC4899", // Rose Red
    "#8B5CF6", // Amethyst Purple
    "#64748B"  // Slate Gray
)

@Composable
fun CorkboardPane(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    isCompactScreen: Boolean = false,
    onSaveProject: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val corkboard = uiState.corkboardDoc
    val cards = uiState.sceneCards
    val isSideBySide = uiState.isCorkboardSideBySide
    var showTemplateDialog by remember { mutableStateOf(false) }

    if (showTemplateDialog) {
        CorkboardTemplateDialog(
            onSelectTemplate = { template ->
                viewModel.applyCorkboardTemplate(context, template)
                showTemplateDialog = false
                Toast.makeText(context, "Applied beat template: ${template.name}", Toast.LENGTH_SHORT).show()
            },
            onClearBoard = {
                viewModel.clearCorkboardCards(context)
                showTemplateDialog = false
                Toast.makeText(context, "Corkboard cleared to blank slate", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showTemplateDialog = false }
        )
    }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .background(DeskBackground)
            .fillMaxHeight()
    ) {
        // ==========================================
        // 1. TOP HEADER & ACT CONTROLS
        // ==========================================
        if (isCompactScreen) {
            Surface(
                color = ObsidianSurface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    // Row 1: Workspace Switcher + Save + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Switcher tabs
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            val editorLabel = if (uiState.projectFormat == ProjectFormat.NOVEL) "📚 Novel" else if (uiState.projectFormat == ProjectFormat.TV_SERIES) "🎬 Ep ${uiState.tvSeriesProject?.getActiveEpisode()?.episodeNumber ?: "101"}" else "🎬 Script"
                            Surface(
                                color = ObsidianSurfaceVariant,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable { viewModel.closeCorkboard() }
                            ) {
                                Text(
                                    text = editorLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }

                            // Board (active)
                            Surface(
                                color = ElectrumGold.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, ElectrumGold)
                            ) {
                                Text(
                                    text = "📌 Board",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectrumGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }

                            // Lore
                            Surface(
                                color = ObsidianSurfaceVariant,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    viewModel.closeCorkboard()
                                    viewModel.openStoryBible(context)
                                }
                            ) {
                                Text(
                                    text = "📖 Lore",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Right: Save & Close
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = ElectrumGold,
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier.clickable { onSaveProject() }
                            ) {
                                Text(
                                    text = "💾 Save",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ObsidianBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = { viewModel.closeCorkboard() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Corkboard",
                                    tint = ScreenplayTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Acts selector + AI Summarize
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Acts:",
                                style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            listOf(3, 4, 5).forEach { actPreset ->
                                val isSelected = corkboard.actCount == actPreset
                                Surface(
                                    color = if (isSelected) ElectrumGold.copy(alpha = 0.25f) else ObsidianBlack,
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(1.dp, if (isSelected) ElectrumGold else ObsidianSurfaceVariant),
                                    modifier = Modifier.clickable { viewModel.setCorkboardActCount(context, actPreset) }
                                ) {
                                    Text(
                                        text = "$actPreset",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) ElectrumGold else ScreenplayTextMuted
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                                modifier = Modifier.clickable {
                                    if (corkboard.actCount < 9) {
                                        viewModel.setCorkboardActCount(context, corkboard.actCount + 1)
                                    }
                                }
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { showTemplateDialog = true }
                        ) {
                            Text(
                                text = "📋 Templates ▾",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectrumGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        val isAnyAiLoading = cards.any { it.isAiSummarizing }
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, LightGold.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable(enabled = !isAnyAiLoading) {
                                viewModel.summarizeAllSceneCardsWithAi(context)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                if (isAnyAiLoading) {
                                    CircularProgressIndicator(strokeWidth = 1.5.dp, modifier = Modifier.size(12.dp), color = LightGold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "AI...", style = MaterialTheme.typography.labelSmall.copy(color = LightGold, fontSize = 11.sp))
                                } else {
                                    Text(text = "✨ AI Summarize", style = MaterialTheme.typography.labelSmall.copy(color = LightGold, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                color = ObsidianSurface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title and scene count and Workspace Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudioMonoIcon(type = StudioIconType.CORKBOARD, tint = ScribeStarNeonRed, size = 14.dp)
                        Text(
                            text = "CORKBOARD OUTLINE",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ScreenplayTextDark,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = "${cards.size} SCENES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = ScreenplayCourierFontFamily,
                                color = ScreenplayTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        )

                        Box(modifier = Modifier.width(1.dp).height(18.dp).background(ObsidianSurfaceVariant))

                        // Unified Workspace Switcher Strip
                        val editorLabel = if (uiState.projectFormat == ProjectFormat.NOVEL) "Novel" else if (uiState.projectFormat == ProjectFormat.TV_SERIES) "Ep ${uiState.tvSeriesProject?.getActiveEpisode()?.episodeNumber ?: "101"}" else "Script"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .background(ObsidianBlack)
                                .border(BorderStroke(1.dp, ObsidianSurfaceVariant), RoundedCornerShape(0.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .clickable { viewModel.closeCorkboard() }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                StudioMonoIcon(type = StudioIconType.SCRIPT, tint = ScreenplayTextMuted, size = 12.5.dp)
                                Text(
                                    text = editorLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(22.dp).background(ObsidianSurfaceVariant))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .background(ScribeStarNeonRed.copy(alpha = 0.14f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                StudioMonoIcon(type = StudioIconType.CORKBOARD, tint = ScribeStarNeonRed, size = 12.5.dp)
                                Text(
                                    text = "Corkboard",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(22.dp).background(ObsidianSurfaceVariant))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.closeCorkboard()
                                        viewModel.openStoryBible(context)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                StudioMonoIcon(type = StudioIconType.BIBLE, tint = ScreenplayTextMuted, size = 12.5.dp)
                                Text(
                                    text = "Story Bible",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Center: Joined Act Count Strip [ 3 | 4 | 5 | + ]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ACTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = ScreenplayCourierFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ScreenplayTextMuted,
                                fontSize = 10.5.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .background(ObsidianBlack)
                                .border(BorderStroke(1.dp, ObsidianSurfaceVariant), RoundedCornerShape(0.dp))
                        ) {
                            listOf(3, 4, 5).forEach { actPreset ->
                                val isSelected = corkboard.actCount == actPreset
                                Text(
                                    text = "$actPreset",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = ScreenplayCourierFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ScribeStarNeonRed else ScreenplayTextMuted
                                    ),
                                    modifier = Modifier
                                        .background(if (isSelected) ScribeStarNeonRed.copy(alpha = 0.14f) else Color.Transparent)
                                        .clickable { viewModel.setCorkboardActCount(context, actPreset) }
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(ObsidianSurfaceVariant))
                            }
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = ScreenplayCourierFontFamily,
                                    color = ScreenplayTextDark,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clickable {
                                        if (corkboard.actCount < 9) {
                                            viewModel.setCorkboardActCount(context, corkboard.actCount + 1)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Right Actions: AI Summarize All, Templates, Split/Full, Save, Close
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isAnyAiLoading = cards.any { it.isAiSummarizing }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable(enabled = !isAnyAiLoading) {
                                    viewModel.summarizeAllSceneCardsWithAi(context)
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            if (isAnyAiLoading) {
                                CircularProgressIndicator(
                                    strokeWidth = 1.5.dp,
                                    modifier = Modifier.size(12.dp),
                                    color = ScribeStarNeonRed
                                )
                                Text(
                                    text = "Summarizing...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                                )
                            } else {
                                StudioMonoIcon(type = StudioIconType.SPARKLE, tint = ScreenplayTextMuted, size = 13.dp)
                                Text(
                                    text = "AI Summarize",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable { showTemplateDialog = true }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            StudioMonoIcon(type = StudioIconType.LIST, tint = ScreenplayTextMuted, size = 13.dp)
                            Text(
                                text = "Templates ▾",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ScreenplayTextMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable { viewModel.toggleCorkboardSideBySide() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            StudioMonoIcon(type = StudioIconType.PAGE, tint = ScreenplayTextMuted, size = 13.dp)
                            Text(
                                text = if (isSideBySide) "Full Board" else "Side-by-Side",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ScreenplayTextMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            color = ScribeStarNeonRed,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.clickable { onSaveProject() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                StudioMonoIcon(type = StudioIconType.SAVE, tint = Color.White, size = 13.dp)
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.closeCorkboard() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Corkboard",
                                tint = ScreenplayTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = ObsidianSurfaceVariant)

        // First Draft Airlock & Fog of War Banner
        val canEditOutline = uiState.firstDraftState.canEditOutline()
        val isArcade = uiState.firstDraftState.isArcadeLocked
        val unlockedSceneSlots = if (isArcade) uiState.firstDraftState.unlockedCorkboardSceneSlots else Int.MAX_VALUE

        if (isArcade) {
            Surface(
                color = if (!canEditOutline) Color(0xFF280811) else Color(0xFF0A2418),
                border = BorderStroke(1.dp, if (!canEditOutline) Color(0xFFFF1E42) else Color(0xFF10B981)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (!canEditOutline) {
                        "🔒 OUTLINE AIRLOCK SEALED (READ-ONLY) • Fog of War: $unlockedSceneSlots Scene Slots Visible (Write scenes in DRAFT to unlock rolling N+5..N+7 slots)"
                    } else {
                        "🔓 OUTLINE STAGE ACTIVE • Fog of War: ${cards.size}/$unlockedSceneSlots Scene Slots Unlocked • Max 2 sentences per Macro-Beat"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (!canEditOutline) Color(0xFFFFB4AB) else Color(0xFF6EE7B7),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // ==========================================
        // 2. HORIZONTAL ACT SWIMLANES / COLUMNS
        // ==========================================
        val horizontalScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                corkboard.acts.forEach { act ->
                    val actCards = cards.filter { it.actIndex == act.actIndex }
                    ActColumn(
                        act = act,
                        totalActs = corkboard.actCount,
                        cards = actCards,
                        allCards = cards,
                        isReadOnly = !canEditOutline,
                        unlockedSceneSlots = unlockedSceneSlots,
                        onUpdateActTitle = { newTitle ->
                            if (canEditOutline) {
                                viewModel.updateCorkboardActTitle(context, act.actIndex, newTitle, act.subtitle)
                            }
                        },
                        onSelectScene = { sceneNumber ->
                            val targetSceneItem = uiState.scenes.find { it.index == sceneNumber }
                            if (targetSceneItem != null) {
                                viewModel.selectScene(targetSceneItem)
                            }
                        },
                        onUpdateSummary = { blockId, summary ->
                            if (!canEditOutline) return@ActColumn
                            val clipped = if (isArcade) {
                                if (uiState.projectFormat == ProjectFormat.NOVEL) {
                                    FirstDraftTextMetrics.clipToWordLimit(summary, 50)
                                } else {
                                    FirstDraftTextMetrics.clipToSentenceLimit(summary, 2)
                                }
                            } else summary
                            viewModel.updateSceneCardSummary(context, blockId, clipped)
                        },
                        onUpdateTitle = { blockId, newTitle ->
                            if (canEditOutline) {
                                viewModel.updateSceneCardTitle(context, blockId, newTitle)
                            }
                        },
                        onUpdateColor = { blockId, color ->
                            if (canEditOutline) {
                                viewModel.updateSceneCardColor(context, blockId, color)
                            }
                        },
                        onAddCard = {
                            if (!canEditOutline) {
                                Toast.makeText(context, "🔒 Outline Airlock is sealed!", Toast.LENGTH_SHORT).show()
                            } else if (isArcade && cards.size >= unlockedSceneSlots) {
                                Toast.makeText(
                                    context,
                                    "🔒 Fog of War: Only $unlockedSceneSlots scene slots unlocked right now. Advance to DRAFT and write scenes to reveal N+5..N+7!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                viewModel.addCorkboardCard(context, act.actIndex)
                            }
                        },
                        onDeleteCard = { blockId ->
                            if (canEditOutline) {
                                viewModel.deleteCorkboardCard(context, blockId)
                            }
                        },
                        onMoveSceneUp = { card ->
                            if (!canEditOutline) return@ActColumn
                            val currentPos0 = cards.indexOfFirst { it.sceneHeadingBlockId == card.sceneHeadingBlockId }
                            if (currentPos0 > 0) {
                                viewModel.reorderCorkboardCard(context, currentPos0, currentPos0 - 1)
                            }
                        },
                        onMoveSceneDown = { card ->
                            if (!canEditOutline) return@ActColumn
                            val currentPos0 = cards.indexOfFirst { it.sceneHeadingBlockId == card.sceneHeadingBlockId }
                            if (currentPos0 >= 0 && currentPos0 < cards.size - 1) {
                                viewModel.reorderCorkboardCard(context, currentPos0, currentPos0 + 1)
                            }
                        },
                        onMoveToAct = { blockId, targetActIndex ->
                            if (canEditOutline) {
                                viewModel.moveSceneCardToAct(context, blockId, targetActIndex)
                            }
                        },
                        onSummarizeWithAi = { blockId ->
                            viewModel.summarizeSceneCardWithAi(context, blockId)
                        },
                        modifier = Modifier
                            .width(if (isSideBySide) 290.dp else 330.dp)
                            .fillMaxHeight()
                    )
                }
            }

            FloatingThemeTogglePill(
                isDark = LocalSeshatDarkTheme.current,
                onToggleTheme = { viewModel.toggleThemeMode(context) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 12.dp)
            )
        }
    }
}

/**
 * An individual Act column on the Corkboard containing its vertical stack of scene cards.
 */
@Composable
private fun ActColumn(
    act: CorkboardAct,
    totalActs: Int,
    cards: List<SceneCard>,
    allCards: List<SceneCard>,
    isReadOnly: Boolean = false,
    unlockedSceneSlots: Int = Int.MAX_VALUE,
    onUpdateActTitle: (String) -> Unit,
    onSelectScene: (Int) -> Unit,
    onUpdateSummary: (String, String) -> Unit,
    onUpdateTitle: (String, String) -> Unit,
    onUpdateColor: (String, String) -> Unit,
    onAddCard: () -> Unit,
    onDeleteCard: (String) -> Unit,
    onMoveSceneUp: (SceneCard) -> Unit,
    onMoveSceneDown: (SceneCard) -> Unit,
    onMoveToAct: (String, Int) -> Unit,
    onSummarizeWithAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, ObsidianSurfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Act Header with Editable Title, Scene Count, and + Add Beat Button
            var isEditingTitle by remember { mutableStateOf(false) }
            var titleInput by remember { mutableStateOf(act.title) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBlack, RoundedCornerShape(0.dp))
                    .border(BorderStroke(1.dp, ObsidianSurfaceVariant), RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditingTitle) {
                    BasicTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        textStyle = MaterialTheme.typography.titleSmall.copy(
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(ElectrumGold),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            isEditingTitle = false
                            if (titleInput.isNotBlank()) onUpdateActTitle(titleInput)
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Text(text = "✓", color = LightGold, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = act.title.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                titleInput = act.title
                                isEditingTitle = true
                            }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Surface(
                        color = ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "${cards.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LightGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onAddCard,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Beat to Act",
                            tint = ElectrumGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            if (act.subtitle.isNotBlank()) {
                Text(
                    text = act.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vertically scrollable stack of scene cards for this Act
            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No beats in ${act.title}.\nAdd a beat card or choose a template.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScreenplayTextMuted.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onAddCard() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ElectrumGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Beat Card",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectrumGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards, key = { it.sceneHeadingBlockId }) { card ->
                        val globalIdx = allCards.indexOfFirst { it.sceneHeadingBlockId == card.sceneHeadingBlockId }
                        val isFogged = globalIdx >= unlockedSceneSlots
                        if (isFogged) {
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🔒 SCENE #${card.sceneNumber} — FOG OF WAR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Complete earlier scenes in DRAFT to unlock rolling N+5 to N+7 Scene Cards.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ScreenplayTextMuted,
                                            fontSize = 10.5.sp
                                        ),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            val cardColor = remember(card.colorTag) {
                                try {
                                    Color(android.graphics.Color.parseColor(card.colorTag))
                                } catch (_: Exception) {
                                    ElectrumGold
                                }
                            }

                            CorkboardSceneCard(
                                card = card,
                                cardColor = cardColor,
                                totalActs = totalActs,
                                isReadOnly = isReadOnly,
                                onSelectScene = { onSelectScene(card.sceneNumber) },
                                onUpdateTitle = { newTitle -> onUpdateTitle(card.sceneHeadingBlockId, newTitle) },
                                onUpdateSummary = { newSummary -> onUpdateSummary(card.sceneHeadingBlockId, newSummary) },
                                onUpdateColor = { newColor -> onUpdateColor(card.sceneHeadingBlockId, newColor) },
                                onDelete = { onDeleteCard(card.sceneHeadingBlockId) },
                                onMoveUp = { onMoveSceneUp(card) },
                                onMoveDown = { onMoveSceneDown(card) },
                                onMoveToAct = { targetAct -> onMoveToAct(card.sceneHeadingBlockId, targetAct) },
                                onSummarizeWithAi = { onSummarizeWithAi(card.sceneHeadingBlockId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A tactile corkboard scene index card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CorkboardSceneCard(
    card: SceneCard,
    cardColor: Color,
    totalActs: Int,
    isReadOnly: Boolean = false,
    onSelectScene: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateSummary: (String) -> Unit,
    onUpdateColor: (String) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveToAct: (Int) -> Unit,
    onSummarizeWithAi: () -> Unit
) {
    var isColorPaletteOpen by remember { mutableStateOf(false) }
    var isActDropdownOpen by remember { mutableStateOf(false) }

    // Scrivener / Arc Studio-grade physical white index card (#FFFFFF in both dark and light mode)
    Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            // 3px top color rule across top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(cardColor)
            )

            // Index Card Header Strip (Courier Prime bold slugline on #FFFFFF)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${card.sceneNumber}.",
                        style = TextStyle(
                            fontFamily = ScreenplayCourierFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = cardColor
                        ),
                        modifier = Modifier
                            .clickable { onSelectScene() }
                            .padding(end = 6.dp)
                    )

                    var isEditingCardTitle by remember { mutableStateOf(false) }
                    var titleText by remember(card.headingText) { mutableStateOf(card.headingText) }

                    if (isEditingCardTitle) {
                        BasicTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            textStyle = TextStyle(
                                fontFamily = ScreenplayCourierFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF0F172A)
                            ),
                            cursorBrush = SolidColor(ScribeStarNeonRed),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                isEditingCardTitle = false
                                if (titleText.isNotBlank()) onUpdateTitle(titleText)
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Text(text = "✓", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        Text(
                            text = card.headingText,
                            style = TextStyle(
                                fontFamily = ScreenplayCourierFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF0F172A)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable {
                                    titleText = card.headingText
                                    isEditingCardTitle = true
                                }
                        )
                    }
                }

                // Color accent picker square swatch
                Box {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(0.dp))
                            .background(cardColor)
                            .clickable { isColorPaletteOpen = true }
                            .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(0.dp))
                    )

                    DropdownMenu(
                        expanded = isColorPaletteOpen,
                        onDismissRequest = { isColorPaletteOpen = false }
                    ) {
                        CARD_PALETTE.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RoundedCornerShape(0.dp))
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = hex, color = ScreenplayTextDark)
                                    }
                                },
                                onClick = {
                                    onUpdateColor(hex)
                                    isColorPaletteOpen = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))

            // Index Card Body (#FFFFFF white surface, #334155 readable synopsis ink)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                if (card.prompt.isNotBlank()) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = card.prompt,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF475569),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                var summaryText by remember(card.summary) { mutableStateOf(card.summary) }

                BasicTextField(
                    value = summaryText,
                    readOnly = isReadOnly,
                    onValueChange = {
                        if (!isReadOnly) {
                            summaryText = it
                            onUpdateSummary(it)
                        }
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF334155),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    ),
                    cursorBrush = SolidColor(ScribeStarNeonRed),
                    decorationBox = { innerTextField ->
                        if (summaryText.isBlank()) {
                            Text(
                                text = "Write beat notes / scene synopsis...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )

                if (card.charactersInScene.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        card.charactersInScene.take(4).forEach { charName ->
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = charName,
                                    style = TextStyle(
                                        fontFamily = ScreenplayCourierFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (card.charactersInScene.size > 4) {
                            Text(
                                text = "+${card.charactersInScene.size - 4}",
                                style = TextStyle(fontSize = 9.sp, color = Color(0xFF64748B)),
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))

            // Index Card Footer Bar (#F8FAFC light slate footer with monospace runtime & controls)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "~${card.estimatedMinutes}:00",
                    style = TextStyle(
                        fontFamily = StudioMonoFontFamily,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (card.isAiSummarizing) {
                        CircularProgressIndicator(
                            strokeWidth = 1.dp,
                            modifier = Modifier.size(13.dp),
                            color = ScribeStarNeonRed
                        )
                    } else {
                        IconButton(
                            onClick = onSummarizeWithAi,
                            modifier = Modifier.size(22.dp)
                        ) {
                            StudioMonoIcon(type = StudioIconType.SPARKLE, tint = Color(0xFF64748B), size = 12.dp)
                        }
                    }

                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Scene Up",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Scene Down",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Box {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { isActDropdownOpen = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACT ${card.actIndex + 1}",
                                    style = TextStyle(
                                        fontFamily = StudioMonoFontFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change Act",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isActDropdownOpen,
                            onDismissRequest = { isActDropdownOpen = false }
                        ) {
                            for (targetAct in 0 until totalActs) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Move to Act ${targetAct + 1}",
                                            fontWeight = if (targetAct == card.actIndex) FontWeight.Bold else FontWeight.Normal,
                                            color = if (targetAct == card.actIndex) ScribeStarNeonRed else ScreenplayTextDark
                                        )
                                    },
                                    onClick = {
                                        onMoveToAct(targetAct)
                                        isActDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Card",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}


