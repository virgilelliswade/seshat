package com.seshat.writer.universe

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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.bible.TvEpisodeOutline
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.floatingGlassIsland

/**
 * Centerpiece "The Hub / World Tree" screen for Universe projects (Cinematic, TV, Novel, Blended).
 * Displays the Master Lore summary, universe-level navigation (Master Lore Doc, Corkboard/Outlines,
 * Pitch Deck, Writers' Room), and the interactive branch tree of individual projects.
 * Clicking any branch climbs down into that project's editor or outline.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorldTreeHubPane(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    onSaveProject: () -> Unit,
    isCompactScreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalSeshatDarkTheme.current
    val scrollState = rememberScrollState()

    val blended = uiState.blendedUniverseProject
    val tvSeries = uiState.tvSeriesProject
    val bible = uiState.storyBible

    val theme = blended?.theme ?: UniverseTheme.ARBOREAL
    val universeTitle = blended?.title
        ?: tvSeries?.title
        ?: bible.scriptTitle.ifBlank { uiState.title.ifBlank { "UNTITLED UNIVERSE" } }

    val universeKindLabel = when {
        blended != null -> theme.engineName
        tvSeries != null || uiState.projectFormat == ProjectFormat.TV_SERIES -> "TELEVISION UNIVERSE"
        uiState.projectFormat == ProjectFormat.NOVEL -> "NOVEL UNIVERSE"
        else -> "CINEMATIC UNIVERSE"
    }

    val defaultBranchFormat = when {
        blended?.universeKind?.uppercase() == "NOVEL" -> ProjectFormat.NOVEL
        blended?.universeKind?.uppercase() == "TELEVISION" -> ProjectFormat.TV_SERIES
        else -> ProjectFormat.SCREENPLAY
    }

    var isEditingTitle by remember(universeTitle) { mutableStateOf(false) }
    var draftUniverseTitle by remember(universeTitle) { mutableStateOf(universeTitle) }
    var showAddBranchRow by remember { mutableStateOf(false) }
    var newBranchTitle by remember { mutableStateOf("") }
    var newBranchFormat by remember(defaultBranchFormat) { mutableStateOf(defaultBranchFormat) }
    var newBranchRole by remember { mutableStateOf(BranchRole.MAIN_SPINE) }
    var selectedParentId by remember { mutableStateOf<String?>(null) }
    var selectedEraTag by remember(theme) { mutableStateOf(blended?.eras?.firstOrNull()?.name ?: "Epoch I") }

    var arcDraft by remember(bible.tvConfig.seasonalArcSummary) { mutableStateOf(bible.tvConfig.seasonalArcSummary) }

    val goldAccent = if (isDark) ElectrumGold else Color(0xFF991B1B)
    val primaryText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val mutedText = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)
    val cardInsetBg = if (isDark) ObsidianBlack else Color(0xFFF8FAFC)
    val cardInsetBorder = if (isDark) ObsidianSurfaceVariant else Color(0xFFCBD5E1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ambientWorkspaceBrush(isDark))
    ) {
        // 1. Top Navigation Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompactScreen) 8.dp else 14.dp, vertical = 8.dp)
                .floatingGlassIsland(isDark = isDark, elevation = 14.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(0.dp))
                        .background(if (isDark) ObsidianBlack else Color(0xFFE2E8F0))
                        .border(BorderStroke(1.dp, cardInsetBorder), RoundedCornerShape(0.dp))
                        .clickable { viewModel.openStudioDrawer() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Studio Navigation Drawer",
                        tint = secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "🌳 THE HUB",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = goldAccent
                    )
                )

                Surface(
                    color = ScribeStarNeonRed,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = if (isCompactScreen) "WORLD TREE" else "WORLD TREE • $universeKindLabel",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HubTopPill(
                    text = if (isCompactScreen) "🏠" else "🏠 Start",
                    onClick = { viewModel.openStartMenu() },
                    isDark = isDark
                )
                HubTopPill(
                    text = if (isCompactScreen) "💾" else "💾 Save",
                    onClick = onSaveProject,
                    isDark = isDark,
                    isPrimary = true
                )
            }
        }

        // 2. Scrollable Centerpiece Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isCompactScreen) 10.dp else 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 980.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // A. Root Universe Header + Master Lore Strip + Launchpad Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .floatingGlassIsland(
                            isDark = isDark,
                            elevation = 16.dp,
                            accentColor = goldAccent,
                            isHighlighted = true
                        )
                        .padding(if (isCompactScreen) 14.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Editable Root Title Row
                    if (isEditingTitle) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draftUniverseTitle,
                                onValueChange = { draftUniverseTitle = it },
                                label = { Text("Universe Title", fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = goldAccent,
                                    unfocusedBorderColor = cardInsetBorder,
                                    focusedTextColor = primaryText,
                                    unfocusedTextColor = primaryText
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    viewModel.renameUniverse(context, draftUniverseTitle)
                                    isEditingTitle = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectrumGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Text("Save", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${theme.icon} ${theme.engineName.uppercase()}: ${universeTitle.uppercase()}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = MainAppFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primaryText,
                                        fontSize = if (isCompactScreen) 18.sp else 22.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${theme.foundationsLabel} branching into your living universe. Climb down any branch into its Studio editor, or return to this hub anytime.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = secondaryText,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = goldAccent.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(1.dp, goldAccent),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(0.dp))
                                        .clickable {
                                            Universe3DVisualizer.launch3DTree(
                                                context = context,
                                                universeTitle = universeTitle,
                                                bible = bible,
                                                blendedProject = blended
                                            )
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("🪐", fontSize = 13.sp)
                                        Text(
                                            text = "Launch 3D Tree",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = goldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    color = cardInsetBg,
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(1.dp, cardInsetBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(0.dp))
                                        .clickable {
                                            draftUniverseTitle = universeTitle
                                            isEditingTitle = true
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename Universe",
                                            tint = goldAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Rename",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = goldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Master Lore Inheritance Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardInsetBg, RoundedCornerShape(0.dp))
                            .border(1.dp, goldAccent.copy(alpha = 0.35f), RoundedCornerShape(0.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "${theme.foundationsLabel.uppercase()}:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = goldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                            HubLoreChip("🎭 ${theme.archetypesLabel}: ${bible.characters.size}", isDark)
                            HubLoreChip("🔍 Locations: ${bible.locations.size}", isDark)
                            HubLoreChip("📌 Beats & Events: ${bible.beats.size}", isDark)
                            HubLoreChip("⏳ ${theme.erasLabel}: ${blended?.eras?.size ?: 4}", isDark)
                        }
                        if (!isCompactScreen) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Shared across every branch",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = mutedText,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }

                    // Universe-Level Launchpad Buttons
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HubActionCardButton(
                            icon = "🪐",
                            title = "Launch 3D World Tree",
                            subtitle = "Interactive 3D spatial orbit in browser",
                            accentColor = Color(0xFF10B981),
                            isDark = isDark,
                            onClick = {
                                Universe3DVisualizer.launch3DTree(
                                    context = context,
                                    universeTitle = universeTitle,
                                    bible = bible,
                                    blendedProject = blended
                                )
                            },
                            modifier = if (isCompactScreen) Modifier.fillMaxWidth() else Modifier.widthIn(min = 210.dp)
                        )
                        HubActionCardButton(
                            icon = "📖",
                            title = "Master Lore Doc",
                            subtitle = "Characters, locations, world rules & beats",
                            accentColor = ElectrumGold,
                            isDark = isDark,
                            onClick = { viewModel.openStoryBible(context) },
                            modifier = if (isCompactScreen) Modifier.fillMaxWidth() else Modifier.widthIn(min = 210.dp)
                        )
                        HubActionCardButton(
                            icon = "📌",
                            title = "Corkboard / Outlines",
                            subtitle = "Index cards & act swimlanes",
                            accentColor = Color(0xFF3B82F6),
                            isDark = isDark,
                            onClick = { viewModel.openHubCorkboard() },
                            modifier = if (isCompactScreen) Modifier.fillMaxWidth() else Modifier.widthIn(min = 210.dp)
                        )
                        HubActionCardButton(
                            icon = "🎤",
                            title = "Pitch Deck",
                            subtitle = "45s elevator pitch & 16:9 deck",
                            accentColor = ScribeStarNeonRed,
                            isDark = isDark,
                            onClick = { viewModel.openPitchDeck() },
                            modifier = if (isCompactScreen) Modifier.fillMaxWidth() else Modifier.widthIn(min = 210.dp)
                        )
                        if (tvSeries != null) {
                            HubActionCardButton(
                                icon = "👥",
                                title = "Writers' Room",
                                subtitle = "Season arc & episodic pitch",
                                accentColor = Color(0xFF10B981),
                                isDark = isDark,
                                onClick = { viewModel.openWritersRoom() },
                                modifier = if (isCompactScreen) Modifier.fillMaxWidth() else Modifier.widthIn(min = 210.dp)
                            )
                        }
                    }
                }

                // B. Interactive World Tree Branches
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .floatingGlassIsland(isDark = isDark, elevation = 14.dp)
                        .padding(if (isCompactScreen) 14.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val branchItems: List<WorldTreeBranchItem> = when {
                        blended != null -> blended.assignments.mapIndexed { idx, a ->
                            val stats = if (a.format == ProjectFormat.NOVEL) {
                                "${a.novelManuscript.chapters.size} chapters • ${a.novelManuscript.totalWordCount()} words"
                            } else {
                                "${a.blocks.size} blocks"
                            }
                            val parentTitle = a.spinoffParentId?.let { pid ->
                                blended.assignments.firstOrNull { it.id == pid }?.title
                            }
                            WorldTreeBranchItem(
                                index = idx,
                                badge = a.mediumBadge(),
                                title = a.title,
                                meta = stats,
                                isActive = idx == blended.activeAssignmentIndex,
                                branchRole = a.branchRole,
                                parentTitle = parentTitle,
                                eraTag = a.eraTag
                            )
                        }
                        tvSeries != null -> tvSeries.episodes.mapIndexed { idx, ep ->
                            WorldTreeBranchItem(
                                index = idx,
                                badge = "📺 EP ${ep.episodeNumber}",
                                title = ep.title,
                                meta = "${ep.sceneCount} scenes • ${ep.pageCount} pp",
                                isActive = idx == tvSeries.activeEpisodeIndex
                            )
                        }
                        else -> bible.tvConfig.episodes.mapIndexed { idx, ep ->
                            WorldTreeBranchItem(
                                index = idx,
                                badge = "🎬 PART ${ep.episodeNumber}",
                                title = ep.title.ifBlank { "Untitled Installment" },
                                meta = ep.status,
                                isActive = idx == 0
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${theme.icon} ${theme.mainProjectsLabel.uppercase()} & ${theme.spinoffsLabel.uppercase()} (${branchItems.size})",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = goldAccent
                                )
                            )
                            Text(
                                text = "Click any project branch to open its Editor & Outline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = mutedText,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            color = if (showAddBranchRow) ObsidianSurfaceVariant else ElectrumGold,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable { showAddBranchRow = !showAddBranchRow }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                if (!showAddBranchRow) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Sprout Branch",
                                        tint = Color.Black,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = if (showAddBranchRow) "Cancel" else "+ Sprout Branch",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (showAddBranchRow) Color.White else Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    if (showAddBranchRow) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardInsetBg, RoundedCornerShape(0.dp))
                                .border(1.dp, ElectrumGold.copy(alpha = 0.5f), RoundedCornerShape(0.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🌱 CREATE A NEW CORE PROJECT OR SPINOFF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = goldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )

                            // 1. Medium / Format Picker
                            if (blended != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "🎬 Film" to ProjectFormat.SCREENPLAY,
                                        "📺 TV" to ProjectFormat.TV_SERIES,
                                        "📖 Novel" to ProjectFormat.NOVEL,
                                        "💥 Comic" to ProjectFormat.COMIC_BOOK,
                                        "🎭 Stage" to ProjectFormat.STAGE_PLAY
                                    ).forEach { (label, fmt) ->
                                        val selected = newBranchFormat == fmt
                                        Surface(
                                            color = if (selected) ElectrumGold.copy(alpha = 0.2f) else ObsidianSurface,
                                            shape = RoundedCornerShape(0.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(0.dp))
                                                .clickable { newBranchFormat = fmt }
                                                .border(
                                                    1.dp,
                                                    if (selected) ElectrumGold else ObsidianSurfaceVariant,
                                                    RoundedCornerShape(0.dp)
                                                )
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (selected) ElectrumGold else ScreenplayTextMuted,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Branch Role Selector
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "BRANCH RELATIONSHIP:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = mutedText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BranchRole.entries.forEach { role ->
                                        val isSel = newBranchRole == role
                                        Surface(
                                            color = if (isSel) goldAccent.copy(alpha = 0.22f) else ObsidianSurface,
                                            shape = RoundedCornerShape(0.dp),
                                            border = BorderStroke(1.dp, if (isSel) goldAccent else ObsidianSurfaceVariant),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(0.dp))
                                                .clickable { newBranchRole = role }
                                        ) {
                                            Text(
                                                text = "${role.icon} ${role.label}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSel) goldAccent else secondaryText,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 10.5.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Parent Project Selector (If not MAIN_SPINE)
                            if (newBranchRole != BranchRole.MAIN_SPINE && blended != null && blended.assignments.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "PARENT WORK (BRANCH OFF FROM):",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = mutedText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        blended.assignments.forEach { parentWork ->
                                            val isSel = selectedParentId == parentWork.id || (selectedParentId == null && parentWork == blended.assignments.first())
                                            Surface(
                                                color = if (isSel) Color(0xFF38BDF8).copy(alpha = 0.22f) else ObsidianSurface,
                                                shape = RoundedCornerShape(0.dp),
                                                border = BorderStroke(1.dp, if (isSel) Color(0xFF38BDF8) else ObsidianSurfaceVariant),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(0.dp))
                                                    .clickable { selectedParentId = parentWork.id }
                                            ) {
                                                Text(
                                                    text = "${parentWork.mediumBadge()} ${parentWork.title}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSel) Color(0xFF38BDF8) else secondaryText,
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 10.5.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Historical Era Selector (theme.erasLabel)
                            if (blended != null && blended.eras.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "HISTORICAL ERA / TIMELINE PLACEMENT:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = mutedText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        blended.eras.forEach { era ->
                                            val isSel = selectedEraTag == era.name
                                            Surface(
                                                color = if (isSel) ElectrumGold.copy(alpha = 0.22f) else ObsidianSurface,
                                                shape = RoundedCornerShape(0.dp),
                                                border = BorderStroke(1.dp, if (isSel) ElectrumGold else ObsidianSurfaceVariant),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(0.dp))
                                                    .clickable { selectedEraTag = era.name }
                                            ) {
                                                Text(
                                                    text = "⏳ ${era.name}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSel) ElectrumGold else secondaryText,
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 10.5.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Title & Submit Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newBranchTitle,
                                    onValueChange = { newBranchTitle = it },
                                    placeholder = {
                                        Text(
                                            "New project / branch title...",
                                            color = mutedText,
                                            fontSize = 12.sp
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = primaryText),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val titleToAdd = newBranchTitle.trim().ifBlank {
                                            when {
                                                tvSeries != null -> "Episode ${100 + tvSeries.episodes.size + 1}"
                                                newBranchFormat == ProjectFormat.NOVEL -> "Book ${(blended?.assignments?.size ?: 0) + 1}"
                                                newBranchFormat == ProjectFormat.SCREENPLAY -> "Film ${(blended?.assignments?.size ?: 0) + 1}"
                                                else -> "Branch ${(blended?.assignments?.size ?: 0) + 1}"
                                            }
                                        }
                                        when {
                                            blended != null -> {
                                                val sType = if (newBranchFormat == ProjectFormat.TV_SERIES) {
                                                    ScreenplayType.TV_HOUR_DRAMA
                                                } else {
                                                    ScreenplayType.FEATURE
                                                }
                                                val effectiveParentId = if (newBranchRole != BranchRole.MAIN_SPINE) {
                                                    selectedParentId ?: blended.assignments.firstOrNull()?.id
                                                } else null
                                                viewModel.addBlendedAssignment(
                                                    context = context,
                                                    assignmentTitle = titleToAdd,
                                                    format = newBranchFormat,
                                                    screenplayType = sType,
                                                    branchRole = newBranchRole,
                                                    spinoffParentId = effectiveParentId,
                                                    eraTag = selectedEraTag,
                                                    openImmediately = false
                                                )
                                            }
                                            tvSeries != null -> {
                                                val nextNum = "${100 + tvSeries.episodes.size + 1}"
                                                viewModel.addTvEpisode(
                                                    context = context,
                                                    episodeNumber = nextNum,
                                                    episodeTitle = titleToAdd,
                                                    openImmediately = false
                                                )
                                            }
                                            else -> {
                                                val nextNum = bible.tvConfig.episodes.size + 1
                                                viewModel.saveStoryBible(
                                                    context,
                                                    bible.copy(
                                                        tvConfig = bible.tvConfig.copy(
                                                            isTvSeries = true,
                                                            episodes = bible.tvConfig.episodes + TvEpisodeOutline(
                                                                episodeNumber = nextNum,
                                                                title = titleToAdd
                                                            )
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                        newBranchTitle = ""
                                        showAddBranchRow = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectrumGold,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(0.dp)
                                ) {
                                    Text("🌱 Sprout Branch", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (branchItems.isEmpty()) {
                        Text(
                            text = "No branches yet — click '+ Sprout Branch' to grow your World Tree.",
                            style = MaterialTheme.typography.bodySmall.copy(color = mutedText)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            branchItems.forEachIndexed { idx, branch ->
                                val isLast = idx == branchItems.lastIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tree Trunk + Horizontal Arm Connector
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .fillMaxHeight()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 10.dp)
                                                .width(2.dp)
                                                .fillMaxHeight(if (isLast) 0.5f else 1f)
                                                .align(Alignment.TopStart)
                                                .background(
                                                    if (branch.branchRole == BranchRole.MAIN_SPINE) ElectrumGold.copy(alpha = 0.65f)
                                                    else Color(0xFF38BDF8).copy(alpha = 0.65f)
                                                )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 10.dp)
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .align(Alignment.CenterStart)
                                                .background(
                                                    if (branch.branchRole == BranchRole.MAIN_SPINE) ElectrumGold.copy(alpha = 0.65f)
                                                    else Color(0xFF38BDF8).copy(alpha = 0.65f)
                                                )
                                        )
                                    }

                                    Surface(
                                        color = if (branch.isActive) ElectrumGold.copy(alpha = 0.14f) else cardInsetBg,
                                        shape = RoundedCornerShape(0.dp),
                                        border = BorderStroke(
                                            width = if (branch.isActive) 1.5.dp else 1.dp,
                                            color = if (branch.isActive) ElectrumGold else cardInsetBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.openBranchFromHub(context, branch.index, openCorkboard = false)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        color = if (isDark) ObsidianSurfaceVariant else Color(0xFFE2E8F0),
                                                        shape = RoundedCornerShape(0.dp)
                                                    ) {
                                                        Text(
                                                            text = branch.badge,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = goldAccent,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.sp
                                                            ),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    Surface(
                                                        color = if (branch.branchRole == BranchRole.MAIN_SPINE) ElectrumGold.copy(alpha = 0.16f) else Color(0xFF38BDF8).copy(alpha = 0.16f),
                                                        shape = RoundedCornerShape(0.dp),
                                                        border = BorderStroke(1.dp, if (branch.branchRole == BranchRole.MAIN_SPINE) ElectrumGold.copy(alpha = 0.45f) else Color(0xFF38BDF8).copy(alpha = 0.45f))
                                                    ) {
                                                        Text(
                                                            text = "${branch.branchRole.icon} ${branch.branchRole.label}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = if (branch.branchRole == BranchRole.MAIN_SPINE) ElectrumGold else Color(0xFF38BDF8),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 9.5.sp
                                                            ),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    Text(
                                                        text = branch.title,
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            color = if (branch.isActive) goldAccent else primaryText,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.5.sp
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    if (branch.parentTitle != null) {
                                                        Text(
                                                            text = "↳ shoots off \"${branch.parentTitle}\"",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = Color(0xFF38BDF8),
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 10.5.sp
                                                            )
                                                        )
                                                    }
                                                    if (branch.eraTag != null) {
                                                        Text(
                                                            text = "⏳ ${branch.eraTag}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = mutedText,
                                                                fontSize = 10.5.sp
                                                            )
                                                        )
                                                    }
                                                    if (branch.meta.isNotBlank()) {
                                                        Text(
                                                            text = "• ${branch.meta}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = mutedText,
                                                                fontSize = 10.5.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    color = if (isDark) ObsidianSurface else Color.White,
                                                    shape = RoundedCornerShape(0.dp),
                                                    border = BorderStroke(1.dp, cardInsetBorder),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(0.dp))
                                                        .clickable {
                                                            viewModel.openBranchFromHub(context, branch.index, openCorkboard = true)
                                                        }
                                                ) {
                                                    Text(
                                                        text = "📌 Outline",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = secondaryText,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 10.5.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                    )
                                                }

                                                Surface(
                                                    color = ElectrumGold,
                                                    shape = RoundedCornerShape(0.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                                    ) {
                                                        Text(
                                                            text = "Open",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = Color.Black,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp
                                                            )
                                                        )
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                            contentDescription = "Open Branch",
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // C. Universe Premise & Overarching Arc Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .floatingGlassIsland(isDark = isDark, elevation = 12.dp)
                        .padding(if (isCompactScreen) 14.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌌 UNIVERSE PREMISE & OVERARCHING ARC",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )
                        )
                        Surface(
                            color = ElectrumGold,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable {
                                    viewModel.saveStoryBible(
                                        context,
                                        bible.copy(
                                            tvConfig = bible.tvConfig.copy(
                                                seasonalArcSummary = arcDraft
                                            )
                                        )
                                    )
                                }
                        ) {
                            Text(
                                text = "Save Arc",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = arcDraft,
                        onValueChange = { arcDraft = it },
                        label = { Text("Overarching Saga / Season Arc & Central Engine", fontSize = 11.sp) },
                        placeholder = { Text("Describe the macro arc and central premise uniting every branch in this universe...", fontSize = 12.sp) },
                        minLines = 3,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldAccent,
                            unfocusedBorderColor = cardInsetBorder,
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private data class WorldTreeBranchItem(
    val index: Int,
    val badge: String,
    val title: String,
    val meta: String,
    val isActive: Boolean,
    val branchRole: BranchRole = BranchRole.MAIN_SPINE,
    val parentTitle: String? = null,
    val eraTag: String? = null
)

@Composable
private fun HubTopPill(
    text: String,
    onClick: () -> Unit,
    isDark: Boolean,
    isPrimary: Boolean = false
) {
    Surface(
        color = when {
            isPrimary -> ScribeStarNeonRed
            isDark -> ObsidianBlack
            else -> Color(0xFFF1F5F9)
        },
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(
            1.dp,
            if (isPrimary) ScribeStarNeonRed else if (isDark) ObsidianSurfaceVariant else Color(0xFFCBD5E1)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isPrimary) Color.White else if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun HubLoreChip(text: String, isDark: Boolean) {
    Surface(
        color = if (isDark) ObsidianSurfaceVariant else Color(0xFFE2E8F0),
        shape = RoundedCornerShape(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun HubActionCardButton(
    icon: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isDark) ObsidianBlack else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(0.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(0.dp))
            ) {
                Text(text = icon, fontSize = 16.sp)
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B),
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
