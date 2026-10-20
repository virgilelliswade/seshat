package com.seshat.writer.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import com.seshat.writer.library.ScriptLibraryManager
import com.seshat.writer.library.ScriptProject
import com.seshat.writer.importer.ScriptImporter
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.R
import com.seshat.writer.library.InitialWorkspaceView
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.BrandAccent
import com.seshat.writer.ui.theme.CircuitCyan
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.RawLightSurface
import com.seshat.writer.ui.theme.RawScreenplayTextMutedLight
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import androidx.compose.ui.graphics.Brush
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.smokedGlassBrush
import com.seshat.writer.templates.ProjectTemplate
import com.seshat.writer.templates.TemplateRegistry
import kotlinx.coroutines.delay

/**
 * Navigation steps within the Start Menu hierarchy.
 */
enum class StartMenuStep {
    SPLASH_DISSOLVE,   // Kept for backward compatibility
    TOP_LEVEL,         // [Start New Project] vs [Open Existing Project] vs [Continue Writing]
    FORMAT_SELECT,     // Level 1: [Single Screenplay] vs [Screenplay Universe] vs [Stage Play] vs [Comic Book] vs [Novel]
    UNIVERSE_SELECT,   // Level 2 (Universe Sub-menu): [Cinematic Universe (Features)] vs [Television Universe]
    NOVEL_SELECT,      // Level 2 (Novel Sub-menu): [Single Novel] vs [Novel Series]
    SCREENPLAY_TYPE,   // Level 2 (TV Series Only): [1-Hour Drama Show] vs [30-Min Comedy Show]
    TEMPLATE_SELECT,   // Level 3: [Blank (Default)] vs [Template 1] vs [Template 2]...
    WORKSPACE_VIEW     // Level 4: [Lore Doc & Story Bible] vs [Corkboard Outline] vs [Drafting Canvas]
}

@Composable
fun StartMenuScreen(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    onCloseMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var areButtonsVisible by remember { mutableStateOf(true) }
    var currentStep by remember { mutableStateOf(StartMenuStep.TOP_LEVEL) }
    var selectedFormat by remember { mutableStateOf(ProjectFormat.SCREENPLAY) }
    var selectedScreenplayType by remember { mutableStateOf<ScreenplayType?>(ScreenplayType.FEATURE) }
    var selectedTemplate by remember { mutableStateOf<ProjectTemplate?>(null) }
    var projectTitleInput by remember { mutableStateOf("") }
    var showLibraryDialog by remember { mutableStateOf(false) }
    var isUniverseMode by remember { mutableStateOf(false) }

    val buttonsAlpha by animateFloatAsState(
        targetValue = if (areButtonsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "StartMenuButtonsAlpha"
    )

    val buttonsOffsetY by animateDpAsState(
        targetValue = if (areButtonsVisible) 0.dp else 20.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "StartMenuButtonsOffsetY"
    )

    val scrimAlpha by animateFloatAsState(
        targetValue = if (areButtonsVisible) 0.45f else 0.0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "StartMenuScrimAlpha"
    )

    if (showLibraryDialog) {
        ScriptLibraryDialog(
            viewModel = viewModel,
            onDismiss = {
                showLibraryDialog = false
                onCloseMenu()
            }
        )
    }

    if (uiState.isSettingsDialogOpen) {
        StudioSettingsDialog(
            currentThemeMode = uiState.themeMode,
            onThemeModeChanged = { newMode ->
                viewModel.setThemeMode(context, newMode)
            },
            onDismiss = {
                viewModel.closeSettingsDialog()
            }
        )
    }

    val isDark = LocalSeshatDarkTheme.current
    val hasActiveProject = uiState.title.isNotBlank() && uiState.title != "UNTITLED SCREENPLAY"

    val backgroundModifier = if (isDark) {
        Modifier.background(Color.Black)
    } else {
        Modifier.background(Color.White)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier)
            .then(
                if (!areButtonsVisible) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        areButtonsVisible = true
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // =========================================================================
        // FULLSCREEN CLEAN STUDIO BACKGROUND & SESHAT STATUE (NO UI / NO BUTTONS)
        // =========================================================================
        Image(
            painter = painterResource(id = R.drawable.seshat_studio_splash),
            contentDescription = "Goddess Seshat Studio - Mistress of the House of Books",
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        )

        // Dynamic Scrim over Studio & Statue: Completely clear during 5s hero intro,
        // then smoothly fades in as buttons appear so all text and cards have 100% legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background((if (isDark) Color.Black else Color.White).copy(alpha = scrimAlpha))
        )

        // =========================================================================
        // FOREGROUND CONTENT: TOP BAR, LARGE TITLE & FLYING BUTTONS
        // =========================================================================
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Theme toggle, Settings button, and return-to-script button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .graphicsLayer {
                        alpha = if (areButtonsVisible) 1f else 0.4f
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme Mode Toggle (☀️ Light / 🌙 Dark)
                    StartMenuTopBarButton(
                        text = if (isDark) "🌙 Dark Mode" else "☀️ Light Mode",
                        onClick = { viewModel.toggleThemeMode(context) }
                    )

                    // Centrally available Studio Settings Button
                    StartMenuTopBarButton(
                        text = "⚙️ Settings",
                        onClick = { viewModel.openSettingsDialog() }
                    )
                }

                // Return to Script button (crisp, light, high-contrast text)
                if (hasActiveProject) {
                    StartMenuTopBarButton(
                        text = "Return to '${uiState.title.take(18)}'",
                        trailingIcon = Icons.Default.Close,
                        onClick = onCloseMenu
                    )
                }
            }

            // =====================================================================
            // LARGER FONT FOR THE WORD SESHAT (PERSISTENT & MAJESTIC AT THE TOP)
            // =====================================================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 6.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val titleFontSize = if (maxWidth >= 600.dp) {
                        64.sp
                    } else if (maxWidth >= 400.dp) {
                        52.sp
                    } else {
                        46.sp
                    }
                    val titleSpacing = if (maxWidth >= 600.dp) 10.dp else 6.dp

                    SeshatNeonTitle(
                        fontSize = titleFontSize,
                        letterSpacing = titleSpacing,
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .neonBloom(
                            color = ScribeStarNeonRed,
                            cornerRadius = 20.dp,
                            glowRadius = 6.dp,
                            strokeWidth = 1.4.dp,
                            outerAlpha = 0.22f,
                            midAlpha = 0.42f
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) smokedGlassBrush(glassOpacity = 0.92f) else opticalPillBrush(isDark = false))
                        .border(BorderStroke(1.5.dp, ScribeStarNeonRed), RoundedCornerShape(20.dp))
                ) {
                    Text(
                        text = "W R I T I N G   S T U D I O",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }

            // =====================================================================
            // 3. BELOW THE WORD TO BOTTOM OF SCREEN: FLYING BUTTONS & CARDS
            // Floating cleanly in front of the clean studio background & statue
            // =====================================================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Subtle tap-to-skip hint during the 5-second showcase
                if (!areButtonsVisible) {
                    Text(
                        text = "Tap anywhere to begin",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = (if (isDark) Color.White else Color(0xFF0F172A)).copy(alpha = 0.85f),
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }

                if (buttonsAlpha > 0.01f) {
                    Crossfade(
                        targetState = currentStep,
                        animationSpec = tween(durationMillis = 350),
                        label = "StartMenuCrossfade"
                    ) { step ->
                        when (step) {
                            StartMenuStep.SPLASH_DISSOLVE,
                            StartMenuStep.TOP_LEVEL -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(bottom = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlassStartMenuHub(
                                        hasActiveProject = hasActiveProject,
                                        activeTitle = uiState.title,
                                        onSelectFormat = { format, type ->
                                            if (format == ProjectFormat.NOVEL) {
                                                projectTitleInput = ""
                                                isUniverseMode = false
                                                currentStep = StartMenuStep.NOVEL_SELECT
                                            } else {
                                                selectedFormat = format
                                                selectedScreenplayType = type
                                                selectedTemplate = TemplateRegistry.getBlankTemplate(format, type)
                                                isUniverseMode = false
                                                currentStep = StartMenuStep.WORKSPACE_VIEW
                                            }
                                        },
                                        onSelectUniverse = {
                                            isUniverseMode = true
                                            currentStep = StartMenuStep.UNIVERSE_SELECT
                                        },
                                        onNewProject = {
                                            projectTitleInput = ""
                                            isUniverseMode = false
                                            currentStep = StartMenuStep.FORMAT_SELECT
                                        },
                                        onOpenLibrary = {
                                            showLibraryDialog = true
                                        },
                                        onContinueWriting = {
                                            onCloseMenu()
                                        },
                                        onOpenProject = { proj ->
                                            val imp = ScriptImporter.importFromFile(proj.file)
                                            if (imp.isSuccess && imp.document != null) {
                                                viewModel.loadDocument(imp.document.blocks, imp.document.titlePage)
                                                Toast.makeText(context, "Opened '${proj.title}'", Toast.LENGTH_SHORT).show()
                                                onCloseMenu()
                                            } else {
                                                Toast.makeText(context, "Failed to load: ${imp.errorMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.graphicsLayer {
                                            alpha = buttonsAlpha
                                            translationY = buttonsOffsetY.toPx()
                                        }
                                    )
                                }
                            }

                            StartMenuStep.FORMAT_SELECT -> {
                                FormatSelectContent(
                                    onSelectSingleScreenplay = {
                                        selectedFormat = ProjectFormat.SCREENPLAY
                                        selectedScreenplayType = ScreenplayType.FEATURE
                                        selectedTemplate = TemplateRegistry.BLANK_FEATURE
                                        isUniverseMode = false
                                        currentStep = StartMenuStep.WORKSPACE_VIEW
                                    },
                                    onSelectUniverse = {
                                        isUniverseMode = true
                                        currentStep = StartMenuStep.UNIVERSE_SELECT
                                    },
                                    onSelectStagePlay = {
                                        selectedFormat = ProjectFormat.STAGE_PLAY
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_STAGE_PLAY
                                        isUniverseMode = false
                                        currentStep = StartMenuStep.WORKSPACE_VIEW
                                    },
                                    onSelectComicBook = {
                                        selectedFormat = ProjectFormat.COMIC_BOOK
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_COMIC_BOOK
                                        isUniverseMode = false
                                        currentStep = StartMenuStep.WORKSPACE_VIEW
                                    },
                                    onSelectNovel = {
                                        projectTitleInput = ""
                                        currentStep = StartMenuStep.NOVEL_SELECT
                                    },
                                    onBack = { currentStep = StartMenuStep.TOP_LEVEL }
                                )
                            }

                            StartMenuStep.UNIVERSE_SELECT -> {
                                UniverseSelectContent(
                                    onSelectCinematicFeatures = {
                                        selectedFormat = ProjectFormat.SCREENPLAY
                                        selectedScreenplayType = ScreenplayType.FEATURE
                                        selectedTemplate = TemplateRegistry.BLANK_FEATURE
                                        isUniverseMode = true
                                        currentStep = StartMenuStep.WORKSPACE_VIEW
                                    },
                                    onSelectTelevisionUniverse = {
                                        selectedFormat = ProjectFormat.TV_SERIES
                                        selectedScreenplayType = ScreenplayType.TV_HOUR_DRAMA
                                        selectedTemplate = TemplateRegistry.BLANK_TV
                                        isUniverseMode = true
                                        currentStep = StartMenuStep.WORKSPACE_VIEW
                                    },
                                    onBack = { currentStep = StartMenuStep.FORMAT_SELECT }
                                )
                            }

                            StartMenuStep.NOVEL_SELECT -> {
                                NovelSelectContent(
                                    onSelectSingleNovel = {
                                        selectedFormat = ProjectFormat.NOVEL
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_NOVEL
                                        isUniverseMode = false
                                        currentStep = StartMenuStep.WORKSPACE_VIEW
                                    },
                                    onSelectNovelSeries = {
                                        selectedFormat = ProjectFormat.NOVEL
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_NOVEL
                                        isUniverseMode = true
                                        currentStep = StartMenuStep.WORKSPACE_VIEW
                                    },
                                    onBack = { currentStep = StartMenuStep.FORMAT_SELECT }
                                )
                            }

                            StartMenuStep.SCREENPLAY_TYPE -> {
                                StartMenuLayout(
                                    headerTitle = "TELEVISION SERIES FORMAT",
                                    headerSubtitle = "Select your show pacing, episodic deck structure, and seasonal arc",
                                    showBackButton = true,
                                    onBack = { currentStep = if (isUniverseMode) StartMenuStep.UNIVERSE_SELECT else StartMenuStep.FORMAT_SELECT }
                                ) {
                                    StartMenuBigButton(
                                        badgeText = "📺",
                                        title = "1-HOUR DRAMA SHOW (60m)",
                                        subtitle = "Prestige, cable & serialized drama (~50–60 min) • 5 Acts + Teaser, episodic deck & seasonal arc",
                                        badgeColor = Color(0xFF3B82F6),
                                        onClick = {
                                            selectedScreenplayType = ScreenplayType.TV_HOUR_DRAMA
                                            selectedTemplate = TemplateRegistry.BLANK_TV
                                            currentStep = StartMenuStep.WORKSPACE_VIEW
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    StartMenuBigButton(
                                        badgeText = "🛋️",
                                        title = "30-MIN COMEDY SHOW (30m)",
                                        subtitle = "Multi-cam or single-cam sitcom (~25–35 min) • 2–3 Acts + Cold Open & Tag, episodic deck",
                                        badgeColor = Color(0xFFEC4899),
                                        onClick = {
                                            selectedScreenplayType = ScreenplayType.TV_HALF_HOUR_COMEDY
                                            selectedTemplate = TemplateRegistry.BLANK_TV
                                            currentStep = StartMenuStep.WORKSPACE_VIEW
                                        }
                                    )
                                }
                            }

                            StartMenuStep.TEMPLATE_SELECT -> {
                                val templates = remember(selectedFormat, selectedScreenplayType) {
                                    TemplateRegistry.getTemplatesFor(selectedFormat, selectedScreenplayType)
                                }
                                val formatName = when (selectedFormat) {
                                    ProjectFormat.NOVEL -> "NOVEL"
                                    ProjectFormat.STAGE_PLAY -> "STAGE PLAY"
                                    ProjectFormat.COMIC_BOOK -> "COMIC BOOK"
                                    ProjectFormat.TV_SERIES -> if (selectedScreenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY) "30-MIN COMEDY" else "1-HOUR DRAMA"
                                    ProjectFormat.SCREENPLAY -> if (isUniverseMode) "CINEMATIC UNIVERSE" else "SINGLE SCREENPLAY"
                                }
                                StartMenuLayout(
                                    headerTitle = "CHOOSE TEMPLATE • $formatName",
                                    headerSubtitle = "Start with a clean blank page or select a structured storytelling framework",
                                    showBackButton = true,
                                    onBack = {
                                        if (selectedFormat == ProjectFormat.TV_SERIES) {
                                            currentStep = StartMenuStep.SCREENPLAY_TYPE
                                        } else if (isUniverseMode) {
                                            currentStep = StartMenuStep.UNIVERSE_SELECT
                                        } else {
                                            currentStep = StartMenuStep.FORMAT_SELECT
                                        }
                                    }
                                ) {
                                    templates.forEach { tmpl ->
                                        StartMenuBigButton(
                                            badgeText = tmpl.badgeIcon,
                                            title = tmpl.name.uppercase(),
                                            subtitle = if (tmpl.isBlank) "${tmpl.subtitle} • Clean blank canvas with zero sample text" else "${tmpl.subtitle} • ${tmpl.description}",
                                            badgeColor = tmpl.badgeColor,
                                            onClick = {
                                                selectedTemplate = tmpl
                                                currentStep = StartMenuStep.WORKSPACE_VIEW
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
                                }
                            }

                            StartMenuStep.WORKSPACE_VIEW -> {
                                WorkspaceViewContent(
                                    selectedFormat = selectedFormat,
                                    selectedScreenplayType = selectedScreenplayType,
                                    isUniverseMode = isUniverseMode,
                                    projectTitleInput = projectTitleInput,
                                    onTitleChange = { projectTitleInput = it },
                                    isDark = isDark,
                                    onSelectView = { view ->
                                        viewModel.startNewProject(
                                            context = context,
                                            format = selectedFormat,
                                            screenplayType = selectedScreenplayType,
                                            initialWorkspaceView = view,
                                            customTitle = projectTitleInput,
                                            template = selectedTemplate
                                        )
                                        onCloseMenu()
                                    },
                                    onBack = {
                                        if (selectedFormat == ProjectFormat.NOVEL) {
                                            currentStep = StartMenuStep.NOVEL_SELECT
                                        } else if (isUniverseMode) {
                                            currentStep = StartMenuStep.UNIVERSE_SELECT
                                        } else {
                                            currentStep = StartMenuStep.FORMAT_SELECT
                                        }
                                    }
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
private fun FormatSelectContent(
    onSelectSingleScreenplay: () -> Unit,
    onSelectUniverse: () -> Unit,
    onSelectStagePlay: () -> Unit,
    onSelectComicBook: () -> Unit,
    onSelectNovel: () -> Unit,
    onBack: () -> Unit
) {
    StartMenuLayout(
        headerTitle = "CHOOSE MEDIUM",
        headerSubtitle = "What format are you creating today?",
        showBackButton = true,
        onBack = onBack
    ) {
        // 1. SINGLE SCREENPLAY
        StartMenuBigButton(
            badgeText = "🎬",
            title = "SINGLE SCREENPLAY",
            subtitle = "Standalone feature film or individual script (~90–120 min) • Industry standard Courier 12pt",
            badgeColor = ElectrumGold,
            onClick = onSelectSingleScreenplay
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. SCREENPLAY UNIVERSE
        StartMenuBigButton(
            badgeText = "🪐",
            title = "SCREENPLAY UNIVERSE",
            subtitle = "Multi-script story universe • Interconnected Cinematic Universes (Features) & Serialized Television Universes with shared Lore Bible & continuity",
            badgeColor = ScribeStarNeonRed,
            onClick = onSelectUniverse
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. THEATRICAL STAGE PLAY
        StartMenuBigButton(
            badgeText = "🎭",
            title = "THEATRICAL STAGE PLAY",
            subtitle = "Proscenium, black box & musical theatre • Acts, scenes, stage directions & rapid dialogue switching",
            badgeColor = ElectrumGold,
            onClick = onSelectStagePlay
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. COMIC BOOK SCRIPT
        StartMenuBigButton(
            badgeText = "💥",
            title = "COMIC BOOK SCRIPT",
            subtitle = "Standard comic page & panel scripting • Page headings, panel descriptions, art notes, lettering & sound effects",
            badgeColor = ScribeStarNeonRed,
            onClick = onSelectComicBook
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. NOVEL
        StartMenuBigButton(
            badgeText = "📖",
            title = "NOVEL",
            subtitle = "Prose book manuscripts, chapter-based structures, and long-form narrative fiction",
            badgeColor = Color(0xFF8B5CF6),
            onClick = onSelectNovel
        )
    }
}

@Composable
private fun UniverseSelectContent(
    onSelectCinematicFeatures: () -> Unit,
    onSelectTelevisionUniverse: () -> Unit,
    onBack: () -> Unit
) {
    StartMenuLayout(
        headerTitle = "SCREENPLAY UNIVERSE",
        headerSubtitle = "Select your interconnected storytelling medium",
        showBackButton = true,
        onBack = onBack
    ) {
        // Option 1: Cinematic Universe (Features)
        StartMenuBigButton(
            badgeText = "🎬",
            title = "CINEMATIC UNIVERSE (FEATURES)",
            subtitle = "Multi-film franchise, trilogy, or connected cinematic universe • Shared Master Lore Bible, installment arcs, and sequel/prequel continuity",
            badgeColor = ElectrumGold,
            onClick = onSelectCinematicFeatures
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Option 2: Television Universe
        StartMenuBigButton(
            badgeText = "📺",
            title = "TELEVISION UNIVERSE",
            subtitle = "Serialized TV show, multi-season universe, or episodic anthology • Episode binders (101, 102...), seasonal arcs, and unified Series Lore Bible",
            badgeColor = Color(0xFF3B82F6),
            onClick = onSelectTelevisionUniverse
        )
    }
}

@Composable
private fun NovelSelectContent(
    onSelectSingleNovel: () -> Unit,
    onSelectNovelSeries: () -> Unit,
    onBack: () -> Unit
) {
    StartMenuLayout(
        headerTitle = "NOVEL ARCHITECTURE",
        headerSubtitle = "Are you crafting a standalone book or an interconnected series?",
        showBackButton = true,
        onBack = onBack
    ) {
        // Choice 1: Single Novel
        StartMenuBigButton(
            badgeText = "📖",
            title = "SINGLE NOVEL",
            subtitle = "Standalone prose manuscript, novel or book • Dedicated World Lore Bible, chapter corkboard & draft manuscript",
            badgeColor = Color(0xFF8B5CF6),
            onClick = onSelectSingleNovel
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Choice 2: Novel Series
        StartMenuBigButton(
            badgeText = "📚",
            title = "NOVEL SERIES",
            subtitle = "Multi-book saga, trilogy, or interconnected prose universe • Unified Series Master Lore Bible, multi-book swimlanes & saga continuity",
            badgeColor = ScribeStarNeonRed,
            onClick = onSelectNovelSeries
        )
    }
}

@Composable
private fun WorkspaceViewContent(
    selectedFormat: ProjectFormat,
    selectedScreenplayType: ScreenplayType?,
    isUniverseMode: Boolean,
    projectTitleInput: String,
    onTitleChange: (String) -> Unit,
    isDark: Boolean,
    onSelectView: (InitialWorkspaceView) -> Unit,
    onBack: () -> Unit
) {
    val formatLabel = when {
        isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "CINEMATIC UNIVERSE (FEATURES)"
        isUniverseMode && selectedFormat == ProjectFormat.TV_SERIES -> "TELEVISION UNIVERSE"
        isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "NOVEL SERIES"
        selectedFormat == ProjectFormat.NOVEL -> "SINGLE NOVEL"
        selectedFormat == ProjectFormat.STAGE_PLAY -> "STAGE PLAY"
        selectedFormat == ProjectFormat.COMIC_BOOK -> "COMIC BOOK"
        selectedFormat == ProjectFormat.TV_SERIES -> when (selectedScreenplayType) {
            ScreenplayType.TV_HALF_HOUR_COMEDY -> "TV COMEDY SHOW"
            else -> "TV DRAMA SHOW"
        }
        else -> "SINGLE SCREENPLAY"
    }

    StartMenuLayout(
        headerTitle = "STARTING WORKSPACE",
        headerSubtitle = "$formatLabel • Where would you like to begin?",
        showBackButton = true,
        onBack = onBack
    ) {
        // Optional Project Title Input - High Contrast
        Surface(
            color = if (isDark) Color(0xFF0F1522).copy(alpha = 0.92f) else ObsidianSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.4.dp, ScribeStarNeonRed.copy(alpha = 0.70f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .neonBloom(
                    color = ScribeStarNeonRed,
                    cornerRadius = 14.dp,
                    glowRadius = 6.dp,
                    strokeWidth = 1.4.dp,
                    outerAlpha = 0.12f,
                    midAlpha = 0.28f
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val titleLabel = if (isUniverseMode) {
                    when (selectedFormat) {
                        ProjectFormat.TV_SERIES -> "TELEVISION UNIVERSE TITLE (OPTIONAL)"
                        ProjectFormat.NOVEL -> "NOVEL SERIES TITLE (OPTIONAL)"
                        else -> "CINEMATIC UNIVERSE TITLE (OPTIONAL)"
                    }
                } else {
                    if (selectedFormat == ProjectFormat.NOVEL) "NOVEL TITLE (OPTIONAL)"
                    else "PROJECT TITLE (OPTIONAL)"
                }
                Text(
                    text = titleLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = ScribeStarNeonRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    if (projectTitleInput.isEmpty()) {
                        val placeholderText = when {
                            isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "e.g. The Dune Saga / Chronicle Universe"
                            isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "e.g. The Spire Saga / Foundation Universe"
                            selectedFormat == ProjectFormat.TV_SERIES -> "e.g. Scribes of Thebes"
                            selectedFormat == ProjectFormat.NOVEL -> "e.g. The Library of Alexandria"
                            else -> "e.g. The Emerald Tablet"
                        }
                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) Color(0xFF94A3B8) else ScreenplayTextMuted
                            )
                        )
                    }
                    BasicTextField(
                        value = projectTitleInput,
                        onValueChange = onTitleChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(ScribeStarNeonRed),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Choice 1: Lore Doc & Story Bible
        val loreTitle = when {
            isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "MASTER FRANCHISE BIBLE"
            isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "SERIES MASTER LORE BIBLE"
            selectedFormat == ProjectFormat.TV_SERIES -> "SERIES LORE BIBLE"
            selectedFormat == ProjectFormat.NOVEL -> "WORLD & LORE BIBLE"
            else -> "LORE DOC & STORY BIBLE"
        }
        val loreDesc = when {
            isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "Develop your Cinematic Franchise Bible (Universe Rules, Installment Arcs, Ensembles & Continuity)"
            isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "Multi-book series bible (Worldbuilding, Magic/Tech systems, Character dossiers, Book arcs & Continuity)"
            selectedFormat == ProjectFormat.TV_SERIES -> "Develop your Series Bible (Show Rules, Pilot Arc, Character Ensembles, Seasons)"
            selectedFormat == ProjectFormat.NOVEL -> "Worldbuilding encyclopedia, character dossiers, magic/tech systems, and lore notes"
            else -> "Character dossiers, locations, timeline chronology, and story rules before drafting"
        }
        StartMenuBigButton(
            badgeText = "📖",
            title = loreTitle,
            subtitle = loreDesc,
            badgeColor = ElectrumGold,
            onClick = { onSelectView(InitialWorkspaceView.LORE_DOC) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Choice 2: Corkboard Outline
        val outlineDesc = when {
            isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "Plan installment beats and scene index cards across film franchise swimlanes"
            isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "Structure multi-book story milestones and chapter cards across saga swimlanes"
            selectedFormat == ProjectFormat.NOVEL -> "Structure novel parts, milestones, and chapter cards across story swimlanes"
            selectedFormat == ProjectFormat.TV_SERIES -> "Plan episode act beats and index cards across tailored episodic swimlanes"
            else -> "Plan scene index cards across tailored 4-Act swimlanes with zero script pollution"
        }
        StartMenuBigButton(
            badgeText = "📌",
            title = "CORKBOARD OUTLINE",
            subtitle = outlineDesc,
            badgeColor = Color(0xFF3B82F6),
            onClick = { onSelectView(InitialWorkspaceView.OUTLINE) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Choice 3: Drafting Canvas
        val draftingTitle = when {
            isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "INSTALLMENT 1 SCREENPLAY"
            isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "DRAFT MANUSCRIPT (BOOK 1)"
            selectedFormat == ProjectFormat.NOVEL -> "DRAFT MANUSCRIPT"
            selectedFormat == ProjectFormat.TV_SERIES -> "PILOT SCRIPT (EPISODE 101)"
            else -> "SCREENPLAY EDITOR"
        }
        val draftingDesc = when {
            isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "Start fresh with a clean, blank Feature 1 script in Courier 12pt"
            isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "Start writing Book 1 with a clean, blank Chapter 1 prose canvas"
            selectedFormat == ProjectFormat.NOVEL -> "Start fresh with a clean, blank Chapter 1 prose canvas"
            selectedFormat == ProjectFormat.TV_SERIES -> "Start fresh with a clean, blank Episode 101 script"
            else -> "Start fresh with a clean, blank page in Courier 12pt"
        }
        StartMenuBigButton(
            badgeText = "✍️",
            title = draftingTitle,
            subtitle = draftingDesc,
            badgeColor = Color(0xFF10B981),
            onClick = { onSelectView(InitialWorkspaceView.EDITOR) }
        )
    }
}

/**
 * Interactive glowing top bar pill button with red mouseover glow and smooth scale animation.
 */
@Composable
private fun StartMenuTopBarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    accentColor: Color = ScribeStarNeonRed
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 12.dp else 4.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "TopBarBtnGlow"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.45f else 0.10f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "TopBarBtnOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.80f else 0.22f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "TopBarBtnMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isLit) 1.8.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "TopBarBtnStroke"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "TopBarBtnScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .neonBloom(
                color = accentColor,
                cornerRadius = 20.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isLit) 1.0f else 0.80f
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        if (isLit) listOf(Color(0x60450E1A), Color(0x7524050D))
                        else listOf(Color(0x351E293B), Color(0x450F172A))
                    } else {
                        if (isLit) listOf(Color(0xFFE11D48), Color(0xFFBE123C))
                        else listOf(Color(0xF5FFFFFF), Color(0xEDF8FAFC))
                    }
                )
            )
            .border(
                BorderStroke(
                    strokeWidth,
                    if (isDark) (if (isLit) accentColor else accentColor.copy(alpha = 0.70f))
                    else (if (isLit) accentColor else Color(0xFFCBD5E1))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isDark) Color.White else (if (isLit) Color.White else Color(0xFF0F172A)),
                    fontWeight = if (isLit) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = if (isLit) Color.White else (if (isDark) accentColor else Color(0xFF0F172A)),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Interactive glowing back navigation button with red mouseover bloom.
 */
@Composable
private fun StartMenuBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 14.dp else 2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BackBtnGlow"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.50f else 0.08f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BackBtnOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.85f else 0.18f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BackBtnMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isLit) 2.0.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BackBtnStroke"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isHovered) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "BackBtnScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(42.dp)
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 21.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isLit) 1.0f else 0.80f
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        if (isLit) listOf(Color(0x60450E1A), Color(0x7524050D))
                        else listOf(Color(0x351E293B), Color(0x450F172A))
                    } else {
                        if (isLit) listOf(Color(0xFFE11D48), Color(0xFFBE123C))
                        else listOf(Color(0xF5FFFFFF), Color(0xEDF8FAFC))
                    }
                )
            )
            .border(
                BorderStroke(
                    strokeWidth,
                    if (isDark) (if (isLit) ScribeStarNeonRed else ScribeStarNeonRed.copy(alpha = 0.65f))
                    else (if (isLit) ScribeStarNeonRed else Color(0xFFCBD5E1))
                ),
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = if (isLit) Color.White else (if (isDark) ScribeStarNeonRed else Color(0xFF0F172A)),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Standard layout wrapper for Start Menu steps with breadcrumbs and centered content,
 * overlaid directly on top of the permanent head-to-toe Goddess Seshat background.
 */
@Composable
private fun StartMenuLayout(
    headerTitle: String,
    headerSubtitle: String,
    showBackButton: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val isDark = LocalSeshatDarkTheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Back Navigation and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    StartMenuBackButton(onClick = onBack)
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = if (isDark) Color.White else BrandAccent,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = headerSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDark) Color(0xFFCBD5E1) else ScreenplayTextMuted,
                            letterSpacing = 0.5.sp,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            content()
        }
    }
}

/**
 * Sleek modern glowing capsule button matching the ScribeStar start menu design.
 */
@Composable
private fun StartMenuSleekCapsuleButton(
    title: String,
    iconType: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isHighlighted) 16.dp else 4.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "CapsuleGlowRadius"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.55f else 0.12f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "CapsuleOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.85f else 0.25f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "CapsuleMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.2.dp else 1.4.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "CapsuleStrokeWidth"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isHovered) 1.025f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "CapsuleScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .height(58.dp)
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 18.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 1.0f else 0.80f
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        if (isHighlighted) listOf(Color(0x60450E1A), Color(0x7524050D))
                        else listOf(Color(0x281E293B), Color(0x3D0F172A))
                    } else {
                        if (isHighlighted) listOf(Color(0xFFE11D48), Color(0xFFBE123C))
                        else listOf(Color(0xF5FFFFFF), Color(0xEDF8FAFC))
                    }
                )
            )
            .border(
                BorderStroke(
                    strokeWidth,
                    if (isHighlighted) ScribeStarNeonRed else if (isDark) ScribeStarNeonRed.copy(alpha = 0.70f) else Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                    color = if (isDark) Color.White else (if (isHighlighted) Color.White else Color(0xFF0F172A)),
                    fontSize = 15.sp
                )
            )

            SleekStartIcon(
                type = iconType,
                color = if (isHighlighted) Color.White else (if (isDark) ScribeStarNeonRed else Color(0xFF0F172A))
            )
        }
    }
}

/**
 * Procedural neon outline glyph for start menu actions (Play ▶, Folder 📁, Edit ✍️)
 */
@Composable
private fun SleekStartIcon(type: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        try {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            when (type) {
                0 -> { // Play arrow triangle
                    val path = Path().apply {
                        moveTo(w * 0.28f, h * 0.18f)
                        lineTo(w * 0.82f, h * 0.5f)
                        lineTo(w * 0.28f, h * 0.82f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                1 -> { // Folder outline
                    val path = Path().apply {
                        moveTo(w * 0.15f, h * 0.32f)
                        lineTo(w * 0.42f, h * 0.32f)
                        lineTo(w * 0.52f, h * 0.45f)
                        lineTo(w * 0.85f, h * 0.45f)
                        lineTo(w * 0.85f, h * 0.80f)
                        lineTo(w * 0.15f, h * 0.80f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                else -> { // Edit Quill / Pen
                    val path = Path().apply {
                        moveTo(w * 0.72f, h * 0.16f)
                        lineTo(w * 0.84f, h * 0.28f)
                        lineTo(w * 0.36f, h * 0.78f)
                        lineTo(w * 0.16f, h * 0.84f)
                        lineTo(w * 0.22f, h * 0.64f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        } catch (_: Throwable) {
            // Defensive drawing fallback
        }
    }
}

/**
 * Modern tactile card button with ScribeStar neon styling for sub-menu selections.
 */
@Composable
private fun StartMenuBigButton(
    badgeText: String,
    title: String,
    subtitle: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isHighlighted) 18.dp else 4.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BigBtnGlowRadius"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.60f else 0.12f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BigBtnOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.90f else 0.25f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BigBtnMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.2.dp else 1.4.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BigBtnStrokeWidth"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isHovered) 1.025f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "BigBtnScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 18.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 1.0f else 0.80f
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        if (isHighlighted) listOf(
                            Color(0x60450E1A),
                            Color(0x7524050D)
                        ) else listOf(
                            Color(0x281E293B),
                            Color(0x3D0F172A)
                        )
                    } else {
                        if (isHighlighted) listOf(
                            Color(0xFFE11D48),
                            Color(0xFFBE123C)
                        ) else listOf(
                            Color(0xF5FFFFFF),
                            Color(0xEDF8FAFC)
                        )
                    }
                )
            )
            .border(
                BorderStroke(
                    width = strokeWidth,
                    color = if (isHighlighted) ScribeStarNeonRed else if (isDark) ScribeStarNeonRed.copy(alpha = 0.70f) else Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Surface(
                color = if (isHighlighted) badgeColor.copy(alpha = 0.35f) else badgeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(if (isHighlighted) 1.6.dp else 1.2.dp, if (isHighlighted) ScribeStarNeonRed else badgeColor.copy(alpha = 0.8f)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeText,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Titles
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = if (isDark) Color.White else (if (isHighlighted) Color.White else Color(0xFF0F172A)),
                        fontSize = 15.sp
                    )
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = if (isDark) {
                                if (isHighlighted) Color.White.copy(alpha = 0.95f) else Color(0xFFCBD5E1)
                            } else {
                                if (isHighlighted) Color.White.copy(alpha = 0.95f) else Color(0xFF475569)
                            },
                            letterSpacing = 0.4.sp,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isHighlighted) Color.White else ScribeStarNeonRed,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        translationX = if (isHighlighted) 4.dp.toPx() else 0f
                    }
            )
        }
    }
}

/**
 * Modern Glass Start Menu Hub matching the visual concept showcase (Concept B).
 * Features floating format cards flanking the Goddess Seshat statue on left & right,
 * and a centered floating action pill dock (+ New Project, Import Script, Continue) at the bottom.
 */
@Composable
private fun GlassStartMenuHub(
    hasActiveProject: Boolean,
    activeTitle: String,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinueWriting: () -> Unit,
    onOpenProject: (ScriptProject) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recentProjects = remember(context, activeTitle) {
        try {
            ScriptLibraryManager.listProjects(context, activeTitle).take(4)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    val handleContinue = remember(hasActiveProject, recentProjects, onContinueWriting, onOpenProject, onNewProject) {
        {
            if (hasActiveProject) {
                onContinueWriting()
            } else if (recentProjects.isNotEmpty()) {
                onOpenProject(recentProjects.first())
            } else {
                onNewProject()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val availableWidth = maxWidth
        if (availableWidth >= 680.dp) {
            GlassStartMenuHubWide(
                availableWidth = availableWidth,
                onSelectFormat = onSelectFormat,
                onSelectUniverse = onSelectUniverse,
                onNewProject = onNewProject,
                onOpenLibrary = onOpenLibrary,
                onContinue = handleContinue
            )
        } else if (availableWidth >= 480.dp) {
            GlassStartMenuHubMedium(
                availableWidth = availableWidth,
                onSelectFormat = onSelectFormat,
                onSelectUniverse = onSelectUniverse,
                onNewProject = onNewProject,
                onOpenLibrary = onOpenLibrary,
                onContinue = handleContinue
            )
        } else {
            GlassStartMenuHubCompact(
                availableWidth = availableWidth,
                onSelectFormat = onSelectFormat,
                onSelectUniverse = onSelectUniverse,
                onNewProject = onNewProject,
                onOpenLibrary = onOpenLibrary,
                onContinue = handleContinue
            )
        }
    }
}

@Composable
private fun GlassStartMenuHubWide(
    availableWidth: Dp,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Center justified with smooth outward expansion as window expands
    val cardWidth = (240.dp + (availableWidth - 680.dp) * 0.12f).coerceIn(240.dp, 310.dp)
    val centerGap = (100.dp + (availableWidth - 680.dp) * 0.18f).coerceIn(100.dp, 240.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Midsection: Strictly center-justified row with dynamic center gap framing Goddess Seshat
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(centerGap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top
        ) {
            // Left Column (Single Screenplay, Screenplay Universe)
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                GlassFormatCard(
                    title = "Single Screenplay",
                    iconType = 0,
                    onClick = { onSelectFormat(ProjectFormat.SCREENPLAY, ScreenplayType.FEATURE) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Screenplay Universe",
                    iconType = 5,
                    onClick = onSelectUniverse,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right Column (Stage Play, Comic Book, Novel)
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                GlassFormatCard(
                    title = "Stage Play",
                    iconType = 3,
                    onClick = { onSelectFormat(ProjectFormat.STAGE_PLAY, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Comic Book",
                    iconType = 4,
                    onClick = { onSelectFormat(ProjectFormat.COMIC_BOOK, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Novel",
                    iconType = 2,
                    onClick = { onSelectFormat(ProjectFormat.NOVEL, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Concept B Floating Action Pill Dock (Centered)
        ConceptBActionDock(
            isCompact = false,
            onNewProject = onNewProject,
            onOpenLibrary = onOpenLibrary,
            onContinue = onContinue
        )
    }
}

@Composable
private fun GlassStartMenuHubMedium(
    availableWidth: Dp,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = ((availableWidth - 56.dp) * 0.44f).coerceIn(190.dp, 240.dp)
    val centerGap = (24.dp + (availableWidth - 480.dp) * 0.15f).coerceIn(24.dp, 70.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(centerGap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassFormatCard(
                    title = "Single Screenplay",
                    iconType = 0,
                    onClick = { onSelectFormat(ProjectFormat.SCREENPLAY, ScreenplayType.FEATURE) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Screenplay Universe",
                    iconType = 5,
                    onClick = onSelectUniverse,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassFormatCard(
                    title = "Stage Play",
                    iconType = 3,
                    onClick = { onSelectFormat(ProjectFormat.STAGE_PLAY, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Comic Book",
                    iconType = 4,
                    onClick = { onSelectFormat(ProjectFormat.COMIC_BOOK, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Novel",
                    iconType = 2,
                    onClick = { onSelectFormat(ProjectFormat.NOVEL, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ConceptBActionDock(
            isCompact = false,
            onNewProject = onNewProject,
            onOpenLibrary = onOpenLibrary,
            onContinue = onContinue
        )
    }
}

@Composable
private fun GlassStartMenuHubCompact(
    availableWidth: Dp,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = ((availableWidth - 36.dp) / 2).coerceIn(145.dp, 200.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            GlassFormatCard(
                title = "Screenplay",
                iconType = 0,
                onClick = { onSelectFormat(ProjectFormat.SCREENPLAY, ScreenplayType.FEATURE) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
            GlassFormatCard(
                title = "Universe",
                iconType = 5,
                onClick = onSelectUniverse,
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            GlassFormatCard(
                title = "Stage Play",
                iconType = 3,
                onClick = { onSelectFormat(ProjectFormat.STAGE_PLAY, null) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
            GlassFormatCard(
                title = "Comic Book",
                iconType = 4,
                onClick = { onSelectFormat(ProjectFormat.COMIC_BOOK, null) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }
        GlassFormatCard(
            title = "Novel",
            iconType = 2,
            onClick = { onSelectFormat(ProjectFormat.NOVEL, null) },
            isCompact = true,
            modifier = Modifier.width(cardWidth)
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConceptBActionDock(
            isCompact = true,
            onNewProject = onNewProject,
            onOpenLibrary = onOpenLibrary,
            onContinue = onContinue
        )
    }
}

/**
 * Concept B Floating Action Pill Dock.
 * Displays + New Project (radiant neon red), Import Script (frosted glass), and Continue (smoky glass).
 */
@Composable
private fun ConceptBActionDock(
    isCompact: Boolean,
    onNewProject: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    Box(
        modifier = modifier
            .neonBloom(
                color = Color.Black,
                cornerRadius = 30.dp,
                glowRadius = 10.dp,
                outerAlpha = 0.35f,
                midAlpha = 0.60f
            )
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (isDark) Brush.verticalGradient(
                    listOf(Color(0x351E293B), Color(0x450F172A))
                ) else Brush.verticalGradient(
                    listOf(Color(0xE5FFFFFF), Color(0xD0F1F5F9))
                )
            )
            .border(
                BorderStroke(
                    1.2.dp,
                    if (isDark) Color(0x35FFFFFF) else Color(0x7094A3B8)
                ),
                RoundedCornerShape(30.dp)
            )
            .padding(horizontal = if (isCompact) 6.dp else 8.dp, vertical = if (isCompact) 5.dp else 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConceptBPrimaryPill(
                text = "+ New Project",
                onClick = onNewProject,
                isCompact = isCompact
            )

            ConceptBGlassPill(
                text = "Import Script",
                onClick = onOpenLibrary,
                isCompact = isCompact
            )

            ConceptBGlassPill(
                text = "Continue",
                onClick = onContinue,
                isCompact = isCompact,
                accentColor = Color(0xFF38BDF8)
            )
        }
    }
}

@Composable
private fun ConceptBPrimaryPill(
    text: String,
    onClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else if (isHovered) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "PrimaryPillScale"
    )
    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 16.dp else 8.dp,
        animationSpec = tween(durationMillis = 180),
        label = "PrimaryPillGlow"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 24.dp,
                glowRadius = glowRadius,
                strokeWidth = 1.4.dp,
                outerAlpha = if (isLit) 0.55f else 0.30f,
                midAlpha = if (isLit) 0.90f else 0.65f,
                coreAlpha = 1.0f
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    if (isLit) listOf(Color(0xFFFF3B50), Color(0xFFD61332))
                    else listOf(Color(0xFFFF2A3D), Color(0xFFB91028))
                )
            )
            .border(
                BorderStroke(1.2.dp, if (isLit) Color.White else Color(0xFFFF6B81)),
                RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = if (isCompact) 14.dp else 22.dp,
                vertical = if (isCompact) 9.dp else 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = MainAppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 12.5.sp else 14.sp,
                color = Color.White,
                letterSpacing = 0.3.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun ConceptBGlassPill(
    text: String,
    onClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = ScribeStarNeonRed
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else if (isHovered) 1.03f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "GlassPillScale"
    )
    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "GlassPillGlow"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isLit) {
                    Modifier.neonBloom(
                        color = accentColor,
                        cornerRadius = 24.dp,
                        glowRadius = glowRadius,
                        strokeWidth = 1.2.dp,
                        outerAlpha = 0.35f,
                        midAlpha = 0.70f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        if (isLit) listOf(Color(0x55334155), Color(0x651E293B))
                        else listOf(Color(0x381E293B), Color(0x480F172A))
                    } else {
                        if (isLit) listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                        else listOf(Color(0xF5FFFFFF), Color(0xEDF1F5F9))
                    }
                )
            )
            .border(
                BorderStroke(
                    1.1.dp,
                    if (isLit) accentColor else if (isDark) Color(0x40FFFFFF) else Color(0xFF94A3B8)
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = if (isCompact) 13.dp else 20.dp,
                vertical = if (isCompact) 9.dp else 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = MainAppFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = if (isCompact) 12.5.sp else 14.sp,
                color = if (isLit || isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                letterSpacing = 0.2.sp
            ),
            maxLines = 1
        )
    }
}

/**
 * Smoked glass card for Medium selection (Single Screenplay, Screenplay Universe, Stage Play, Comic Book, Novel).
 * Designed according to Concept B with horizontal pill card, glowing neon outline icon, and crisp Roboto typography.
 */
@Composable
private fun GlassFormatCard(
    title: String,
    iconType: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isHighlighted) 18.dp else 3.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "FormatCardGlowRadius"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.60f else 0.10f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "FormatCardOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.90f else 0.22f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "FormatCardMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.0.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "FormatCardStrokeWidth"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else if (isHovered) 1.03f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "FormatCardScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(if (isCompact) 52.dp else 60.dp)
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 18.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 1.0f else 0.70f
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        if (isHighlighted) listOf(Color(0x60450E1A), Color(0x7524050D))
                        else listOf(Color(0x351E293B), Color(0x480F172A))
                    } else {
                        if (isHighlighted) listOf(Color(0xFFE11D48), Color(0xFFBE123C))
                        else listOf(Color(0xF0FFFFFF), Color(0xD8F8FAFC))
                    }
                )
            )
            .border(
                BorderStroke(
                    width = strokeWidth,
                    color = if (isHighlighted) ScribeStarNeonRed else if (isDark) Color(0x35FFFFFF) else Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (isCompact) 12.dp else 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            FormatCardIcon(
                type = iconType,
                color = if (isHighlighted) Color.White else ScribeStarNeonRed,
                modifier = Modifier.size(if (isCompact) 26.dp else 32.dp)
            )

            Spacer(modifier = Modifier.width(if (isCompact) 10.dp else 16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = if (isHighlighted || isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                    fontSize = if (isCompact) 13.sp else 16.sp,
                    letterSpacing = 0.2.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Procedural vector neon icons matching Concept B.
 */
@Composable
private fun FormatCardIcon(type: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 1.8.dp.toPx()
        when (type) {
            0 -> { // Single Screenplay (document outline with dog-eared folded corner & 3 script lines)
                val left = w * 0.18f
                val right = w * 0.82f
                val top = h * 0.12f
                val bottom = h * 0.88f
                val fold = w * 0.22f
                val docPath = Path().apply {
                    moveTo(left, top)
                    lineTo(right - fold, top)
                    lineTo(right, top + fold)
                    lineTo(right, bottom)
                    lineTo(left, bottom)
                    close()
                }
                drawPath(path = docPath, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                val flapPath = Path().apply {
                    moveTo(right - fold, top)
                    lineTo(right - fold, top + fold)
                    lineTo(right, top + fold)
                }
                drawPath(path = flapPath, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                val lineL = left + w * 0.12f
                val lineR = right - w * 0.12f
                drawLine(color = color, start = Offset(lineL, top + h * 0.38f), end = Offset(lineR, top + h * 0.38f), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(lineL, top + h * 0.52f), end = Offset(lineR, top + h * 0.52f), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(lineL, top + h * 0.66f), end = Offset(lineL + (lineR - lineL) * 0.60f, top + h * 0.66f), strokeWidth = strokeW, cap = StrokeCap.Round)
            }
            1 -> { // TV Series
                drawLine(color = Color.White.copy(alpha = 0.7f), start = Offset(w * 0.5f, h * 0.22f), end = Offset(w * 0.32f, h * 0.08f), strokeWidth = strokeW)
                drawLine(color = Color.White.copy(alpha = 0.7f), start = Offset(w * 0.5f, h * 0.22f), end = Offset(w * 0.68f, h * 0.08f), strokeWidth = strokeW)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.12f, h * 0.22f),
                    size = Size(w * 0.76f, h * 0.66f),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(width = strokeW)
                )
                val playPath = Path().apply {
                    moveTo(w * 0.44f, h * 0.44f)
                    lineTo(w * 0.60f, h * 0.55f)
                    lineTo(w * 0.44f, h * 0.66f)
                    close()
                }
                drawPath(path = playPath, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            2 -> { // Novel (closed bound book with spine & debossed bookmark lines)
                val bL = w * 0.24f
                val bR = w * 0.76f
                val bT = h * 0.16f
                val bB = h * 0.84f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(bL, bT),
                    size = Size(bR - bL, bB - bT),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(width = strokeW)
                )
                drawLine(color = color, start = Offset(bL + w * 0.10f, bT), end = Offset(bL + w * 0.10f, bB), strokeWidth = strokeW)
                drawLine(color = color, start = Offset(bL + w * 0.22f, bT + h * 0.24f), end = Offset(bR - w * 0.10f, bT + h * 0.24f), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(bL + w * 0.22f, bT + h * 0.38f), end = Offset(bR - w * 0.10f, bT + h * 0.38f), strokeWidth = strokeW, cap = StrokeCap.Round)
            }
            3 -> { // Stage Play (theater stage arch with curtain valance & audience heads)
                val aLeft = w * 0.15f
                val aRight = w * 0.85f
                val aTop = h * 0.15f
                val aBottom = h * 0.68f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(aLeft, aTop),
                    size = Size(aRight - aLeft, aBottom - aTop),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(width = strokeW)
                )
                val curtainPath = Path().apply {
                    moveTo(aLeft, aTop + h * 0.20f)
                    quadraticTo(w * 0.35f, aTop + h * 0.08f, w * 0.50f, aTop + h * 0.18f)
                    quadraticTo(w * 0.65f, aTop + h * 0.08f, aRight, aTop + h * 0.20f)
                }
                drawPath(path = curtainPath, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                drawCircle(color = color, radius = w * 0.045f, center = Offset(w * 0.35f, h * 0.82f))
                drawCircle(color = color, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.82f))
                drawCircle(color = color, radius = w * 0.045f, center = Offset(w * 0.65f, h * 0.82f))
            }
            4 -> { // Comic Book (open book with two curved pages meeting at center)
                val cx = w * 0.50f
                val bTop = h * 0.22f
                val bBottom = h * 0.78f
                val wingW = w * 0.33f
                val leftPage = Path().apply {
                    moveTo(cx, bBottom - h * 0.06f)
                    quadraticTo(cx - wingW * 0.5f, bBottom, cx - wingW, bBottom - h * 0.04f)
                    lineTo(cx - wingW, bTop)
                    quadraticTo(cx - wingW * 0.5f, bTop + h * 0.05f, cx, bTop - h * 0.02f)
                    close()
                }
                drawPath(path = leftPage, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                val rightPage = Path().apply {
                    moveTo(cx, bBottom - h * 0.06f)
                    quadraticTo(cx + wingW * 0.5f, bBottom, cx + wingW, bBottom - h * 0.04f)
                    lineTo(cx + wingW, bTop)
                    quadraticTo(cx + wingW * 0.5f, bTop + h * 0.05f, cx, bTop - h * 0.02f)
                    close()
                }
                drawPath(path = rightPage, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawLine(color = color, start = Offset(cx, bTop - h * 0.02f), end = Offset(cx, bBottom - h * 0.06f), strokeWidth = strokeW)
            }
            5 -> { // Screenplay Universe (central play circle with satellite orbit ring)
                val cx = w * 0.50f
                val cy = h * 0.50f
                drawCircle(color = color, radius = w * 0.26f, center = Offset(cx, cy), style = Stroke(width = strokeW))
                val playPath = Path().apply {
                    moveTo(cx - w * 0.08f, cy - h * 0.12f)
                    lineTo(cx + w * 0.12f, cy)
                    lineTo(cx - w * 0.08f, cy + h * 0.12f)
                    close()
                }
                drawPath(path = playPath, color = color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                rotate(-25f, pivot = Offset(cx, cy)) {
                    drawOval(
                        color = color.copy(alpha = 0.85f),
                        topLeft = Offset(cx - w * 0.46f, cy - h * 0.22f),
                        size = Size(w * 0.92f, h * 0.44f),
                        style = Stroke(width = strokeW * 0.9f)
                    )
                    drawCircle(color = color, radius = strokeW * 1.5f, center = Offset(cx + w * 0.46f, cy))
                }
            }
            else -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.2f, h * 0.2f),
                    size = Size(w * 0.6f, h * 0.6f),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                    style = Stroke(width = strokeW)
                )
            }
        }
    }
}

/**
 * Smoked glass card representing a recent project tile.
 * Features red border, crystalline transparency, and glowing neon bloom on hover/press.
 */
@Composable
private fun GlassProjectCard(
    project: ScriptProject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isHighlighted) 14.dp else 2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ProjGlowRadius"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.50f else 0.08f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ProjOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.80f else 0.18f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ProjMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.0.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "ProjStrokeWidth"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isHovered) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "ProjScale"
    )

    val dateStr = remember(project.lastModified) {
        try {
            val now = System.currentTimeMillis()
            val diff = now - project.lastModified
            if (diff < 24 * 60 * 60 * 1000L) {
                "Today, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(project.lastModified))
            } else if (diff < 48 * 60 * 60 * 1000L) {
                "Yesterday, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(project.lastModified))
            } else {
                SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(project.lastModified))
            }
        } catch (_: Throwable) {
            "Recent"
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(105.dp)
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 14.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 1.0f else 0.75f
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        if (isHighlighted) listOf(
                            Color(0x5535121E),
                            Color(0x68200A12)
                        ) else listOf(
                            Color(0x281A2333),
                            Color(0x3C0D121C)
                        )
                    } else {
                        if (isHighlighted) listOf(
                            Color(0xFFE11D48),
                            Color(0xFFBE123C)
                        ) else listOf(
                            Color(0xF5FFFFFF),
                            Color(0xEDF8FAFC)
                        )
                    }
                )
            )
            .border(
                BorderStroke(
                    width = strokeWidth,
                    color = if (isHighlighted) ScribeStarNeonRed else if (isDark) ScribeStarNeonRed.copy(alpha = 0.65f) else Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else (if (isHighlighted) Color.White else Color(0xFF0F172A)),
                    fontSize = 13.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Page count pill
                Surface(
                    color = if (isDark) Color(0x33FFFFFF) else Color(0x150F172A),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${project.pageCount} pp",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Draft tag pill
                Surface(
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = project.draftTag.take(12),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Last edited $dateStr",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 10.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Starter card displayed when no recent projects exist yet.
 * Features red border, high transparency, and red glowing bloom on hover/press.
 */
@Composable
private fun GlassStarterCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isHighlighted) 14.dp else 2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "StarterGlowRadius"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.50f else 0.08f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "StarterOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.80f else 0.18f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "StarterMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.0.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "StarterStrokeWidth"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isHovered) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "StarterScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(105.dp)
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 14.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 1.0f else 0.75f
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    if (isHighlighted) {
                        if (isDark) {
                            listOf(
                                Color(0x5535121E),
                                Color(0x68200A12)
                            )
                        } else {
                            listOf(
                                Color(0x28FF1E44),
                                Color(0x18FFE4E6)
                            )
                        }
                    } else {
                        if (isDark) {
                            listOf(
                                Color(0x281A2333),
                                Color(0x3C0D121C)
                            )
                        } else {
                            listOf(
                                Color(0xF0FFFFFF),
                                Color(0xE8F1F5F9)
                            )
                        }
                    }
                )
            )
            .border(
                BorderStroke(
                    width = strokeWidth,
                    color = if (isHighlighted) ScribeStarNeonRed else if (isDark) ScribeStarNeonRed.copy(alpha = 0.65f) else Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Start Your First Project",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else (if (isHighlighted) Color(0xFF991B1B) else Color(0xFF0F172A)),
                    fontSize = 13.5.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick a format above to begin drafting your story",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 11.sp
                )
            )
        }
    }
}
