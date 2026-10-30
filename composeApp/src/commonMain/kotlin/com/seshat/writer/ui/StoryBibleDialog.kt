package com.seshat.writer.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.hoverable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import com.seshat.writer.ui.theme.smokedGlassBrush
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.res.painterResource
import com.seshat.writer.R
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.BitmapFactory
import android.graphics.asImageBitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiPreferences
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.seshat.writer.bible.CharacterDossier
import com.seshat.writer.bible.LocationCodex
import com.seshat.writer.bible.LoreDocument
import com.seshat.writer.bible.StoryBeat
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.bible.StoryBibleRepository
import com.seshat.writer.bible.TvEpisodeOutline
import com.seshat.writer.bible.TvSeriesBibleConfig
import com.seshat.writer.firstdraft.FirstDraftStage
import com.seshat.writer.firstdraft.FirstDraftState
import com.seshat.writer.firstdraft.FirstDraftTextMetrics
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted

private data class SeriesHubDescriptor(
    val tabLabel: String,
    val icon: String,
    val headerTitle: String,
    val description: String,
    val seriesTitleLabel: String,
    val formatLabel: String,
    val formatOptions: List<String>,
    val arcLabel: String,
    val arcPlaceholder: String,
    val itemsSectionTitle: String,
    val addItemButtonLabel: String,
    val itemPrefix: String,
    val itemTitleLabel: String,
    val itemStoryALabel: String,
    val itemStoryBLabel: String
)

private fun getSeriesHubDescriptor(
    projectFormat: ProjectFormat,
    screenplayType: ScreenplayType
): SeriesHubDescriptor {
    val isTv = projectFormat == ProjectFormat.TV_SERIES ||
            (projectFormat == ProjectFormat.SCREENPLAY &&
                    (screenplayType == ScreenplayType.TV_HOUR_DRAMA || screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY))

    return when {
        isTv -> SeriesHubDescriptor(
            tabLabel = "📺 TV Hub",
            icon = "📺",
            headerTitle = "TV SHOW BIBLE HUB",
            description = "A multi-script workspace linking episodic scripts to one unified Master Lore Document. Track season arcs, A/B episodic storylines, and character transformations.",
            seriesTitleLabel = "Series Title",
            formatLabel = "Format",
            formatOptions = listOf(
                "1-Hour Serialized Drama",
                "1-Hour Episodic Procedural",
                "30-Min Single-Cam Comedy",
                "30-Min Multi-Cam Sitcom",
                "Limited / Mini-Series (4-8 Ep)",
                "Anthology Series",
                "Animated Series (22-Min)",
                "Custom / Other..."
            ),
            arcLabel = "Season 1 Overarching Arc & Central Dramatic Question",
            arcPlaceholder = "Describe the engine that drives the whole season from Episode 1 to the Season Finale.",
            itemsSectionTitle = "EPISODES",
            addItemButtonLabel = "Add Episode",
            itemPrefix = "EP.",
            itemTitleLabel = "Episode Title",
            itemStoryALabel = "A-Story (Central Episode Engine)",
            itemStoryBLabel = "B-Story (Secondary Character Arc)"
        )
        projectFormat == ProjectFormat.NOVEL -> SeriesHubDescriptor(
            tabLabel = "📚 Book Series Hub",
            icon = "📚",
            headerTitle = "BOOK SERIES & SAGA HUB",
            description = "A multi-book saga workspace linking volumes to one unified Master Lore Document. Track overarching saga arcs, book-by-book storylines, and character transformations across volumes.",
            seriesTitleLabel = "Book Series / Saga Title",
            formatLabel = "Format",
            formatOptions = listOf(
                "Standalone Novel",
                "Book Trilogy (3 Books)",
                "Duology (2 Books)",
                "Ongoing Book Series (4+ Volumes)",
                "Novella Series",
                "Standalone with Sequel Potential",
                "Anthology / Short Story Collection",
                "Custom / Other..."
            ),
            arcLabel = "Overarching Saga Arc & Mythos",
            arcPlaceholder = "Describe the master narrative arc spanning from Volume 1 through the series climax.",
            itemsSectionTitle = "VOLUMES & INSTALLMENTS",
            addItemButtonLabel = "Add Volume",
            itemPrefix = "VOL.",
            itemTitleLabel = "Book / Volume Title",
            itemStoryALabel = "Primary Narrative & Climax",
            itemStoryBLabel = "Subplot & Long-Term Saga Thread"
        )
        projectFormat == ProjectFormat.STAGE_PLAY -> SeriesHubDescriptor(
            tabLabel = "🎭 Staging Hub",
            icon = "🎭",
            headerTitle = "PRODUCTION & STAGING HUB",
            description = "A theatrical workspace linking acts and repertory performances to one unified Master Lore Document. Track staging directions, act breaks, and dramatic developments.",
            seriesTitleLabel = "Production / Repertory Title",
            formatLabel = "Format",
            formatOptions = listOf(
                "Full-Length Two-Act Play",
                "Three-Act Dramatic Play",
                "One-Act Play",
                "Musical Theater (Two Acts)",
                "Repertory Cycle / Multi-Play",
                "Chamber Drama",
                "Custom / Other..."
            ),
            arcLabel = "Thematic Arc & Production Vision",
            arcPlaceholder = "Describe the staging concepts, dramatic progression, and thematic unity.",
            itemsSectionTitle = "ACTS & STAGING",
            addItemButtonLabel = "Add Act",
            itemPrefix = "ACT",
            itemTitleLabel = "Act / Staging Title",
            itemStoryALabel = "Primary Theatrical Drama",
            itemStoryBLabel = "Staging Focus & Character Beat"
        )
        projectFormat == ProjectFormat.COMIC_BOOK -> SeriesHubDescriptor(
            tabLabel = "💥 Comic Series Hub",
            icon = "💥",
            headerTitle = "COMIC SERIES & RUN HUB",
            description = "A multi-issue comic universe workspace linking issue scripts to one unified Master Lore Document. Track master run arcs, issue hooks, and character transformations.",
            seriesTitleLabel = "Comic Series Title",
            formatLabel = "Format",
            formatOptions = listOf(
                "6-Issue Mini-Series",
                "Ongoing Monthly Run (12+ Issues)",
                "Original Graphic Novel (OGN)",
                "4-Issue Limited Series",
                "Maxi-Series (12 Issues)",
                "Crossover Event Run",
                "Custom / Other..."
            ),
            arcLabel = "Master Run Arc & Universe Premise",
            arcPlaceholder = "Describe the multi-issue run arc, central villain/threat, and world stakes.",
            itemsSectionTitle = "ISSUES",
            addItemButtonLabel = "Add Issue",
            itemPrefix = "ISSUE",
            itemTitleLabel = "Issue Title",
            itemStoryALabel = "Main Issue Plot & Climax",
            itemStoryBLabel = "B-Plot & Series Thread"
        )
        else -> SeriesHubDescriptor(
            tabLabel = "🎬 Film Series Hub",
            icon = "🎬",
            headerTitle = "FILM SERIES & FRANCHISE HUB",
            description = "A multi-film cinematic workspace linking sequels and prequels to one unified Master Lore Document. Track franchise continuity, installment arcs, and character transformations across films.",
            seriesTitleLabel = "Film Franchise / Series Title",
            formatLabel = "Format",
            formatOptions = listOf(
                "Feature Film Trilogy",
                "Duology / Two-Part Feature",
                "Cinematic Franchise / Universe",
                "Standalone Feature (90-120m)",
                "Anthology Feature Film",
                "Short Film Series",
                "Custom / Other..."
            ),
            arcLabel = "Cinematic Universe Arc & Continuity",
            arcPlaceholder = "Describe the overarching conflict and mythos connecting each theatrical installment.",
            itemsSectionTitle = "FILMS & INSTALLMENTS",
            addItemButtonLabel = "Add Film",
            itemPrefix = "FILM",
            itemTitleLabel = "Film Title",
            itemStoryALabel = "Main Theatrical Plot & Climax",
            itemStoryBLabel = "Subplot & Franchise Arc Thread"
        )
    }
}

@Composable
fun StoryBibleDialog(
    initialBible: StoryBible,
    activeCharactersInScript: List<String> = emptyList(),
    activeLocationsInScript: List<String> = emptyList(),
    projectFormat: ProjectFormat = ProjectFormat.SCREENPLAY,
    screenplayType: ScreenplayType = ScreenplayType.FEATURE,
    firstDraftState: FirstDraftState = FirstDraftState(isFirstDraftMode = false, stage = FirstDraftStage.STUDIO_UNLOCKED),
    onSaveBible: (StoryBible) -> Unit,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val isCompactScreen = configuration.screenWidthDp < 700
    val isReadOnlyAirlock = !firstDraftState.canEditBible()

    var bible by remember { mutableStateOf(initialBible) }
    var mainTab by remember { mutableIntStateOf(0) }
    var loreSubTab by remember { mutableIntStateOf(0) }
    var copyNotification by remember { mutableStateOf<String?>(null) }

    val hubDescriptor = remember(projectFormat, screenplayType) {
        getSeriesHubDescriptor(projectFormat, screenplayType)
    }

    fun updateAndPersist(updated: StoryBible) {
        if (isReadOnlyAirlock) {
            Toast.makeText(context, "🔒 Bible Airlock is sealed! Purchase a 15-min Airlock Pass in the XP Shop to edit.", Toast.LENGTH_SHORT).show()
            return
        }
        bible = updated
        onSaveBible(updated)
        StoryBibleRepository.saveBible(context, updated)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val isDark = LocalSeshatDarkTheme.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ambientWorkspaceBrush(isDark))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Persistent Seshat Studio & Statue Backdrop from Concept B
            Image(
                painter = painterResource(id = R.drawable.seshat_studio_splash),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isDark) 0.18f else 0.08f
                    }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Floating Top Action Header (Bar-less Spatial Lore Studio)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Floating Circle Dismiss Pill & Floating Logo+Title Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    spotColor = if (isDark) ScribeStarNeonRed.copy(alpha = 0.35f) else Color(0x3064748B)
                                )
                                .clip(CircleShape)
                                .background(opticalPillBrush(isDark = isDark))
                                .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), CircleShape)
                                .clickable(onClick = onDismiss)
                                .size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Story Bible",
                                    tint = ScribeStarNeonRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = if (isDark) Color.Black else Color(0x3064748B)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(opticalPillBrush(isDark = isDark))
                                .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), RoundedCornerShape(16.dp))
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 42.dp)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.seshat_emblem_logo),
                                        contentDescription = "ScribeStar Logo",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = if (isCompactScreen) "STORY BIBLE" else "STORY BIBLE & LORE",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = MainAppFontFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.1.sp,
                                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                                fontSize = 13.sp
                                            )
                                        )
                                        if (!isCompactScreen) {
                                            val completeness = bible.lore.completenessScore()
                                            Text(
                                                text = "AI Continuity: $completeness% • ${bible.lore.wordCount()} words",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = MainAppFontFamily,
                                                    color = if (completeness >= 75) (if (isDark) Color(0xFF81C784) else Color(0xFF15803D)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right: Floating Utility Actions (Theme Toggle & Copy Markdown)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingThemeTogglePill(
                            isDark = isDark,
                            onToggleTheme = onToggleTheme,
                            isCompact = isCompactScreen
                        )

                        val copyInteraction = remember { MutableInteractionSource() }
                        val isCopyHovered by copyInteraction.collectIsHoveredAsState()
                        val isCopyPressed by copyInteraction.collectIsPressedAsState()
                        val isCopyLit = isCopyHovered || isCopyPressed

                        val copyScale by animateFloatAsState(
                            targetValue = if (isCopyPressed) 0.95f else if (isCopyHovered) 1.04f else 1.0f,
                            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                            label = "CopyScale"
                        )
                        val copyGlow by animateDpAsState(
                            targetValue = if (isCopyLit) 8.dp else 0.dp,
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "CopyGlow"
                        )
                        val copyStroke by animateDpAsState(
                            targetValue = if (isCopyLit) 1.8.dp else 1.2.dp,
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "CopyStroke"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = copyScale
                                    scaleY = copyScale
                                }
                                .defaultMinSize(minHeight = 42.dp, minWidth = if (isCompactScreen) 42.dp else 54.dp)
                                .shadow(
                                    elevation = if (isCopyLit) 8.dp else 4.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = if (isCopyLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
                                )
                                .then(
                                    if (isCopyLit) {
                                        Modifier.neonBloom(
                                            color = ScribeStarNeonRed,
                                            cornerRadius = 16.dp,
                                            glowRadius = copyGlow,
                                            strokeWidth = copyStroke,
                                            outerAlpha = 0.45f,
                                            midAlpha = 0.75f
                                        )
                                    } else Modifier
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isCopyLit) SolidColor(ScribeStarNeonRed)
                                    else opticalPillBrush(isLit = isCopyLit, isPrimary = isCopyLit, isDark = isDark)
                                )
                                .border(
                                    if (isCopyLit) BorderStroke(copyStroke, Color.Transparent)
                                    else prismaticGlassBorder(
                                        width = copyStroke,
                                        isLit = isCopyLit,
                                        isDark = isDark
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = copyInteraction,
                                    indication = null
                                ) {
                                    val md = StoryBibleRepository.exportBibleToMarkdown(bible)
                                    clipboardManager.setText(AnnotatedString(md))
                                    copyNotification = "Copied!"
                                }
                                .padding(horizontal = if (isCompactScreen) 10.dp else 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = copyNotification ?: (if (isCompactScreen) "📋 Copy" else "📋 Copy Markdown"),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    color = if (isDark) (if (isCopyLit) Color.White else ElectrumGold) else (if (isCopyLit) Color.White else Color(0xFF0F172A)),
                                    fontWeight = if (isCopyLit) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                // Main Navigation Tabs (Floating Glass Pills with No Underlying Bar - Centered)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            "🌍 Lore Pillars (4)",
                            "👥 Characters (${bible.characters.size})",
                            "🗺️ Locations (${bible.locations.size})",
                            "📌 Beats (${bible.beats.size})",
                            hubDescriptor.tabLabel
                        )
                        tabs.forEachIndexed { index, title ->
                            val isSelected = mainTab == index
                            val tabInteraction = remember { MutableInteractionSource() }
                            val isHovered by tabInteraction.collectIsHoveredAsState()
                            val isPressed by tabInteraction.collectIsPressedAsState()
                            val isLit = isSelected || isHovered || isPressed

                            val tabScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.95f else if (isHovered) 1.04f else 1.0f,
                                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                                label = "TabScale_$index"
                            )
                            val tabGlow by animateDpAsState(
                                targetValue = if (isSelected) 8.dp else if (isHovered) 5.dp else 0.dp,
                                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                label = "TabGlow_$index"
                            )
                            val tabStroke by animateDpAsState(
                                targetValue = if (isLit) 1.8.dp else 1.2.dp,
                                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                label = "TabStroke_$index"
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = tabScale
                                        scaleY = tabScale
                                    }
                                    .defaultMinSize(minHeight = 42.dp, minWidth = 54.dp)
                                    .shadow(
                                        elevation = if (isSelected || isLit) 8.dp else 4.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor = if (isSelected || isLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
                                    )
                                    .then(
                                        if (isLit) {
                                            Modifier.neonBloom(
                                                color = ScribeStarNeonRed,
                                                cornerRadius = 16.dp,
                                                glowRadius = tabGlow,
                                                strokeWidth = tabStroke,
                                                outerAlpha = if (isSelected) 0.40f else 0.25f,
                                                midAlpha = if (isSelected) 0.70f else 0.45f
                                            )
                                        } else Modifier
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) SolidColor(ScribeStarNeonRed)
                                        else opticalPillBrush(isLit = isLit, isPrimary = isSelected, isDark = isDark)
                                    )
                                    .border(
                                        if (isSelected) BorderStroke(tabStroke, Color.Transparent)
                                        else prismaticGlassBorder(
                                            width = tabStroke,
                                            isLit = isLit,
                                            isDark = isDark
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = tabInteraction,
                                        indication = null
                                    ) {
                                        mainTab = index
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = title,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MainAppFontFamily,
                                        fontWeight = if (isSelected || isLit) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (isDark) Color.White else (if (isSelected || isLit) Color.White else Color(0xFF0F172A)),
                                        fontSize = 12.5.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Airlock Status Banner when in First Draft Mode
                if (firstDraftState.isArcadeLocked) {
                    Surface(
                        color = if (isReadOnlyAirlock) Color(0xFF280811) else Color(0xFF0A2418),
                        border = BorderStroke(1.dp, if (isReadOnlyAirlock) ScribeStarNeonRed else Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isReadOnlyAirlock) {
                                "🔒 BIBLE AIRLOCK SEALED (READ-ONLY) • Purchase 15-min Airlock Pass (2,500 XP) in XP Shop to unlock edits"
                            } else {
                                "🔓 BIBLE STAGE ACTIVE • Max ${firstDraftState.maxCharacterDossierSlots()} Character Dossiers (${firstDraftState.maxWordsPerCharacterDossier()}w each) • Max ${firstDraftState.maxWordsPerLorePillar()}w per Lore Pillar"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isReadOnlyAirlock) Color(0xFFFFB4AB) else Color(0xFF6EE7B7),
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Tab Content - Fills the entire screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (mainTab) {
                        0 -> LorePillarsSection(
                            lore = bible.lore,
                            subTab = loreSubTab,
                            onSubTabChange = { loreSubTab = it },
                            onUpdateLore = { updatedLore ->
                                updateAndPersist(bible.copy(lore = updatedLore))
                            },
                            firstDraftState = firstDraftState,
                            isCompactScreen = isCompactScreen
                        )
                        1 -> CharacterDossiersSection(
                            characters = bible.characters,
                            activeCharactersInScript = activeCharactersInScript,
                            onUpdateCharacters = { updatedList ->
                                updateAndPersist(bible.copy(characters = updatedList))
                            },
                            firstDraftState = firstDraftState,
                            isCompactScreen = isCompactScreen
                        )
                        2 -> LocationCodexSection(
                            locations = bible.locations,
                            activeLocationsInScript = activeLocationsInScript,
                            onUpdateLocations = { updatedList ->
                                updateAndPersist(bible.copy(locations = updatedList))
                            },
                            isCompactScreen = isCompactScreen
                        )
                        3 -> BeatsOutlineSection(
                            beats = bible.beats,
                            onUpdateBeats = { updatedList ->
                                updateAndPersist(bible.copy(beats = updatedList))
                            },
                            isCompactScreen = isCompactScreen
                        )
                        4 -> SeriesHubSection(
                            descriptor = hubDescriptor,
                            tvConfig = bible.tvConfig,
                            onUpdateTvConfig = { updatedTv ->
                                updateAndPersist(bible.copy(tvConfig = updatedTv))
                            },
                            firstDraftState = firstDraftState,
                            isCompactScreen = isCompactScreen
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// REUSABLE GLASS UI COMPONENTS
// ==========================================
@Composable
private fun GlassCardItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .hoverable(interactionSource = interactionSource)
            .then(
                if (isHovered) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 12.dp,
                        glowRadius = 6.dp,
                        strokeWidth = 1.2.dp,
                        outerAlpha = if (isDark) 0.25f else 0.15f,
                        midAlpha = if (isDark) 0.45f else 0.30f
                    )
                } else Modifier
            )
            .shadow(
                elevation = if (isHovered) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = if (isDark) (if (isHovered) ScribeStarNeonRed else Color.Black) else Color(0x2564748B)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            if (isHovered) Color(0x452E1018) else Color(0x35141C2A),
                            if (isHovered) Color(0x5519060B) else Color(0x450C1019)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    )
                }
            )
            .border(
                BorderStroke(
                    if (isHovered) 1.5.dp else 1.dp,
                    if (isHovered) ScribeStarNeonRed else if (isDark) ScribeStarNeonRed.copy(alpha = 0.35f) else Color(0xFFCBD5E1)
                ),
                RoundedCornerShape(12.dp)
            )
    ) {
        content()
    }
}

@Composable
private fun GlassActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 8.dp else if (isPrimary) 5.dp else 0.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ActionGlow"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.50f else if (isPrimary) 0.28f else 0.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ActionOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.85f else if (isPrimary) 0.52f else 0.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ActionMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isLit) 1.8.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ActionStroke"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "ActionScale"
    )

    val isDark = LocalSeshatDarkTheme.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .defaultMinSize(minHeight = 42.dp, minWidth = 54.dp)
            .shadow(
                elevation = if (isLit || isPrimary) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isLit || isPrimary) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
            )
            .then(
                if (isLit || isPrimary) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 16.dp,
                        glowRadius = glowRadius,
                        strokeWidth = strokeWidth,
                        outerAlpha = outerAlpha,
                        midAlpha = midAlpha,
                        coreAlpha = if (isLit) 1.0f else 0.85f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isPrimary) SolidColor(ScribeStarNeonRed)
                else opticalPillBrush(isLit = isLit, isPrimary = isPrimary, isDark = isDark)
            )
            .border(
                if (isPrimary) BorderStroke(strokeWidth, Color.Transparent)
                else prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isLit || isPrimary,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isDark) (if (isLit || isPrimary) Color.White else ElectrumGold) else (if (isLit || isPrimary) Color.White else Color(0xFF0F172A)),
                    fontWeight = if (isLit || isPrimary) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 12.5.sp
                ),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ==========================================
// 1. LORE PILLARS SECTION (4 PILLARS)
// ==========================================
@Composable
private fun LorePillarsSection(
    lore: LoreDocument,
    subTab: Int,
    onSubTabChange: (Int) -> Unit,
    onUpdateLore: (LoreDocument) -> Unit,
    firstDraftState: FirstDraftState = FirstDraftState(isFirstDraftMode = false, stage = FirstDraftStage.STUDIO_UNLOCKED),
    isCompactScreen: Boolean = false
) {
    val isDark = LocalSeshatDarkTheme.current
    Column(modifier = Modifier.fillMaxSize()) {
        // Floating Pillar Selector Subtabs (Bar-less Spatial Lore Studio - Centered)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pillars = listOf(
                    "✨ Cosmology" to lore.cosmology.isNotBlank(),
                    "🏛️ Anthropology" to lore.anthropology.isNotBlank(),
                    "⚡ Technology" to lore.technology.isNotBlank(),
                    "⏳ Chronology" to lore.chronology.isNotBlank()
                )
                pillars.forEachIndexed { index, (label, isFilled) ->
                    val isSelected = subTab == index
                    val subInteraction = remember { MutableInteractionSource() }
                    val isHovered by subInteraction.collectIsHoveredAsState()
                    val isPressed by subInteraction.collectIsPressedAsState()
                    val isLit = isSelected || isHovered || isPressed

                    val pillarScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else if (isHovered) 1.04f else 1.0f,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                        label = "PillarScale_$index"
                    )
                    val pillarGlow by animateDpAsState(
                        targetValue = if (isSelected) 8.dp else if (isHovered) 5.dp else 0.dp,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "PillarGlow_$index"
                    )
                    val pillarStroke by animateDpAsState(
                        targetValue = if (isLit) 1.8.dp else 1.2.dp,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "PillarStroke_$index"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = pillarScale
                                scaleY = pillarScale
                            }
                            .defaultMinSize(minHeight = 42.dp, minWidth = 54.dp)
                            .shadow(
                                elevation = if (isSelected || isLit) 8.dp else 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = if (isSelected || isLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
                            )
                            .then(
                                if (isLit) {
                                    Modifier.neonBloom(
                                        color = ScribeStarNeonRed,
                                        cornerRadius = 16.dp,
                                        glowRadius = pillarGlow,
                                        strokeWidth = pillarStroke,
                                        outerAlpha = if (isSelected) 0.40f else 0.25f,
                                        midAlpha = if (isSelected) 0.70f else 0.45f
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) SolidColor(ScribeStarNeonRed)
                                else opticalPillBrush(isLit = isLit, isPrimary = isSelected, isDark = isDark)
                            )
                            .border(
                                if (isSelected) BorderStroke(pillarStroke, Color.Transparent)
                                else prismaticGlassBorder(
                                    width = pillarStroke,
                                    isLit = isLit,
                                    isDark = isDark
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = subInteraction,
                                indication = null
                            ) { onSubTabChange(index) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = label,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    color = if (isDark) Color.White else (if (isSelected || isLit) Color.White else Color(0xFF0F172A)),
                                    fontWeight = if (isSelected || isLit) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            )
                            if (isFilled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✓",
                                    color = if (isSelected || isLit) Color.White else (if (isDark) Color(0xFF4CAF50) else Color(0xFF15803D)),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Pillar Editor & Guide
        val (pillarTitle, pillarPrompt, pillarContent, updatePillar) = when (subTab) {
            0 -> Quad(
                "✨ COSMOLOGY & METAPHYSICAL LAWS",
                "Define the universe's origin, metaphysical rules, magic limits, physical constants, atmospheric conditions, geography, and celestial entities.",
                lore.cosmology
            ) { text: String -> onUpdateLore(lore.copy(cosmology = text)) }
            1 -> Quad(
                "🏛️ ANTHROPOLOGY, FACTIONS & SOCIAL CUSTOMS",
                "Detail cultural factions, tribes, social classes, linguistic dialects, etiquette, taboos, religious rituals, and caste hierarchies.",
                lore.anthropology
            ) { text: String -> onUpdateLore(lore.copy(anthropology = text)) }
            2 -> Quad(
                "⚡ TECHNOLOGY LEVEL & INVENTIONS",
                "Specify scientific paradigm, transport, weapon lethality, energy sources, communication limits, and unique inventions.",
                lore.technology
            ) { text: String -> onUpdateLore(lore.copy(technology = text)) }
            else -> Quad(
                "⏳ CHRONOLOGY & WORLD TIMELINE",
                "Chronicle ancient epochs, recent wars, political treaties, preceding backstory, and sequential milestones leading directly into Scene 1.",
                lore.chronology
            ) { text: String -> onUpdateLore(lore.copy(chronology = text)) }
        }

        val maxPillarWords = firstDraftState.maxWordsPerLorePillar()
        val maxTotalWords = firstDraftState.maxTotalLoreWords()
        val currentPillarWords = FirstDraftTextMetrics.countWords(pillarContent)
        val totalLoreWords = lore.wordCount()

        val cappedUpdatePillar: (String) -> Unit = { newText ->
            val clipped = if (firstDraftState.isArcadeLocked) {
                FirstDraftTextMetrics.clipToWordLimit(newText, maxPillarWords)
            } else newText
            val deltaWords = FirstDraftTextMetrics.countWords(clipped) - currentPillarWords
            if (firstDraftState.isArcadeLocked && totalLoreWords + deltaWords > maxTotalWords && deltaWords > 0) {
                val allowedForThisPillar = (maxTotalWords - (totalLoreWords - currentPillarWords)).coerceAtLeast(0)
                updatePillar(FirstDraftTextMetrics.clipToWordLimit(clipped, allowedForThisPillar))
            } else {
                updatePillar(clipped)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pillarTitle,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) LightGold else Color(0xFF991B1B)
                    )
                )
                if (firstDraftState.isArcadeLocked) {
                    Text(
                        text = "$currentPillarWords / $maxPillarWords words",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (currentPillarWords >= maxPillarWords) ScribeStarNeonRed else ElectrumGold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pillarPrompt,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isDark) ScreenplayTextMuted else Color(0xFF334155),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            // White lore document writing surface (pure white in dark mode, crisp dark ink)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(4.dp))
            ) {
                OutlinedTextField(
                    value = pillarContent,
                    onValueChange = cappedUpdatePillar,
                    readOnly = !firstDraftState.canEditBible(),
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF111111),
                        fontFamily = ScreenplayCourierFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    ),
                    placeholder = {
                        Text(
                            text = "Enter world-building details here...\n\nExample entries:\n• Core rules & limitations\n• Taboos and sacred covenants\n• Technological/magical limits\n• Historical context known to characters",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF94A3B8),
                                fontFamily = ScreenplayCourierFontFamily
                            )
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = ScribeStarNeonRed,
                        focusedTextColor = Color(0xFF111111),
                        unfocusedTextColor = Color(0xFF111111)
                    )
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ==========================================
// 2. CHARACTER DOSSIERS SECTION
// ==========================================
@Composable
private fun CharacterDossiersSection(
    characters: List<CharacterDossier>,
    activeCharactersInScript: List<String>,
    onUpdateCharacters: (List<CharacterDossier>) -> Unit,
    firstDraftState: FirstDraftState = FirstDraftState(isFirstDraftMode = false, stage = FirstDraftStage.STUDIO_UNLOCKED),
    isCompactScreen: Boolean = false
) {
    val context = LocalContext.current
    val isDark = LocalSeshatDarkTheme.current
    val maxSlots = firstDraftState.maxCharacterDossierSlots()
    val maxWordsPerDossier = firstDraftState.maxWordsPerCharacterDossier()
    val canAddSlot = characters.size < maxSlots

    fun tryAddCharacter() {
        if (!canAddSlot) {
            Toast.makeText(
                context,
                "🔒 First Draft Mode caps Character Dossiers at $maxSlots slots (unlock more via Draft milestone loot drops!).",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        onUpdateCharacters(characters + CharacterDossier(name = "NEW CHARACTER"))
    }

    fun clipDossierIfNeeded(updated: CharacterDossier): CharacterDossier {
        if (!firstDraftState.isArcadeLocked) return updated
        // Enforce total words across dossier fields <= maxWordsPerDossier
        val combinedWords = FirstDraftTextMetrics.countWords(
            "${updated.physicalDescription} ${updated.arcAndNeed} ${updated.goalAndStakes} ${updated.voiceCadence} ${updated.secretsAndLore}"
        )
        if (combinedWords <= maxWordsPerDossier) return updated
        val perFieldCap = (maxWordsPerDossier / 3).coerceAtLeast(35)
        return updated.copy(
            physicalDescription = FirstDraftTextMetrics.clipToWordLimit(updated.physicalDescription, perFieldCap),
            arcAndNeed = FirstDraftTextMetrics.clipToWordLimit(updated.arcAndNeed, perFieldCap),
            goalAndStakes = FirstDraftTextMetrics.clipToWordLimit(updated.goalAndStakes, perFieldCap),
            voiceCadence = FirstDraftTextMetrics.clipToWordLimit(updated.voiceCadence, perFieldCap),
            secretsAndLore = FirstDraftTextMetrics.clipToWordLimit(updated.secretsAndLore, perFieldCap)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Controls Row
        if (isCompactScreen) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (firstDraftState.isArcadeLocked) "CHARACTER PROFILES (${characters.size} / $maxSlots SLOTS • MAX ${maxWordsPerDossier}w)" else "CHARACTER PROFILES (${characters.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isDark) LightGold else Color(0xFF991B1B),
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeCharactersInScript.isNotEmpty() && !firstDraftState.isArcadeLocked) {
                        GlassActionButton(
                            text = "⚡ Import (${activeCharactersInScript.size})",
                            onClick = {
                                val existingNames = characters.map { it.name.trim().uppercase() }.toSet()
                                val newOnes = activeCharactersInScript
                                    .filter { it.isNotBlank() && !existingNames.contains(it.trim().uppercase()) }
                                    .map { CharacterDossier(name = it.trim().uppercase()) }
                                onUpdateCharacters((characters + newOnes).take(maxSlots))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GlassActionButton(
                        text = if (canAddSlot) "Add Character (${characters.size}/$maxSlots)" else "🔒 Max $maxSlots Slots",
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(15.dp)) },
                        onClick = { tryAddCharacter() },
                        isPrimary = canAddSlot,
                        modifier = if (activeCharactersInScript.isNotEmpty() && !firstDraftState.isArcadeLocked) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (firstDraftState.isArcadeLocked) "CHARACTER PROFILES (${characters.size} / $maxSlots SLOTS • MAX ${maxWordsPerDossier}w EACH)" else "CHARACTER PROFILES & FACTION ALLEGIANCES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isDark) LightGold else Color(0xFF991B1B),
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (activeCharactersInScript.isNotEmpty() && !firstDraftState.isArcadeLocked) {
                        GlassActionButton(
                            text = "⚡ Import Script Cast (${activeCharactersInScript.size})",
                            onClick = {
                                val existingNames = characters.map { it.name.trim().uppercase() }.toSet()
                                val newOnes = activeCharactersInScript
                                    .filter { it.isNotBlank() && !existingNames.contains(it.trim().uppercase()) }
                                    .map { CharacterDossier(name = it.trim().uppercase()) }
                                onUpdateCharacters((characters + newOnes).take(maxSlots))
                            }
                        )
                    }

                    GlassActionButton(
                        text = if (firstDraftState.isArcadeLocked) {
                            if (canAddSlot) "Add Character (${characters.size}/$maxSlots)" else "🔒 Max $maxSlots Slots"
                        } else "Add Character",
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(15.dp)) },
                        onClick = { tryAddCharacter() },
                        isPrimary = canAddSlot
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (characters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No character dossiers yet. Click 'Add Character' or import names from your script.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) ScreenplayTextMuted else Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(characters, key = { it.id }) { char ->
                    CharacterCardItem(
                        character = char,
                        onUpdate = { updated ->
                            val clipped = clipDossierIfNeeded(updated)
                            onUpdateCharacters(characters.map { if (it.id == clipped.id) clipped else it })
                        },
                        onDelete = {
                            onUpdateCharacters(characters.filter { it.id != char.id })
                        },
                        canUseAiPortrait = firstDraftState.canUseAiCharacterPortraits(),
                        isCompactScreen = isCompactScreen
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterPortraitSection(
    character: CharacterDossier,
    onUpdate: (CharacterDossier) -> Unit,
    isDark: Boolean,
    canUseAiPortrait: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val portraitsDir = File(context.filesDir, "character_portraits")
                    if (!portraitsDir.exists()) portraitsDir.mkdirs()
                    val file = File(portraitsDir, "char_${character.id}_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    onUpdate(character.copy(imageUrl = file.absolutePath))
                    Toast.makeText(context, "Portrait updated", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to import image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val portraitBitmap = remember(character.imageUrl) {
        runCatching {
            if (character.imageUrl.isNotBlank()) {
                val f = File(character.imageUrl)
                if (f.exists()) BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap() else null
            } else null
        }.getOrNull()
    }

    fun generateAiPortrait() {
        val config = AiPreferences.load(context)
        if (config.apiKey.isBlank()) {
            Toast.makeText(context, "Please configure AI in Settings first", Toast.LENGTH_LONG).show()
            return
        }
        if (character.name.isBlank() && character.physicalDescription.isBlank()) {
            Toast.makeText(context, "Please enter a name or physical description", Toast.LENGTH_LONG).show()
            return
        }
        isGenerating = true
        scope.launch {
            val portraitsDir = File(context.filesDir, "character_portraits")
            if (!portraitsDir.exists()) portraitsDir.mkdirs()
            val outputFile = File(portraitsDir, "char_${character.id}_ai_${System.currentTimeMillis()}.jpg")
            val result = withContext(Dispatchers.IO) {
                AiClient.generateCharacterPortraitWithAi(
                    config = config,
                    characterName = character.name.ifBlank { "Character" },
                    role = character.role,
                    physicalDescription = character.physicalDescription,
                    outputFile = outputFile
                )
            }
            isGenerating = false
            result.onSuccess { file ->
                onUpdate(character.copy(imageUrl = file.absolutePath))
                Toast.makeText(context, "AI Portrait generated!", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                Toast.makeText(context, "Generation failed: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) Color(0xFF1E1E28) else Color(0xFFF1F5F9))
                .border(
                    1.dp,
                    if (isDark) ScribeStarNeonRed.copy(alpha = 0.5f) else Color(0xFF991B1B).copy(alpha = 0.4f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (portraitBitmap != null) {
                Image(
                    bitmap = portraitBitmap,
                    contentDescription = "Portrait of ${character.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable { onUpdate(character.copy(imageUrl = "")) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Portrait",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isDark) ScreenplayTextMuted else Color(0xFF94A3B8),
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "No Portrait",
                        fontSize = 9.sp,
                        color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)
                    )
                }
            }
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ScribeStarNeonRed,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canUseAiPortrait) {
                    GlassActionButton(
                        text = if (isGenerating) "Generating..." else "✨ AI Portrait",
                        onClick = { if (!isGenerating) generateAiPortrait() },
                        modifier = Modifier.weight(1f),
                        isPrimary = true
                    )
                } else {
                    GlassActionButton(
                        text = "🔒 AI Portrait (Loot Drop)",
                        onClick = {
                            Toast.makeText(
                                context,
                                "🔒 AI Character Portraits unlock as a Draft Milestone Loot Drop!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    )
                }
                GlassActionButton(
                    text = "🖼️ Upload",
                    onClick = { if (!isGenerating) imagePickerLauncher.launch("image/*") }
                )
            }
            Text(
                text = "AI uses Name, Role & Physical Description to generate portrait.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)
                ),
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun CharacterCardCompact(
    character: CharacterDossier,
    onUpdate: (CharacterDossier) -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean,
    canUseAiPortrait: Boolean = true
) {
    Column(modifier = Modifier.padding(10.dp)) {
        // Name + Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = character.name,
                onValueChange = { onUpdate(character.copy(name = it.uppercase())) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                    fontFamily = ScreenplayCourierFontFamily
                ),
                singleLine = true,
                placeholder = { Text("CHARACTER NAME", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)) },
                label = { Text("Character Name", color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Role and Faction (side-by-side with weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = character.role,
                onValueChange = { onUpdate(character.copy(role = it)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = if (isDark) LightGold else Color(0xFF991B1B),
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                placeholder = { Text("Protagonist", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                label = { Text("Role", color = if (isDark) LightGold else Color(0xFF991B1B), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            OutlinedTextField(
                value = character.factionOrCaste,
                onValueChange = { onUpdate(character.copy(factionOrCaste = it)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = ScreenplayTextDark,
                    fontWeight = FontWeight.SemiBold
                ),
                singleLine = true,
                placeholder = { Text("Faction / Caste", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                label = { Text("Faction", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Portrait Generation & Upload Section
        CharacterPortraitSection(
            character = character,
            onUpdate = onUpdate,
            isDark = isDark,
            canUseAiPortrait = canUseAiPortrait,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Physical Description & Visual Appearance
        OutlinedTextField(
            value = character.physicalDescription,
            onValueChange = { onUpdate(character.copy(physicalDescription = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Physical Description & Appearance (e.g. Tall, piercing grey eyes, scar across left cheek)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
            label = { Text("Physical Description & Visual Appearance", color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = character.arcAndNeed,
            onValueChange = { onUpdate(character.copy(arcAndNeed = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Internal Need & Flaw (e.g. Overcome cynical distrust)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
            label = { Text("Internal Need & Flaw", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = character.goalAndStakes,
            onValueChange = { onUpdate(character.copy(goalAndStakes = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("External Goal & Stakes (e.g. Retrieve archive before dawn)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
            label = { Text("External Goal & Stakes", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = character.voiceCadence,
            onValueChange = { onUpdate(character.copy(voiceCadence = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Vocal Cadence (e.g. Clipped, rhythmic, naval slang)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
            label = { Text("Vocal Cadence", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = character.secretsAndLore,
            onValueChange = { onUpdate(character.copy(secretsAndLore = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Lore Secrets (e.g. Belongs to forbidden Sun Order)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
            label = { Text("Lore Secrets", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium),
            maxLines = 3
        )
    }
}

@Composable
private fun CharacterCardDesktop(
    character: CharacterDossier,
    onUpdate: (CharacterDossier) -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean,
    canUseAiPortrait: Boolean = true
) {
    Column(modifier = Modifier.padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = character.name,
                    onValueChange = { onUpdate(character.copy(name = it.uppercase())) },
                    modifier = Modifier.weight(0.35f),
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                        fontFamily = ScreenplayCourierFontFamily
                    ),
                    singleLine = true,
                    placeholder = { Text("NAME", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = character.role,
                    onValueChange = { onUpdate(character.copy(role = it)) },
                    modifier = Modifier.weight(0.3f),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDark) LightGold else Color(0xFF991B1B),
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    placeholder = { Text("Role (Protagonist)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = character.factionOrCaste,
                    onValueChange = { onUpdate(character.copy(factionOrCaste = it)) },
                    modifier = Modifier.weight(0.35f),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextDark,
                        fontWeight = FontWeight.SemiBold
                    ),
                    singleLine = true,
                    placeholder = { Text("Faction / Caste", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)) }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Portrait section & Physical description side-by-side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CharacterPortraitSection(
                character = character,
                onUpdate = onUpdate,
                isDark = isDark,
                canUseAiPortrait = canUseAiPortrait,
                modifier = Modifier.weight(0.48f)
            )
            OutlinedTextField(
                value = character.physicalDescription,
                onValueChange = { onUpdate(character.copy(physicalDescription = it)) },
                modifier = Modifier.weight(0.52f),
                placeholder = { Text("Physical Description & Appearance (e.g. Silver hair, scar on cheek, weathered cloak)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                label = { Text("Physical Description & Visual Appearance", color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium),
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = character.arcAndNeed,
                onValueChange = { onUpdate(character.copy(arcAndNeed = it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Internal Need & Flaw (e.g. Needs to overcome cynical distrust)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium)
            )
            OutlinedTextField(
                value = character.goalAndStakes,
                onValueChange = { onUpdate(character.copy(goalAndStakes = it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("External Goal & Stakes (e.g. Retrieve the archive before dawn)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = character.voiceCadence,
                onValueChange = { onUpdate(character.copy(voiceCadence = it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Vocal Cadence (e.g. Clipped, rhythmic, uses naval slang)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium)
            )
            OutlinedTextField(
                value = character.secretsAndLore,
                onValueChange = { onUpdate(character.copy(secretsAndLore = it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Lore Secrets (e.g. Secretly belongs to the forbidden Sun Order)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun CharacterCardItem(
    character: CharacterDossier,
    onUpdate: (CharacterDossier) -> Unit,
    onDelete: () -> Unit,
    canUseAiPortrait: Boolean = true,
    isCompactScreen: Boolean = false
) {
    val isDark = LocalSeshatDarkTheme.current
    GlassCardItem(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isCompactScreen) {
            CharacterCardCompact(
                character = character,
                onUpdate = onUpdate,
                onDelete = onDelete,
                isDark = isDark,
                canUseAiPortrait = canUseAiPortrait
            )
        } else {
            CharacterCardDesktop(
                character = character,
                onUpdate = onUpdate,
                onDelete = onDelete,
                isDark = isDark,
                canUseAiPortrait = canUseAiPortrait
            )
        }
    }
}

// ==========================================
// 3. LOCATION CODEX SECTION
// ==========================================
@Composable
private fun LocationCodexSection(
    locations: List<LocationCodex>,
    activeLocationsInScript: List<String>,
    onUpdateLocations: (List<LocationCodex>) -> Unit,
    isCompactScreen: Boolean = false
) {
    val isDark = LocalSeshatDarkTheme.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (isCompactScreen) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "LOCATION CODEX (${locations.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isDark) LightGold else Color(0xFF991B1B),
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeLocationsInScript.isNotEmpty()) {
                        GlassActionButton(
                            text = "⚡ Import (${activeLocationsInScript.size})",
                            onClick = {
                                val existing = locations.map { it.name.trim().uppercase() }.toSet()
                                val newOnes = activeLocationsInScript
                                    .filter { it.isNotBlank() && !existing.contains(it.trim().uppercase()) }
                                    .map { LocationCodex(name = it.trim().uppercase()) }
                                onUpdateLocations(locations + newOnes)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GlassActionButton(
                        text = "Add Location",
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(15.dp)) },
                        onClick = {
                            onUpdateLocations(locations + LocationCodex(name = "NEW LOCATION"))
                        },
                        isPrimary = true,
                        modifier = if (activeLocationsInScript.isNotEmpty()) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOCATION CODEX & ATMOSPHERIC PROFILES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isDark) LightGold else Color(0xFF991B1B),
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (activeLocationsInScript.isNotEmpty()) {
                        GlassActionButton(
                            text = "⚡ Import Script Locations (${activeLocationsInScript.size})",
                            onClick = {
                                val existing = locations.map { it.name.trim().uppercase() }.toSet()
                                val newOnes = activeLocationsInScript
                                    .filter { it.isNotBlank() && !existing.contains(it.trim().uppercase()) }
                                    .map { LocationCodex(name = it.trim().uppercase()) }
                                onUpdateLocations(locations + newOnes)
                            }
                        )
                    }

                    GlassActionButton(
                        text = "Add Location",
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(15.dp)) },
                        onClick = {
                            onUpdateLocations(locations + LocationCodex(name = "NEW LOCATION"))
                        },
                        isPrimary = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (locations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No locations in codex. Add key settings or import from scene sluglines.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) ScreenplayTextMuted else Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(locations, key = { it.id }) { loc ->
                    GlassCardItem(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCompactScreen) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = loc.sluglinePrefix,
                                        onValueChange = { newPrefix -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(sluglinePrefix = newPrefix) else it }) },
                                        modifier = Modifier.width(80.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isDark) LightGold else Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        label = { Text("Prefix", fontSize = 10.sp, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = loc.name,
                                        onValueChange = { newName -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(name = newName.uppercase()) else it }) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.titleSmall.copy(
                                            color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        label = { Text("Location Name", fontSize = 10.sp, color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                    )
                                    IconButton(onClick = { onUpdateLocations(locations.filter { it.id != loc.id }) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = loc.visualAtmosphere,
                                    onValueChange = { newAtmo -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(visualAtmosphere = newAtmo) else it }) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    placeholder = { Text("Visual Atmosphere & Sensory Details", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                    label = { Text("Visual Atmosphere", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = loc.loreAndHazards,
                                    onValueChange = { newHazards -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(loreAndHazards = newHazards) else it }) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    placeholder = { Text("Lore, Faction Control & Physical Hazards", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                    label = { Text("Lore & Hazards", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    maxLines = 3
                                )
                            }
                        } else {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = loc.sluglinePrefix,
                                            onValueChange = { newPrefix -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(sluglinePrefix = newPrefix) else it }) },
                                            modifier = Modifier.width(90.dp),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isDark) LightGold else Color(0xFF991B1B),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            label = { Text("Prefix", fontSize = 10.sp, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = loc.name,
                                            onValueChange = { newName -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(name = newName.uppercase()) else it }) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                                color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            label = { Text("Location Name", fontSize = 10.sp, color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { onUpdateLocations(locations.filter { it.id != loc.id }) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = loc.visualAtmosphere,
                                        onValueChange = { newAtmo -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(visualAtmosphere = newAtmo) else it }) },
                                        modifier = Modifier.weight(1f),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                            fontWeight = FontWeight.Medium
                                        ),
                                        placeholder = { Text("Visual Atmosphere & Sensory Details", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                                        label = { Text("Visual Atmosphere", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                    )
                                    OutlinedTextField(
                                        value = loc.loreAndHazards,
                                        onValueChange = { newHazards -> onUpdateLocations(locations.map { if (it.id == loc.id) loc.copy(loreAndHazards = newHazards) else it }) },
                                        modifier = Modifier.weight(1f),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                            fontWeight = FontWeight.Medium
                                        ),
                                        placeholder = { Text("Lore, Faction Control & Physical Hazards", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                                        label = { Text("Lore & Hazards", color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
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

// ==========================================
// 4. BEATS & OUTLINE SECTION
// ==========================================
@Composable
private fun BeatsOutlineSection(
    beats: List<StoryBeat>,
    onUpdateBeats: (List<StoryBeat>) -> Unit,
    isCompactScreen: Boolean = false
) {
    val isDark = LocalSeshatDarkTheme.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCompactScreen) "STORY BEATS (${beats.size})" else "INTERACTIVE STORY BEATS & SEQUENCE OUTLINE",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isDark) LightGold else Color(0xFF991B1B),
                    fontWeight = FontWeight.ExtraBold
                )
            )

            GlassActionButton(
                text = "Add Beat",
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Beat", tint = Color.White, modifier = Modifier.size(15.dp)) },
                onClick = {
                    val defaultAct = if (beats.isEmpty()) "Act I" else beats.last().act
                    onUpdateBeats(beats + StoryBeat(act = defaultAct, title = "New Story Beat"))
                },
                isPrimary = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (beats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No beats yet. Click 'Add Beat' to outline key sequence moments.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) ScreenplayTextMuted else Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(beats, key = { it.id }) { beat ->
                    GlassCardItem(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCompactScreen) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = beat.act,
                                        onValueChange = { newAct -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(act = newAct) else it }) },
                                        modifier = Modifier.width(80.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isDark) LightGold else Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        label = { Text("Act", fontSize = 10.sp, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = beat.title,
                                        onValueChange = { newTitle -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(title = newTitle) else it }) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.titleSmall.copy(
                                            color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        label = { Text("Beat Title", fontSize = 10.sp, color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                    )
                                    IconButton(onClick = { onUpdateBeats(beats.filter { it.id != beat.id }) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Beat", tint = Color(0xFFEF5350))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = beat.dramaticShift,
                                    onValueChange = { newShift -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(dramaticShift = newShift) else it }) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    placeholder = { Text("Dramatic Shift (e.g. + / -, Climax, Reversal)", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                    label = { Text("Dramatic Shift / Polarity", fontSize = 10.sp, color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontWeight = FontWeight.Bold) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = beat.summary,
                                    onValueChange = { newSummary -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(summary = newSummary) else it }) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    placeholder = { Text("Beat Summary: What happens, who turns the scene, conflict escalation...", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                    label = { Text("Beat Summary", fontSize = 10.sp, color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontWeight = FontWeight.Bold) },
                                    maxLines = 3
                                )
                            }
                        } else {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = beat.act,
                                            onValueChange = { newAct -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(act = newAct) else it }) },
                                            modifier = Modifier.width(100.dp),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isDark) LightGold else Color(0xFF991B1B),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            label = { Text("Act", fontSize = 10.sp, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = beat.title,
                                            onValueChange = { newTitle -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(title = newTitle) else it }) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.titleSmall.copy(
                                                color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            label = { Text("Beat Title", fontSize = 10.sp, color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = beat.dramaticShift,
                                            onValueChange = { newShift -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(dramaticShift = newShift) else it }) },
                                            modifier = Modifier.width(120.dp),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            label = { Text("Shift", fontSize = 10.sp, color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontWeight = FontWeight.Bold) },
                                            placeholder = { Text("Shift", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { onUpdateBeats(beats.filter { it.id != beat.id }) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Beat", tint = Color(0xFFEF5350))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = beat.summary,
                                    onValueChange = { newSummary -> onUpdateBeats(beats.map { if (it.id == beat.id) beat.copy(summary = newSummary) else it }) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    placeholder = { Text("Beat Summary: What happens, who turns the scene, and how does conflict escalate?", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 12.sp) },
                                    label = { Text("Beat Summary", fontSize = 10.sp, color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontWeight = FontWeight.Bold) }
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
private fun SeriesHubFormatDropdown(
    currentFormat: String,
    options: List<String>,
    label: String,
    onSelectFormat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = LocalSeshatDarkTheme.current

    Box(modifier = modifier) {
        OutlinedTextField(
            value = currentFormat,
            onValueChange = { onSelectFormat(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) },
            placeholder = { Text("Select or type format...", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Format",
                        tint = if (expanded) ScribeStarNeonRed else (if (isDark) LightGold else Color(0xFF991B1B))
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(if (isDark) Color(0xFF0F1522) else Color(0xFFFFFFFF))
                .border(
                    1.dp,
                    if (isDark) ScribeStarNeonRed.copy(alpha = 0.5f) else Color(0xFFCBD5E1),
                    RoundedCornerShape(8.dp)
                )
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = if (option == currentFormat) FontWeight.Bold else FontWeight.Normal,
                                color = if (option == currentFormat) {
                                    ScribeStarNeonRed
                                } else if (isDark) {
                                    Color(0xFFF1F5F9)
                                } else {
                                    Color(0xFF0F172A)
                                }
                            )
                        )
                    },
                    onClick = {
                        expanded = false
                        if (option != "Custom / Other...") {
                            onSelectFormat(option)
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 5. SERIES & UNIVERSE HUB SECTION
// ==========================================
@Composable
private fun SeriesHubSection(
    descriptor: SeriesHubDescriptor,
    tvConfig: TvSeriesBibleConfig,
    onUpdateTvConfig: (TvSeriesBibleConfig) -> Unit,
    firstDraftState: FirstDraftState = FirstDraftState(isFirstDraftMode = false, stage = FirstDraftStage.STUDIO_UNLOCKED),
    isCompactScreen: Boolean = false
) {
    val isDark = LocalSeshatDarkTheme.current
    val scroll = rememberScrollState()
    val maxArcWords = firstDraftState.maxWordsTvSeasonArc()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Architectural Roadmap Callout
        GlassCardItem(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${descriptor.icon} ${descriptor.headerTitle}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color(0xFF90CAF9) else Color(0xFF1D4ED8)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = if (isDark) Color(0xFF1565C0) else Color(0xFF1D4ED8), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "ROADMAP",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descriptor.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDark) Color(0xFFBBDEFB) else Color(0xFF1E293B),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Series Metadata
        if (isCompactScreen) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tvConfig.seriesTitle,
                    onValueChange = { onUpdateTvConfig(tvConfig.copy(seriesTitle = it, isTvSeries = true)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(descriptor.seriesTitleLabel, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                        fontWeight = FontWeight.Bold
                    )
                )
                SeriesHubFormatDropdown(
                    currentFormat = tvConfig.format,
                    options = descriptor.formatOptions,
                    label = descriptor.formatLabel,
                    onSelectFormat = { onUpdateTvConfig(tvConfig.copy(format = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tvConfig.seriesTitle,
                    onValueChange = { onUpdateTvConfig(tvConfig.copy(seriesTitle = it, isTvSeries = true)) },
                    modifier = Modifier.weight(1f),
                    label = { Text(descriptor.seriesTitleLabel, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                        fontWeight = FontWeight.Bold
                    )
                )
                SeriesHubFormatDropdown(
                    currentFormat = tvConfig.format,
                    options = descriptor.formatOptions,
                    label = descriptor.formatLabel,
                    onSelectFormat = { onUpdateTvConfig(tvConfig.copy(format = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = tvConfig.seasonalArcSummary,
            onValueChange = {
                val clipped = if (firstDraftState.isArcadeLocked) {
                    FirstDraftTextMetrics.clipToWordLimit(it, maxArcWords)
                } else it
                onUpdateTvConfig(tvConfig.copy(seasonalArcSummary = clipped))
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                val arcWords = FirstDraftTextMetrics.countWords(tvConfig.seasonalArcSummary)
                val suffix = if (firstDraftState.isArcadeLocked) " ($arcWords/$maxArcWords words)" else ""
                Text(descriptor.arcLabel + suffix, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold)
            },
            placeholder = { Text(descriptor.arcPlaceholder, color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B)) },
            minLines = 3,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Items Grid (Episodes / Volumes / Films / Acts / Issues)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${descriptor.itemsSectionTitle} (${tvConfig.episodes.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isDark) LightGold else Color(0xFF991B1B),
                    fontWeight = FontWeight.ExtraBold
                )
            )

            GlassActionButton(
                text = descriptor.addItemButtonLabel,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = descriptor.addItemButtonLabel, tint = Color.White, modifier = Modifier.size(15.dp)) },
                onClick = {
                    val nextNum = tvConfig.episodes.size + 1
                    val newTitle = when {
                        descriptor.itemPrefix == "VOL." -> "Volume $nextNum"
                        descriptor.itemPrefix == "FILM" -> "Part $nextNum"
                        descriptor.itemPrefix == "ACT" -> "Act $nextNum"
                        descriptor.itemPrefix == "ISSUE" -> "Issue #$nextNum"
                        else -> "Episode $nextNum"
                    }
                    val newEp = TvEpisodeOutline(episodeNumber = nextNum, title = newTitle)
                    onUpdateTvConfig(tvConfig.copy(episodes = tvConfig.episodes + newEp))
                },
                isPrimary = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tvConfig.episodes.forEachIndexed { index, ep ->
                GlassCardItem(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCompactScreen) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${descriptor.itemPrefix} ${ep.episodeNumber}:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = ep.title,
                                    onValueChange = { newTitle ->
                                        val updated = tvConfig.episodes.toMutableList()
                                        updated[index] = ep.copy(title = newTitle)
                                        onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleSmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    label = { Text(descriptor.itemTitleLabel, fontSize = 10.sp, color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontWeight = FontWeight.Bold) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = ep.status,
                                    onValueChange = { newStatus ->
                                        val updated = tvConfig.episodes.toMutableList()
                                        updated[index] = ep.copy(status = newStatus)
                                        onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                    },
                                    modifier = Modifier.width(90.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isDark) LightGold else Color(0xFF991B1B),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    label = { Text("Status", fontSize = 10.sp, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) }
                                )
                                if (tvConfig.episodes.size > 1) {
                                    IconButton(onClick = {
                                        val updated = tvConfig.episodes.filterIndexed { i, _ -> i != index }
                                        onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = ep.aStory,
                                onValueChange = { newA ->
                                    val updated = tvConfig.episodes.toMutableList()
                                    updated[index] = ep.copy(aStory = newA)
                                    onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                ),
                                placeholder = { Text("Primary narrative focus...", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                label = { Text(descriptor.itemStoryALabel, color = if (isDark) LightGold else Color(0xFF991B1B), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = ep.bStory,
                                onValueChange = { newB ->
                                    val updated = tvConfig.episodes.toMutableList()
                                    updated[index] = ep.copy(bStory = newB)
                                    onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                ),
                                placeholder = { Text("Secondary narrative or character thread...", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                label = { Text(descriptor.itemStoryBLabel, color = if (isDark) LightGold else Color(0xFF991B1B), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                maxLines = 3
                            )
                        }
                    } else {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${descriptor.itemPrefix} ${ep.episodeNumber}:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = ep.title,
                                        onValueChange = { newTitle ->
                                            val updated = tvConfig.episodes.toMutableList()
                                            updated[index] = ep.copy(title = newTitle)
                                            onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        label = { Text(descriptor.itemTitleLabel, fontSize = 10.sp, color = if (isDark) ScreenplayTextMuted else Color(0xFF334155), fontWeight = FontWeight.Bold) },
                                        textStyle = MaterialTheme.typography.titleSmall.copy(
                                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = ep.status,
                                        onValueChange = { newStatus ->
                                            val updated = tvConfig.episodes.toMutableList()
                                            updated[index] = ep.copy(status = newStatus)
                                            onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                        },
                                        modifier = Modifier.width(130.dp),
                                        singleLine = true,
                                        label = { Text("Status", fontSize = 10.sp, color = if (isDark) LightGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) },
                                        textStyle = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isDark) LightGold else Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                if (tvConfig.episodes.size > 1) {
                                    IconButton(onClick = {
                                        val updated = tvConfig.episodes.filterIndexed { i, _ -> i != index }
                                        onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = ep.aStory,
                                    onValueChange = { newA ->
                                        val updated = tvConfig.episodes.toMutableList()
                                        updated[index] = ep.copy(aStory = newA)
                                        onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    placeholder = { Text("Primary narrative focus...", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                    label = { Text(descriptor.itemStoryALabel, color = if (isDark) LightGold else Color(0xFF991B1B), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                OutlinedTextField(
                                    value = ep.bStory,
                                    onValueChange = { newB ->
                                        val updated = tvConfig.episodes.toMutableList()
                                        updated[index] = ep.copy(bStory = newB)
                                        onUpdateTvConfig(tvConfig.copy(episodes = updated))
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    placeholder = { Text("Secondary narrative or character thread...", color = if (isDark) ScreenplayTextMuted else Color(0xFF64748B), fontSize = 11.sp) },
                                    label = { Text(descriptor.itemStoryBLabel, color = if (isDark) LightGold else Color(0xFF991B1B), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
