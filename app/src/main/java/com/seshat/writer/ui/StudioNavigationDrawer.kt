package com.seshat.writer.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.res.painterResource
import com.seshat.writer.R
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted

/**
 * Universal Studio Navigation Drawer (Option 1)
 *
 * Provides a clean, centralized studio drawer accessible across all project formats
 * (Screenplay, Novel, TV Series, Corkboard).
 *
 * Houses:
 * - Studio Brand & Current Project Header with Live Metrics
 * - Workspaces & Views Switcher
 * - Publishing, Export & Cover Studio
 * - Author Tools & AI Assistants
 * - Persistent Centrally Available Settings Menu
 */
@Composable
fun StudioNavigationDrawer(
    uiState: ScreenplayUiState,
    onCloseDrawer: () -> Unit,
    onSaveProject: () -> Unit,
    onOpenStartMenu: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenEditorCanvas: () -> Unit,
    onOpenCorkboard: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onOpenWritersRoom: () -> Unit,
    onOpenDraftDiff: () -> Unit,
    onOpenFileExport: () -> Unit,
    onOpenCompiler: () -> Unit,
    onOpenCoverCreator: () -> Unit,
    onOpenTitlePage: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenRevisions: () -> Unit,
    onOpenTableRead: () -> Unit,
    onOpenScriptDoctor: () -> Unit,
    onOpenFind: () -> Unit,
    onToggleDictation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiConfig = remember { AiPreferences.load(context) }
    val isNovel = uiState.projectFormat == ProjectFormat.NOVEL
    val isTv = uiState.projectFormat == ProjectFormat.TV_SERIES

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Semi-transparent scrim backdrop - tap to dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCloseDrawer
                )
        )

        // Slide-in drawer container
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Surface(
                color = ObsidianBlack,
                modifier = Modifier
                    .width(330.dp)
                    .fillMaxHeight()
                    .border(BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.25f)))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ==========================================
                    // 1. HEADER: BRAND & PROJECT INFO
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianSurface)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.seshat_emblem_logo),
                                    contentDescription = "Seshat Emblem",
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "SESHAT STUDIO",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp,
                                            color = ElectrumGold
                                        )
                                    )
                                    val formatTag = when (uiState.projectFormat) {
                                        ProjectFormat.NOVEL -> "Novel Manuscript"
                                        ProjectFormat.TV_SERIES -> "TV Series Studio"
                                        else -> "Screenplay Studio"
                                    }
                                    Text(
                                        text = formatTag,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ScreenplayTextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = onCloseDrawer,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Drawer",
                                    tint = ScreenplayTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Project title & Format badge
                        val displayTitle = uiState.title.ifBlank { "UNTITLED PROJECT" }
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ScreenplayTextDark
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Metrics Pill Row: Pages / Scenes (or Chapters) / Words
                        val pagesCount = if (isNovel) uiState.novelManuscript.estimatedPages() else uiState.pageCount
                        val unitsCount = if (isNovel) uiState.novelManuscript.chapters.size else uiState.sceneCount
                        val unitsLabel = if (isNovel) "Ch." else "Sc."
                        val wordsCount = if (isNovel) uiState.novelManuscript.totalWordCount() else uiState.wordCount

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DrawerMetricChip(label = "$pagesCount pp", modifier = Modifier.weight(1f))
                            DrawerMetricChip(label = "$unitsCount $unitsLabel", modifier = Modifier.weight(1f))
                            DrawerMetricChip(label = "$wordsCount w", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Action Buttons Row: [💾 Save] [🏠 Home] [📁 Library]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DrawerQuickButton(
                                text = "💾 Save",
                                onClick = {
                                    onSaveProject()
                                    Toast.makeText(context, "Saved successfully ✓", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DrawerQuickButton(
                                text = "🏠 Home",
                                onClick = {
                                    onCloseDrawer()
                                    onOpenStartMenu()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DrawerQuickButton(
                                text = "📁 Library",
                                onClick = {
                                    onCloseDrawer()
                                    onOpenLibrary()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = ElectrumGold.copy(alpha = 0.2f))

                    // ==========================================
                    // 2. SCROLLABLE NAVIGATION SECTIONS
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // SECTION: WORKSPACES & VIEWS
                        DrawerSection(title = "WORKSPACES & VIEWS") {
                            DrawerNavItem(
                                icon = "✍️",
                                title = if (isNovel) "Novel Editor" else if (isTv) "Episode Script" else "Screenplay Canvas",
                                subtitle = "Primary distraction-free editor",
                                isActive = !uiState.isCorkboardOpen,
                                onClick = {
                                    onCloseDrawer()
                                    onOpenEditorCanvas()
                                }
                            )

                            DrawerNavItem(
                                icon = "📌",
                                title = "Corkboard Outline",
                                subtitle = "Visual index cards & act pacing",
                                isActive = uiState.isCorkboardOpen,
                                onClick = {
                                    onCloseDrawer()
                                    onOpenCorkboard()
                                }
                            )

                            DrawerNavItem(
                                icon = "📖",
                                title = "Story Bible & Lore",
                                subtitle = "Characters, locations & rules",
                                onClick = {
                                    onCloseDrawer()
                                    onOpenStoryBible()
                                }
                            )

                            if (isTv) {
                                DrawerNavItem(
                                    icon = "👥",
                                    title = "Writers' Room",
                                    subtitle = "Multi-writer season arc & pitch",
                                    onClick = {
                                        onCloseDrawer()
                                        onOpenWritersRoom()
                                    }
                                )
                            }

                            if (!isNovel && !uiState.firstDraftState.isArcadeLocked) {
                                DrawerNavItem(
                                    icon = "📑",
                                    title = "Compare Drafts",
                                    subtitle = "Diff changes against previous draft",
                                    onClick = {
                                        onCloseDrawer()
                                        onOpenDraftDiff()
                                    }
                                )
                            }
                        }

                        // SECTION: PUBLISHING & PRODUCTION
                        DrawerSection(title = "PUBLISHING & PRODUCTION") {
                            if (isNovel) {
                                if (!uiState.firstDraftState.isArcadeLocked) {
                                    DrawerNavItem(
                                        icon = "📚",
                                        title = "Compile Manuscript",
                                        subtitle = "Standard manuscript, EPUB & PDF",
                                        onClick = {
                                            onCloseDrawer()
                                            onOpenCompiler()
                                        }
                                    )
                                }
                                if (uiState.firstDraftState.canUseNovelCoverGenerator()) {
                                    DrawerNavItem(
                                        icon = "🎨",
                                        title = "Cover & Jacket Studio",
                                        subtitle = if (uiState.firstDraftState.isArcadeLocked) "🎁 10k-Word Loot Pass Active" else "Dust jackets, wraps & AI art",
                                        onClick = {
                                            onCloseDrawer()
                                            onOpenCoverCreator()
                                        }
                                    )
                                }
                            } else {
                                if (!uiState.firstDraftState.isArcadeLocked) {
                                    DrawerNavItem(
                                        icon = "💾",
                                        title = "File & Export",
                                        subtitle = "FDX, PDF, Fountain, HTML & TXT",
                                        onClick = {
                                            onCloseDrawer()
                                            onOpenFileExport()
                                        }
                                    )
                                    DrawerNavItem(
                                        icon = "📜",
                                        title = "Title Page Creator",
                                        subtitle = "Standard industry title card",
                                        onClick = {
                                            onCloseDrawer()
                                            onOpenTitlePage()
                                        }
                                    )
                                    DrawerNavItem(
                                        icon = "📊",
                                        title = "Production Reports",
                                        subtitle = "Cast breakdown, locations & stats",
                                        onClick = {
                                            onCloseDrawer()
                                            onOpenReports()
                                        }
                                    )
                                    val revColorText = if (uiState.isRevisionTrackingEnabled) " (${uiState.currentRevisionColor.draftTag})" else ""
                                    DrawerNavItem(
                                        icon = "🎬",
                                        title = "Production Revisions$revColorText",
                                        subtitle = "Standard Hollywood colored drafts",
                                        onClick = {
                                            onCloseDrawer()
                                            onOpenRevisions()
                                        }
                                    )
                                }
                                if (uiState.firstDraftState.canUseTableRead()) {
                                    DrawerNavItem(
                                        icon = "🎙️",
                                        title = "Table Read / Audio",
                                        subtitle = if (uiState.firstDraftState.isArcadeLocked) "🎁 Unlocked via Loot Drop" else "Neural multi-voice read-through",
                                        onClick = {
                                            onCloseDrawer()
                                            onOpenTableRead()
                                        }
                                    )
                                }
                            }
                        }

                        // SECTION: AUTHOR TOOLS & AI
                        DrawerSection(title = "AUTHOR TOOLS & AI") {
                            if (uiState.firstDraftState.canUseAiDoctor()) {
                                DrawerNavItem(
                                    icon = "✨",
                                    title = "Script Doctor & AI",
                                    subtitle = if (aiConfig.isEnabled) "Coverage, polish & brainstorm" else "Configure AI keys in Settings",
                                    onClick = {
                                        onCloseDrawer()
                                        onOpenScriptDoctor()
                                    }
                                )
                            }

                            DrawerNavItem(
                                icon = "🔍",
                                title = "Find & Replace",
                                subtitle = "Search text & rename characters",
                                onClick = {
                                    onCloseDrawer()
                                    onOpenFind()
                                }
                            )

                            DrawerNavItem(
                                icon = "🎤",
                                title = "Voice Dictation",
                                subtitle = if (uiState.isDictating) "Listening... Tap to stop" else "Speak to write screenplay blocks",
                                isActive = uiState.isDictating,
                                onClick = {
                                    onCloseDrawer()
                                    onToggleDictation()
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = ElectrumGold.copy(alpha = 0.2f))

                    // ==========================================
                    // 3. PERSISTENT PINNED FOOTER: STUDIO SETTINGS
                    // ==========================================
                    Surface(
                        color = ObsidianSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCloseDrawer()
                                onOpenSettings()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = ObsidianBlack,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, ElectrumGold),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Studio Settings",
                                            tint = ElectrumGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Studio Settings",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ElectrumGold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    val aiStatus = if (aiConfig.isConfigured) "${aiConfig.provider.displayName} Active" else "Setup AI Key"
                                    val themeName = uiState.themeMode.name.lowercase().replaceFirstChar { it.uppercase() }
                                    Text(
                                        text = "$themeName Theme • $aiStatus",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ScreenplayTextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "OPEN ⚙️",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectrumGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMetricChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianBlack,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, ObsidianSurfaceVariant),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = ScreenplayTextMuted
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun DrawerQuickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianBlack,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.45f)),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = ElectrumGold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun DrawerSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElectrumGold.copy(alpha = 0.7f),
                letterSpacing = 1.2.sp,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )
        content()
    }
}

@Composable
private fun DrawerNavItem(
    icon: String,
    title: String,
    subtitle: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) ElectrumGold.copy(alpha = 0.15f) else ObsidianSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            1.dp,
            if (isActive) ElectrumGold else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 10.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) ElectrumGold else ScreenplayTextDark,
                            fontSize = 12.sp
                        )
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectrumGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
