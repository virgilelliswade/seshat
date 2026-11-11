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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
import com.seshat.writer.ui.theme.StudioMonoFontFamily
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.RawLightSurface
import com.seshat.writer.ui.theme.RawScreenplayTextMutedLight
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import androidx.compose.ui.graphics.Brush
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.StudioIconType
import com.seshat.writer.ui.theme.StudioMonoIcon
import com.seshat.writer.ui.theme.cleanLabel
import com.seshat.writer.ui.theme.iconType
import com.seshat.writer.ui.theme.parseStudioButtonLabel
import com.seshat.writer.ui.theme.neonBloom
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.floatingGlassIsland
import com.seshat.writer.ui.theme.opticalGlassBrush
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder
import com.seshat.writer.ui.theme.smokedGlassBrush
import com.seshat.writer.util.PlatformBackHandler
import com.seshat.writer.templates.ProjectTemplate
import com.seshat.writer.templates.TemplateRegistry
import com.seshat.writer.firstdraft.AmbientGenrePreviewCanvas
import com.seshat.writer.firstdraft.CampaignGenre
import com.seshat.writer.firstdraft.FirstDraftController
import com.seshat.writer.firstdraft.FirstDraftStage
import com.seshat.writer.firstdraft.FirstDraftState
import com.seshat.writer.firstdraft.GenreIgnitionTransitionOverlay
import kotlinx.coroutines.delay

private data class PendingCampaignLaunch(
    val format: ProjectFormat,
    val screenplayType: ScreenplayType?,
    val genre: CampaignGenre,
    val title: String,
    val isBlendedUniverse: Boolean = false
)

/**
 * Navigation steps within the Start Menu hierarchy.
 */
enum class StartMenuStep {
    SPLASH_DISSOLVE,   // Kept for backward compatibility
    TOP_LEVEL,         // Concept C Two-Column Hub
    FORMAT_SELECT,     // Level 1 fallback medium picker
    SCREENPLAY_BRANCH, // Level 2 (Screenplay Sub-menu): [Film] vs [Television]
    UNIVERSE_SELECT,   // Level 2 (Universe Sub-menu): [Cinematic] vs [Television] vs [Novel] vs [Blended]
    SCREENPLAY_TYPE,   // Level 3 (TV Only): [1-Hour Drama Show] vs [30-Min Comedy Show]
    TEMPLATE_SELECT,   // Templates
    WORKSPACE_VIEW,    // Starting Workspace: [Lore Doc & Story Bible] vs [Corkboard Outline] vs [Drafting Canvas]
    SPARK_HUB,         // Dedicated Writing Challenge Hub: [New Challenge] vs [Continue Existing Challenge]
    SPARK_NEW_SETUP    // New Writing Challenge Setup (Genre + Title + Launch)
}

@Composable
fun StartMenuScreen(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    onCloseMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var areButtonsVisible by remember { mutableStateOf(false) }
    var navigationStack by remember { mutableStateOf(listOf(StartMenuStep.TOP_LEVEL)) }
    val currentStep = navigationStack.lastOrNull() ?: StartMenuStep.TOP_LEVEL
    var selectedFormat by remember { mutableStateOf(ProjectFormat.SCREENPLAY) }
    var selectedScreenplayType by remember { mutableStateOf<ScreenplayType?>(ScreenplayType.FEATURE) }
    var selectedTemplate by remember { mutableStateOf<ProjectTemplate?>(null) }
    var projectTitleInput by remember { mutableStateOf("") }
    var showLibraryDialog by remember { mutableStateOf(false) }
    var isUniverseMode by remember { mutableStateOf(false) }
    var isBlendedUniverseMode by remember { mutableStateOf(false) }
    var isChallengeFlow by remember { mutableStateOf(false) }
    var pendingCampaignLaunch by remember { mutableStateOf<PendingCampaignLaunch?>(null) }

    LaunchedEffect(Unit) {
        if (!areButtonsVisible) {
            delay(5000L)
            areButtonsVisible = true
        }
    }

    val navigateTo: (StartMenuStep) -> Unit = { step ->
        if (navigationStack.lastOrNull() != step) {
            navigationStack = navigationStack + step
        }
    }

    val navigateBack: () -> Unit = {
        if (navigationStack.size > 1) {
            navigationStack = navigationStack.dropLast(1)
        }
    }

    PlatformBackHandler(
        enabled = navigationStack.size > 1 && !showLibraryDialog && !uiState.isSettingsDialogOpen && pendingCampaignLaunch == null
    ) {
        navigateBack()
    }

    val buttonsAlpha by animateFloatAsState(
        targetValue = if (areButtonsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "StartMenuButtonsAlpha"
    )

    val buttonsOffsetY by animateDpAsState(
        targetValue = if (areButtonsVisible) 0.dp else 20.dp,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "StartMenuButtonsOffsetY"
    )

    val splashBlur by animateDpAsState(
        targetValue = if (areButtonsVisible) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "StartMenuSplashBlur"
    )

    val scrimAlpha by animateFloatAsState(
        targetValue = if (areButtonsVisible) 0.52f else 0.0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "StartMenuScrimAlpha"
    )

    if (showLibraryDialog) {
        ScriptLibraryDialog(
            viewModel = viewModel,
            onDismiss = {
                showLibraryDialog = false
            },
            onProjectOpened = {
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
            currentStudioPalette = uiState.studioPalette,
            onStudioPaletteChanged = { newPalette ->
                viewModel.setStudioPalette(context, newPalette)
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
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    event.key == Key.Escape &&
                    navigationStack.size > 1 &&
                    !showLibraryDialog &&
                    !uiState.isSettingsDialogOpen &&
                    pendingCampaignLaunch == null
                ) {
                    navigateBack()
                    true
                } else {
                    false
                }
            }
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
        // FULLSCREEN CLEAN STUDIO BACKGROUND & SESHAT STATUE (DARK & LIGHT MARBLE)
        // =========================================================================
        Image(
            painter = painterResource(
                id = if (isDark) R.drawable.seshat_studio_splash else R.drawable.seshat_studio_light_splash
            ),
            contentDescription = "Goddess Seshat Studio - Mistress of the House of Books",
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .blur(splashBlur)
        )

        // Concept C Subtle Framing Scrim: Dark mode dims slightly for smoked-glass card contrast;
        // Light mode lets the Carrara white marble temple, statue, and crimson laser trim shine with zero whitewash.
        val activePalette = com.seshat.writer.ui.theme.ActiveStudioPaletteState.value
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            listOf(
                                activePalette.darkBg.copy(alpha = (scrimAlpha * 0.72f).coerceIn(0f, 0.45f)),
                                activePalette.darkAccent.copy(alpha = 0.05f),
                                activePalette.darkBg.copy(alpha = (scrimAlpha * 0.78f).coerceIn(0f, 0.50f))
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                activePalette.lightBg.copy(alpha = (scrimAlpha * 0.35f).coerceIn(0f, 0.22f)),
                                Color.Transparent,
                                activePalette.lightDesk.copy(alpha = (scrimAlpha * 0.45f).coerceIn(0f, 0.28f))
                            )
                        )
                    }
                )
        )

        // =========================================================================
        // FOREGROUND CONTENT: TOP BAR, LARGE TITLE & FLYING BUTTONS
        // =========================================================================
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Settings button and return-to-script button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .graphicsLayer {
                        alpha = buttonsAlpha
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Centrally available Studio Settings Button (contains Light/Dark mode & Color Palettes)
                    StartMenuTopBarButton(
                        text = "⚙️ Settings",
                        onClick = { viewModel.openSettingsDialog() }
                    )
                    StartMenuTopBarButton(
                        text = "✨ What's New",
                        onClick = { viewModel.openWhatsNewDialog() }
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
                    .graphicsLayer {
                        alpha = buttonsAlpha
                        translationY = buttonsOffsetY.toPx()
                    }
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
                        .floatingGlassIsland(
                            isDark = isDark,
                            elevation = 12.dp,
                            accentColor = ScribeStarNeonRed,
                            isHighlighted = true
                        )
                ) {
                    Text(
                        text = "W R I T I N G   S T U D I O",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp,
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
                            letterSpacing = 0.2.sp,
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
                                        onSelectScreenplayBranch = {
                                            isUniverseMode = false
                                            isBlendedUniverseMode = false
                                            isChallengeFlow = false
                                            projectTitleInput = ""
                                            navigateTo(StartMenuStep.SCREENPLAY_BRANCH)
                                        },
                                        onSelectFormat = { format, type ->
                                            selectedFormat = format
                                            selectedScreenplayType = type
                                            selectedTemplate = TemplateRegistry.getBlankTemplate(format, type)
                                            isUniverseMode = false
                                            isBlendedUniverseMode = false
                                            isChallengeFlow = false
                                            projectTitleInput = ""
                                            navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                        },
                                        onSelectUniverse = {
                                            isUniverseMode = true
                                            isBlendedUniverseMode = false
                                            isChallengeFlow = false
                                            projectTitleInput = ""
                                            navigateTo(StartMenuStep.UNIVERSE_SELECT)
                                        },
                                        onSelectSparkScreenplayBranch = {
                                            isUniverseMode = false
                                            isBlendedUniverseMode = false
                                            isChallengeFlow = true
                                            projectTitleInput = ""
                                            navigateTo(StartMenuStep.SCREENPLAY_BRANCH)
                                        },
                                        onSelectSparkFormat = { format, type, isUniverse ->
                                            selectedFormat = format
                                            selectedScreenplayType = type
                                            isUniverseMode = isUniverse
                                            isBlendedUniverseMode = false
                                            isChallengeFlow = true
                                            projectTitleInput = ""
                                            navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                        },
                                        onSelectSparkUniverse = {
                                            isUniverseMode = true
                                            isBlendedUniverseMode = false
                                            isChallengeFlow = true
                                            projectTitleInput = ""
                                            navigateTo(StartMenuStep.UNIVERSE_SELECT)
                                        },
                                        onNewProject = {
                                            projectTitleInput = ""
                                            isUniverseMode = false
                                            isBlendedUniverseMode = false
                                            isChallengeFlow = false
                                            navigateTo(StartMenuStep.FORMAT_SELECT)
                                        },
                                        onOpenSparkMode = {
                                            projectTitleInput = ""
                                            isChallengeFlow = true
                                            navigateTo(StartMenuStep.SPARK_HUB)
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
                                    onSelectScreenplayBranch = {
                                        isUniverseMode = false
                                        isBlendedUniverseMode = false
                                        isChallengeFlow = false
                                        navigateTo(StartMenuStep.SCREENPLAY_BRANCH)
                                    },
                                    onSelectNovel = {
                                        selectedFormat = ProjectFormat.NOVEL
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_NOVEL
                                        isUniverseMode = false
                                        isBlendedUniverseMode = false
                                        isChallengeFlow = false
                                        navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                    },
                                    onSelectComicBook = {
                                        selectedFormat = ProjectFormat.COMIC_BOOK
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_COMIC_BOOK
                                        isUniverseMode = false
                                        isBlendedUniverseMode = false
                                        isChallengeFlow = false
                                        navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                    },
                                    onSelectStagePlay = {
                                        selectedFormat = ProjectFormat.STAGE_PLAY
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_STAGE_PLAY
                                        isUniverseMode = false
                                        isBlendedUniverseMode = false
                                        isChallengeFlow = false
                                        navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                    },
                                    onSelectUniverse = {
                                        isUniverseMode = true
                                        isBlendedUniverseMode = false
                                        isChallengeFlow = false
                                        navigateTo(StartMenuStep.UNIVERSE_SELECT)
                                    },
                                    onBack = navigateBack
                                )
                            }

                            StartMenuStep.SCREENPLAY_BRANCH -> {
                                ScreenplayBranchContent(
                                    isChallengeMode = isChallengeFlow,
                                    onSelectFilm = {
                                        selectedFormat = ProjectFormat.SCREENPLAY
                                        selectedScreenplayType = ScreenplayType.FEATURE
                                        selectedTemplate = TemplateRegistry.BLANK_FEATURE
                                        isUniverseMode = false
                                        isBlendedUniverseMode = false
                                        if (isChallengeFlow) {
                                            navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                        } else {
                                            navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                        }
                                    },
                                    onSelectTelevision = {
                                        selectedFormat = ProjectFormat.TV_SERIES
                                        selectedTemplate = TemplateRegistry.BLANK_TV
                                        isUniverseMode = false
                                        isBlendedUniverseMode = false
                                        navigateTo(StartMenuStep.SCREENPLAY_TYPE)
                                    },
                                    onBack = navigateBack
                                )
                            }

                            StartMenuStep.UNIVERSE_SELECT -> {
                                UniverseSelectContent(
                                    isChallengeMode = isChallengeFlow,
                                    onSelectCinematicFeatures = {
                                        selectedFormat = ProjectFormat.SCREENPLAY
                                        selectedScreenplayType = ScreenplayType.FEATURE
                                        selectedTemplate = TemplateRegistry.BLANK_FEATURE
                                        isUniverseMode = true
                                        isBlendedUniverseMode = false
                                        if (isChallengeFlow) {
                                            navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                        } else {
                                            navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                        }
                                    },
                                    onSelectTelevisionUniverse = {
                                        selectedFormat = ProjectFormat.TV_SERIES
                                        selectedScreenplayType = ScreenplayType.TV_HOUR_DRAMA
                                        selectedTemplate = TemplateRegistry.BLANK_TV
                                        isUniverseMode = true
                                        isBlendedUniverseMode = false
                                        navigateTo(StartMenuStep.SCREENPLAY_TYPE)
                                    },
                                    onSelectNovelUniverse = {
                                        selectedFormat = ProjectFormat.NOVEL
                                        selectedScreenplayType = null
                                        selectedTemplate = TemplateRegistry.BLANK_NOVEL
                                        isUniverseMode = true
                                        isBlendedUniverseMode = false
                                        if (isChallengeFlow) {
                                            navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                        } else {
                                            navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                        }
                                    },
                                    onSelectBlendedUniverse = {
                                        selectedFormat = ProjectFormat.SCREENPLAY
                                        selectedScreenplayType = ScreenplayType.FEATURE
                                        selectedTemplate = TemplateRegistry.BLANK_FEATURE
                                        isUniverseMode = true
                                        isBlendedUniverseMode = true
                                        if (isChallengeFlow) {
                                            navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                        } else {
                                            navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                        }
                                    },
                                    onBack = navigateBack
                                )
                            }

                            StartMenuStep.SCREENPLAY_TYPE -> {
                                StartMenuLayout(
                                    headerTitle = if (isUniverseMode) "TELEVISION UNIVERSE FORMAT" else "TELEVISION SCRIPT FORMAT",
                                    headerSubtitle = "Select your show pacing, episodic deck structure, and act architecture",
                                    showBackButton = true,
                                    onBack = navigateBack
                                ) {
                                    StartMenuBigButton(
                                        badgeText = "📺",
                                        title = "1-HOUR DRAMA SHOW (60m)",
                                        subtitle = "Prestige, cable & serialized drama (~50–60 min) • 5 Acts + Teaser, episodic deck & seasonal arc",
                                        badgeColor = Color(0xFF3B82F6),
                                        onClick = {
                                            selectedFormat = ProjectFormat.TV_SERIES
                                            selectedScreenplayType = ScreenplayType.TV_HOUR_DRAMA
                                            selectedTemplate = TemplateRegistry.BLANK_TV
                                            if (isChallengeFlow) {
                                                navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                            } else {
                                                navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    StartMenuBigButton(
                                        badgeText = "🛋️",
                                        title = "30-MIN COMEDY SHOW (30m)",
                                        subtitle = "Multi-cam or single-cam sitcom (~25–35 min) • 2–3 Acts + Cold Open & Tag, episodic deck",
                                        badgeColor = Color(0xFFEC4899),
                                        onClick = {
                                            selectedFormat = ProjectFormat.TV_SERIES
                                            selectedScreenplayType = ScreenplayType.TV_HALF_HOUR_COMEDY
                                            selectedTemplate = TemplateRegistry.BLANK_TV
                                            if (isChallengeFlow) {
                                                navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                            } else {
                                                navigateTo(StartMenuStep.WORKSPACE_VIEW)
                                            }
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
                                    onBack = navigateBack
                                ) {
                                    templates.forEach { tmpl ->
                                        StartMenuBigButton(
                                            badgeText = tmpl.badgeIcon,
                                            title = tmpl.name.uppercase(),
                                            subtitle = if (tmpl.isBlank) "${tmpl.subtitle} • Clean blank canvas with zero sample text" else "${tmpl.subtitle} • ${tmpl.description}",
                                            badgeColor = tmpl.badgeColor,
                                            onClick = {
                                                selectedTemplate = tmpl
                                                navigateTo(StartMenuStep.WORKSPACE_VIEW)
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
                                    isBlendedUniverseMode = isBlendedUniverseMode,
                                    onBlendedStarterChange = { fmt, sType ->
                                        selectedFormat = fmt
                                        selectedScreenplayType = sType
                                    },
                                    projectTitleInput = projectTitleInput,
                                    onTitleChange = { projectTitleInput = it },
                                    isDark = isDark,
                                    onSelectView = { view ->
                                        if (isBlendedUniverseMode) {
                                            viewModel.startNewBlendedUniverseProject(
                                                context = context,
                                                universeTitle = projectTitleInput,
                                                starterFormat = selectedFormat,
                                                starterScreenplayType = selectedScreenplayType ?: ScreenplayType.FEATURE,
                                                initialWorkspaceView = view,
                                                enableSparkMode = false
                                            )
                                        } else {
                                            viewModel.startNewProject(
                                                context = context,
                                                format = selectedFormat,
                                                screenplayType = selectedScreenplayType,
                                                initialWorkspaceView = view,
                                                customTitle = projectTitleInput,
                                                template = selectedTemplate,
                                                enableSparkMode = false
                                            )
                                        }
                                        onCloseMenu()
                                    },
                                    onBack = navigateBack
                                )
                            }

                            StartMenuStep.SPARK_HUB -> {
                                val savedSparkProjects = remember(context, uiState.title, uiState.firstDraftState) {
                                    FirstDraftController.listSparkProjects(context)
                                }
                                SparkModeHubContent(
                                    activeSparkState = if (uiState.firstDraftState.isFirstDraftMode) uiState.firstDraftState else null,
                                    activeProjectTitle = uiState.title,
                                    savedSparkProjects = savedSparkProjects,
                                    onStartNewSpark = {
                                        projectTitleInput = ""
                                        isBlendedUniverseMode = false
                                        navigateTo(StartMenuStep.SPARK_NEW_SETUP)
                                    },
                                    onContinueActiveSpark = {
                                        onCloseMenu()
                                    },
                                    onResumeSparkProject = { sparkState ->
                                        viewModel.resumeSparkProject(context, sparkState)
                                        onCloseMenu()
                                    },
                                    onBack = navigateBack
                                )
                            }

                            StartMenuStep.SPARK_NEW_SETUP -> {
                                SparkNewSetupContent(
                                    selectedFormat = selectedFormat,
                                    selectedScreenplayType = selectedScreenplayType,
                                    isUniverseMode = isUniverseMode,
                                    isBlendedUniverseMode = isBlendedUniverseMode,
                                    onBlendedStarterChange = { fmt, sType ->
                                        selectedFormat = fmt
                                        selectedScreenplayType = sType
                                    },
                                    projectTitleInput = projectTitleInput,
                                    onTitleChange = { projectTitleInput = it },
                                    isDark = isDark,
                                    onLaunchSpark = { format, sType, genre ->
                                        pendingCampaignLaunch = PendingCampaignLaunch(
                                            format = format,
                                            screenplayType = sType,
                                            genre = genre,
                                            title = projectTitleInput,
                                            isBlendedUniverse = isBlendedUniverseMode
                                        )
                                    },
                                    onBack = navigateBack
                                )
                            }
                        }
                    }
                }
            }

            // Full-Screen 2.2s Genre Campaign Ignition Transition Overlay
            val activeLaunch = pendingCampaignLaunch
            if (activeLaunch != null) {
                GenreIgnitionTransitionOverlay(
                    genre = activeLaunch.genre,
                    projectTitle = activeLaunch.title,
                    onFinished = {
                        pendingCampaignLaunch = null
                        if (activeLaunch.isBlendedUniverse) {
                            viewModel.startNewBlendedUniverseProject(
                                context = context,
                                universeTitle = activeLaunch.title,
                                starterFormat = activeLaunch.format,
                                starterScreenplayType = activeLaunch.screenplayType ?: ScreenplayType.FEATURE,
                                initialWorkspaceView = InitialWorkspaceView.EDITOR,
                                enableSparkMode = true,
                                campaignGenre = activeLaunch.genre
                            )
                        } else {
                            viewModel.startNewProject(
                                context = context,
                                format = activeLaunch.format,
                                screenplayType = activeLaunch.screenplayType,
                                initialWorkspaceView = InitialWorkspaceView.EDITOR,
                                customTitle = activeLaunch.title,
                                template = TemplateRegistry.getBlankTemplate(activeLaunch.format, activeLaunch.screenplayType),
                                enableSparkMode = true,
                                campaignGenre = activeLaunch.genre
                            )
                        }
                        onCloseMenu()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FormatSelectContent(
    onSelectScreenplayBranch: () -> Unit,
    onSelectNovel: () -> Unit,
    onSelectComicBook: () -> Unit,
    onSelectStagePlay: () -> Unit,
    onSelectUniverse: () -> Unit,
    onBack: () -> Unit
) {
    StartMenuLayout(
        headerTitle = "CHOOSE MEDIUM",
        headerSubtitle = "What format are you creating today?",
        showBackButton = true,
        onBack = onBack
    ) {
        // 1. SCREENPLAY (Branches to Film vs TV)
        StartMenuBigButton(
            badgeText = "🎬",
            title = "SCREENPLAY",
            subtitle = "Feature Film or Television Script • Industry standard Courier 12pt",
            badgeColor = ElectrumGold,
            onClick = onSelectScreenplayBranch
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. NOVEL
        StartMenuBigButton(
            badgeText = "📖",
            title = "NOVEL",
            subtitle = "Prose book manuscript, chapter binder, and long-form narrative fiction",
            badgeColor = Color(0xFF8B5CF6),
            onClick = onSelectNovel
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. COMIC BOOK
        StartMenuBigButton(
            badgeText = "💥",
            title = "COMIC BOOK",
            subtitle = "Standard comic page & panel scripting • Page headings, panel descriptions, art notes & SFX",
            badgeColor = ScribeStarNeonRed,
            onClick = onSelectComicBook
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. STAGE PLAY
        StartMenuBigButton(
            badgeText = "🎭",
            title = "STAGE PLAY",
            subtitle = "Proscenium, black box & musical theatre • Acts, scenes, stage directions & rapid dialogue",
            badgeColor = ElectrumGold,
            onClick = onSelectStagePlay
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. CREATE A UNIVERSE
        StartMenuBigButton(
            badgeText = "🪐",
            title = "CREATE A UNIVERSE",
            subtitle = "Build a Cinematic, Television, Novel, or Blended multi-medium story universe with a shared Lore Bible",
            badgeColor = ScribeStarNeonRed,
            onClick = onSelectUniverse
        )
    }
}

@Composable
private fun ScreenplayBranchContent(
    isChallengeMode: Boolean,
    onSelectFilm: () -> Unit,
    onSelectTelevision: () -> Unit,
    onBack: () -> Unit
) {
    StartMenuLayout(
        headerTitle = if (isChallengeMode) "🏆 SCREENPLAY CHALLENGE" else "🎬 SCREENPLAY",
        headerSubtitle = "Choose whether you are writing a Feature Film or Television Script",
        showBackButton = true,
        onBack = onBack
    ) {
        StartMenuBigButton(
            badgeText = "🎬",
            title = if (isChallengeMode) "FILM CHALLENGE (90P FEATURE)" else "FILM (FEATURE SCREENPLAY)",
            subtitle = "Standalone motion picture script (~90–120 pages) • Industry standard Courier 12pt",
            badgeColor = ElectrumGold,
            onClick = onSelectFilm
        )

        Spacer(modifier = Modifier.height(14.dp))

        StartMenuBigButton(
            badgeText = "📺",
            title = if (isChallengeMode) "TV SCRIPT CHALLENGE (PILOT)" else "TELEVISION (TV SCRIPT)",
            subtitle = "1-Hour Drama (60m) or 30-Min Comedy (30m) • Episodic structure, cold open & teaser acts",
            badgeColor = Color(0xFF3B82F6),
            onClick = onSelectTelevision
        )
    }
}

@Composable
private fun UniverseSelectContent(
    isChallengeMode: Boolean,
    onSelectCinematicFeatures: () -> Unit,
    onSelectTelevisionUniverse: () -> Unit,
    onSelectNovelUniverse: () -> Unit,
    onSelectBlendedUniverse: () -> Unit,
    onBack: () -> Unit
) {
    StartMenuLayout(
        headerTitle = if (isChallengeMode) "🏆 UNIVERSE CHALLENGE" else "🪐 CREATE A UNIVERSE",
        headerSubtitle = "Choose the interconnected scope of your story world",
        showBackButton = true,
        onBack = onBack
    ) {
        StartMenuBigButton(
            badgeText = "🎬",
            title = if (isChallengeMode) "CINEMATIC UNIVERSE CHALLENGE" else "CINEMATIC UNIVERSE",
            subtitle = "Multi-film franchise, trilogy, or connected features • Shared Master Lore Bible & continuity",
            badgeColor = ElectrumGold,
            onClick = onSelectCinematicFeatures
        )

        Spacer(modifier = Modifier.height(14.dp))

        StartMenuBigButton(
            badgeText = "📺",
            title = if (isChallengeMode) "TELEVISION UNIVERSE CHALLENGE" else "TELEVISION UNIVERSE",
            subtitle = "Serialized multi-season TV show • Episode binders (101, 102...), writers room & Series Lore Bible",
            badgeColor = Color(0xFF3B82F6),
            onClick = onSelectTelevisionUniverse
        )

        Spacer(modifier = Modifier.height(14.dp))

        StartMenuBigButton(
            badgeText = "📚",
            title = if (isChallengeMode) "NOVEL UNIVERSE CHALLENGE" else "NOVEL UNIVERSE",
            subtitle = "Multi-book saga, trilogy, or interconnected prose world • Master Lore Bible & chapter binders",
            badgeColor = Color(0xFF8B5CF6),
            onClick = onSelectNovelUniverse
        )

        Spacer(modifier = Modifier.height(14.dp))

        StartMenuBigButton(
            badgeText = "🪐",
            title = if (isChallengeMode) "BLENDED UNIVERSE CHALLENGE" else "BLENDED UNIVERSE",
            subtitle = "Combine Films, TV Series, Novels, Comic Books & Stage Plays inside one universe • Add any assignment medium anytime",
            badgeColor = ScribeStarNeonRed,
            onClick = onSelectBlendedUniverse
        )
    }
}

@Composable
private fun BlendedStarterFormatPicker(
    selectedFormat: ProjectFormat,
    selectedScreenplayType: ScreenplayType?,
    isDark: Boolean,
    onSelect: (ProjectFormat, ScreenplayType?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .floatingGlassIsland(
                isDark = isDark,
                elevation = 10.dp,
                accentColor = ElectrumGold,
                isHighlighted = true
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🪐 FIRST UNIVERSE ASSIGNMENT FORMAT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MainAppFontFamily,
                color = ElectrumGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        )
        Text(
            text = "Pick what medium Assignment #1 starts in (you can add Film, TV, Novel, Comic, or Stage assignments later from the Universe Binder):",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = MainAppFontFamily,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                fontSize = 11.sp
            )
        )
        val options = listOf(
            Triple("🎬 Film", ProjectFormat.SCREENPLAY, ScreenplayType.FEATURE),
            Triple("📺 TV", ProjectFormat.TV_SERIES, ScreenplayType.TV_HOUR_DRAMA),
            Triple("📖 Novel", ProjectFormat.NOVEL, null),
            Triple("💥 Comic", ProjectFormat.COMIC_BOOK, null),
            Triple("🎭 Stage", ProjectFormat.STAGE_PLAY, null)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { (label, fmt, sType) ->
                val isSelected = selectedFormat == fmt && (fmt != ProjectFormat.SCREENPLAY || selectedScreenplayType == sType)
                Surface(
                    color = if (isSelected) ScribeStarNeonRed.copy(alpha = 0.22f) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ScribeStarNeonRed else ObsidianSurfaceVariant
                    ),
                    modifier = Modifier.clickable { onSelect(fmt, sType) }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected || isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceViewContent(
    selectedFormat: ProjectFormat,
    selectedScreenplayType: ScreenplayType?,
    isUniverseMode: Boolean,
    isBlendedUniverseMode: Boolean = false,
    onBlendedStarterChange: (ProjectFormat, ScreenplayType?) -> Unit = { _, _ -> },
    projectTitleInput: String,
    onTitleChange: (String) -> Unit,
    isDark: Boolean,
    onSelectView: (InitialWorkspaceView) -> Unit,
    onBack: () -> Unit
) {
    val formatLabel = when {
        isBlendedUniverseMode -> "BLENDED UNIVERSE (MULTI-MEDIUM)"
        isUniverseMode && selectedFormat == ProjectFormat.SCREENPLAY -> "CINEMATIC UNIVERSE"
        isUniverseMode && selectedFormat == ProjectFormat.TV_SERIES -> "TELEVISION UNIVERSE"
        isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "NOVEL UNIVERSE"
        selectedFormat == ProjectFormat.NOVEL -> "NOVEL"
        selectedFormat == ProjectFormat.STAGE_PLAY -> "STAGE PLAY"
        selectedFormat == ProjectFormat.COMIC_BOOK -> "COMIC BOOK"
        selectedFormat == ProjectFormat.TV_SERIES -> when (selectedScreenplayType) {
            ScreenplayType.TV_HALF_HOUR_COMEDY -> "TV COMEDY SHOW"
            else -> "TV DRAMA SHOW"
        }
        else -> "FILM SCREENPLAY"
    }

    StartMenuLayout(
        headerTitle = "STARTING WORKSPACE",
        headerSubtitle = "$formatLabel • Where would you like to begin?",
        showBackButton = true,
        onBack = onBack
    ) {
        if (isBlendedUniverseMode) {
            BlendedStarterFormatPicker(
                selectedFormat = selectedFormat,
                selectedScreenplayType = selectedScreenplayType,
                isDark = isDark,
                onSelect = onBlendedStarterChange
            )
        }

        // Optional Project Title Input - Concept C Floating Glass Island with Drop Shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .floatingGlassIsland(
                    isDark = isDark,
                    elevation = 12.dp,
                    accentColor = ScribeStarNeonRed,
                    isHighlighted = projectTitleInput.isNotBlank()
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val titleLabel = when {
                    isBlendedUniverseMode -> "BLENDED UNIVERSE TITLE (OPTIONAL)"
                    isUniverseMode && selectedFormat == ProjectFormat.TV_SERIES -> "TELEVISION UNIVERSE TITLE (OPTIONAL)"
                    isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "NOVEL UNIVERSE TITLE (OPTIONAL)"
                    isUniverseMode -> "CINEMATIC UNIVERSE TITLE (OPTIONAL)"
                    selectedFormat == ProjectFormat.NOVEL -> "NOVEL TITLE (OPTIONAL)"
                    else -> "PROJECT TITLE (OPTIONAL)"
                }
                Text(
                    text = titleLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = ScribeStarNeonRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
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

@Composable
private fun SparkModeHubContent(
    activeSparkState: FirstDraftState?,
    activeProjectTitle: String,
    savedSparkProjects: List<FirstDraftState>,
    onStartNewSpark: () -> Unit,
    onContinueActiveSpark: () -> Unit,
    onResumeSparkProject: (FirstDraftState) -> Unit,
    onBack: () -> Unit
) {
    StartMenuLayout(
        headerTitle = "🏆 WRITING CHALLENGE HUB",
        headerSubtitle = "Gamified Anti-Procrastination Engine • Timed Stage Quests, 48h Quotas, AI Bouncer & XP Loot Drops",
        showBackButton = true,
        onBack = onBack
    ) {
        // 1. START NEW WRITING CHALLENGE
        StartMenuBigButton(
            badgeText = "🏆",
            title = "START NEW WRITING CHALLENGE",
            subtitle = "Pick a Genre (Survival, Protocol, Quest, Heist, Overdrive, Roast, Virtuoso, or Detective) • Beat 4 timed stages to unlock Studio",
            badgeColor = ScribeStarNeonRed,
            onClick = onStartNewSpark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. CONTINUE EXISTING WRITING CHALLENGE
        if (activeSparkState != null) {
            StartMenuBigButton(
                badgeText = activeSparkState.campaignGenre.icon,
                title = "CONTINUE ACTIVE CHALLENGE: ${activeProjectTitle.uppercase(Locale.US)}",
                subtitle = "${activeSparkState.campaignGenre.modeTitle} • Stage: ${activeSparkState.campaignGenre.stageTitleFor(activeSparkState.stage)} • ⚡ ${activeSparkState.xpBalance} ${activeSparkState.campaignGenre.currencyName} • 🔥 ${activeSparkState.activeStreakDays}d Streak",
                badgeColor = ElectrumGold,
                onClick = onContinueActiveSpark
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (savedSparkProjects.isNotEmpty()) {
            Text(
                text = "SAVED WRITING CHALLENGES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = ElectrumGold,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    fontSize = 11.5.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp)
            )

            savedSparkProjects.forEach { spark ->
                val formatBadge = when {
                    spark.projectFormat == ProjectFormat.NOVEL -> "📖 NOVEL"
                    spark.projectFormat == ProjectFormat.STAGE_PLAY -> "🎭 STAGE"
                    spark.projectFormat == ProjectFormat.COMIC_BOOK -> "💥 COMIC"
                    spark.projectFormat == ProjectFormat.TV_SERIES && spark.screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY -> "🛋️ TV 30M"
                    spark.projectFormat == ProjectFormat.TV_SERIES -> "📺 TV 60M"
                    else -> "🎬 FEATURE"
                }
                val stageBadge = spark.campaignGenre.stageTitleFor(spark.stage)
                val loglinePreview = spark.sparkLogline.take(90).let {
                    if (spark.sparkLogline.length > 90) "$it…" else it
                }
                val sub = buildString {
                    append("${spark.campaignGenre.modeTitle} ($formatBadge) • Stage: $stageBadge • ⚡ ${spark.xpBalance} ${spark.campaignGenre.currencyName} • 🔥 ${spark.activeStreakDays}d Streak")
                    if (loglinePreview.isNotBlank()) {
                        append("\n\"$loglinePreview\"")
                    }
                }
                StartMenuBigButton(
                    badgeText = spark.campaignGenre.icon,
                    title = spark.projectTitle.ifBlank { "UNTITLED CHALLENGE" }.uppercase(Locale.US),
                    subtitle = sub,
                    badgeColor = Color(0xFF10B981),
                    onClick = { onResumeSparkProject(spark) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (activeSparkState == null) {
            StartMenuBigButton(
                badgeText = "🏆",
                title = "NO SAVED CHALLENGES YET",
                subtitle = "Click 'Start New Writing Challenge' above or pick any Challenge on the Splash screen to begin your first run!",
                badgeColor = ElectrumGold,
                onClick = onStartNewSpark
            )
        }
    }
}

@Composable
private fun SparkNewSetupContent(
    selectedFormat: ProjectFormat,
    selectedScreenplayType: ScreenplayType?,
    isUniverseMode: Boolean,
    isBlendedUniverseMode: Boolean = false,
    onBlendedStarterChange: (ProjectFormat, ScreenplayType?) -> Unit = { _, _ -> },
    projectTitleInput: String,
    onTitleChange: (String) -> Unit,
    isDark: Boolean,
    onLaunchSpark: (ProjectFormat, ScreenplayType?, CampaignGenre) -> Unit,
    onBack: () -> Unit
) {
    var selectedGenre by remember { mutableStateOf(CampaignGenre.HORROR) }

    val mediumName = when {
        isBlendedUniverseMode -> "BLENDED UNIVERSE CHALLENGE"
        isUniverseMode && selectedFormat == ProjectFormat.NOVEL -> "NOVEL UNIVERSE CHALLENGE"
        isUniverseMode && selectedFormat == ProjectFormat.TV_SERIES -> "TELEVISION UNIVERSE CHALLENGE"
        isUniverseMode -> "CINEMATIC UNIVERSE CHALLENGE"
        selectedFormat == ProjectFormat.NOVEL -> "NOVEL CHALLENGE (${selectedGenre.novelTargetLabel.uppercase(Locale.US)} TARGET)"
        selectedFormat == ProjectFormat.STAGE_PLAY -> "STAGE PLAY CHALLENGE"
        selectedFormat == ProjectFormat.COMIC_BOOK -> "COMIC BOOK CHALLENGE"
        selectedFormat == ProjectFormat.TV_SERIES -> "TV SCRIPT CHALLENGE"
        else -> "FILM SCREENPLAY CHALLENGE (90 PAGES / ~18,000W)"
    }

    StartMenuLayout(
        headerTitle = "🏆 CHOOSE WRITING CHALLENGE GENRE",
        headerSubtitle = "$mediumName • Your genre transforms the game rules, stages, currency & AI Bouncer",
        showBackButton = true,
        onBack = onBack
    ) {
        if (isBlendedUniverseMode) {
            BlendedStarterFormatPicker(
                selectedFormat = selectedFormat,
                selectedScreenplayType = selectedScreenplayType,
                isDark = isDark,
                onSelect = onBlendedStarterChange
            )
        }
        // 1. 8-GENRE CAMPAIGN GRID (4 rows of 2 cards)
        val genres = CampaignGenre.entries
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.chunked(2).forEach { rowGenres ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowGenres.forEach { genre ->
                        val isSelected = selectedGenre == genre
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = if (isSelected) 12.dp else 6.dp,
                                    shape = RoundedCornerShape(0.dp),
                                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.72f else 0.18f),
                                    spotColor = if (isSelected) ScribeStarNeonRed else Color.Black.copy(alpha = if (isDark) 0.80f else 0.24f)
                                )
                                .neonBloom(
                                    color = if (isSelected) ScribeStarNeonRed else (if (isDark) Color.White else Color(0xFF64748B)),
                                    cornerRadius = 0.dp,
                                    glowRadius = if (isSelected) 12.dp else 5.dp,
                                    strokeWidth = if (isSelected) 1.6.dp else 1.dp,
                                    outerAlpha = if (isSelected) 0.30f else 0.08f,
                                    midAlpha = if (isSelected) 0.55f else 0.16f,
                                    coreAlpha = if (isSelected) 0.90f else 0.35f
                                )
                                .clip(RoundedCornerShape(0.dp))
                                .background(
                                    if (isSelected) {
                                        opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = ScribeStarNeonRed)
                                    } else {
                                        opticalGlassBrush(glassOpacity = if (isDark) 0.82f else 0.90f)
                                    }
                                )
                                .border(
                                    prismaticGlassBorder(
                                        width = if (isSelected) 1.6.dp else 1.dp,
                                        isLit = isSelected,
                                        accentColor = ScribeStarNeonRed,
                                        isDark = isDark
                                    ),
                                    RoundedCornerShape(0.dp)
                                )
                                .clickable { selectedGenre = genre }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = genre.icon,
                                    fontSize = 18.sp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = genre.genreLabel,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = MainAppFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected || isDark) Color.White else Color(0xFF0F172A),
                                            fontSize = 12.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = genre.shortModeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = MainAppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White.copy(alpha = 0.90f) else (if (isDark) ElectrumGold else Color(0xFF475569)),
                                            fontSize = 10.sp,
                                            letterSpacing = 0.4.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. LIVE CAMPAIGN BRIEFING CARD (with Elevated Shadow & Ambient Procedural Genre Transition Backdrop)
        Surface(
            color = Color(0xFF0A0F1A).copy(alpha = 0.94f),
            shape = RoundedCornerShape(0.dp),
            border = prismaticGlassBorder(width = 1.2.dp, isLit = true, isDark = true),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(0.dp),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.75f else 0.25f),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.85f else 0.32f)
                )
                .neonBloom(
                    color = ScribeStarNeonRed,
                    cornerRadius = 0.dp,
                    glowRadius = 10.dp,
                    strokeWidth = 1.2.dp,
                    outerAlpha = 0.16f,
                    midAlpha = 0.32f
                )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AmbientGenrePreviewCanvas(
                    genre = selectedGenre,
                    modifier = Modifier.matchParentSize()
                )
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedGenre.campaignBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = ScribeStarNeonRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = "${selectedGenre.icon} ${selectedGenre.modeTitle}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MainAppFontFamily,
                                color = ElectrumGold,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                    Surface(
                        color = ElectrumGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = "⚡ ${selectedGenre.currencyName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = ElectrumGold,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 4 Stages Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(0.dp))
                        .background(Color(0xFF111827))
                        .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(0.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "4 TIMED CAMPAIGN STAGES:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Text(
                        text = "1️⃣ ${selectedGenre.stage1Title} (30m)  →  2️⃣ ${selectedGenre.stage2Title} (48h)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFFF1F5F9),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                    Text(
                        text = "3️⃣ ${selectedGenre.stage3Title} (24h)  →  4️⃣ ${selectedGenre.stage4Title} (Forward-Only Lock)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFFF1F5F9),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                }

                // 48h Penalty & 60s AI Bouncer Preview
                Text(
                    text = "💀 48h Idle Penalty: ${selectedGenre.decayPenaltyTitle} — ${selectedGenre.decayPenaltyDescription}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "🤖 60s Idle AI Bouncer: ${selectedGenre.bouncerQuotePreview}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                )
                }
            }
        }

        // 3. OPTIONAL PROJECT TITLE INPUT - Concept C Floating Glass Island with Drop Shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .floatingGlassIsland(
                    isDark = isDark,
                    elevation = 12.dp,
                    accentColor = ScribeStarNeonRed,
                    isHighlighted = projectTitleInput.isNotBlank()
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "CAMPAIGN PROJECT TITLE (OPTIONAL)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = ScribeStarNeonRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box {
                    if (projectTitleInput.isEmpty()) {
                        Text(
                            text = "e.g. Velocity Run / The Zero Hour",
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
                        cursorBrush = SolidColor(ElectrumGold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. LAUNCH BUTTON(S)
        val primarySub = when (selectedFormat) {
            ProjectFormat.NOVEL -> "${selectedGenre.modeTitle} • ${selectedGenre.novelTargetLabel} Target • Stage 1: ${selectedGenre.stage1Title} (30m) → ${selectedGenre.stage2Title} (96h) → ${selectedGenre.stage3Title} (48h) → ${selectedGenre.stage4Title}"
            ProjectFormat.STAGE_PLAY -> "${selectedGenre.modeTitle} • Stage Play • Stage 1: ${selectedGenre.stage1Title} (30m) → ${selectedGenre.stage2Title} (48h) → ${selectedGenre.stage3Title} (24h) → ${selectedGenre.stage4Title}"
            ProjectFormat.COMIC_BOOK -> "${selectedGenre.modeTitle} • Comic Issue • Stage 1: ${selectedGenre.stage1Title} (30m) → ${selectedGenre.stage2Title} (48h) → ${selectedGenre.stage3Title} (24h) → ${selectedGenre.stage4Title}"
            ProjectFormat.TV_SERIES -> "${selectedGenre.modeTitle} • ${if (selectedScreenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY) "30m Comedy Pilot" else "60m Drama Pilot"} • Stage 1: ${selectedGenre.stage1Title} (60m) → ${selectedGenre.stage2Title} (72h) → ${selectedGenre.stage3Title} (24h) → ${selectedGenre.stage4Title}"
            else -> "${selectedGenre.modeTitle} • 90-Page Feature • Stage 1: ${selectedGenre.stage1Title} (30m) → ${selectedGenre.stage2Title} (48h) → ${selectedGenre.stage3Title} (24h) → ${selectedGenre.stage4Title}"
        }
        StartMenuBigButton(
            badgeText = selectedGenre.icon,
            title = selectedGenre.launchCtaLabel,
            subtitle = primarySub,
            badgeColor = ElectrumGold,
            onClick = { onLaunchSpark(selectedFormat, selectedScreenplayType, selectedGenre) }
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
            .shadow(
                elevation = if (isLit) 12.dp else 7.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.72f else 0.18f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.82f else 0.25f)
            )
            .neonBloom(
                color = if (isLit) accentColor else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isLit) 0.95f else 0.45f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isLit) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = accentColor)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.84f else 0.92f)
            )
            .border(
                prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isLit,
                    accentColor = accentColor,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        val parsed = remember(text) { parseStudioButtonLabel(text) }
        val textColor = if (isDark) Color.White else (if (isLit) Color.White else Color(0xFF0F172A))
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (parsed.iconType != StudioIconType.NONE) {
                StudioMonoIcon(
                    type = parsed.iconType,
                    tint = if (isLit) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                    size = 13.dp
                )
                if (parsed.cleanLabel.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            if (parsed.cleanLabel.isNotEmpty()) {
                Text(
                    text = parsed.cleanLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = textColor,
                        fontWeight = if (isLit) FontWeight.SemiBold else FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
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
 * Interactive glowing back navigation button with squared-off architectural frame.
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
            .shadow(
                elevation = if (isLit) 12.dp else 6.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.72f else 0.18f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.82f else 0.25f)
            )
            .neonBloom(
                color = if (isLit) ScribeStarNeonRed else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isLit) 0.95f else 0.45f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isLit) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = ScribeStarNeonRed)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.84f else 0.92f)
            )
            .border(
                prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isLit,
                    accentColor = ScribeStarNeonRed,
                    isDark = isDark
                ),
                RoundedCornerShape(0.dp)
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
 * Standard layout wrapper for Start Menu steps with a Concept C Floating Glass Island header
 * and centered content overlaid cleanly on top of the Goddess Seshat artwork.
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
            // Concept C Floating Glass Island Header with Back Navigation and Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .floatingGlassIsland(isDark = isDark, elevation = 14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                letterSpacing = 0.2.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = headerSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) Color(0xFFCBD5E1) else ScreenplayTextMuted,
                                letterSpacing = 0.4.sp,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
            }

            content()
        }
    }
}

/**
 * Sleek modern squared-off button matching the ScribeStar start menu design.
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
            .shadow(
                elevation = if (isHighlighted) 16.dp else 9.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.72f else 0.18f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.85f else 0.28f)
            )
            .neonBloom(
                color = if (isHighlighted) ScribeStarNeonRed else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 0.95f else 0.40f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isHighlighted) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = ScribeStarNeonRed)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.84f else 0.92f)
            )
            .border(
                prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isHighlighted,
                    accentColor = ScribeStarNeonRed,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(0.dp)
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
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
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
 * Modern tactile squared-off card button with ScribeStar styling for sub-menu selections.
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
            .shadow(
                elevation = if (isHighlighted) 18.dp else 10.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.74f else 0.20f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.86f else 0.28f)
            )
            .neonBloom(
                color = if (isHighlighted) ScribeStarNeonRed else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 0.95f else 0.40f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isHighlighted) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = ScribeStarNeonRed)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.84f else 0.92f)
            )
            .border(
                prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isHighlighted,
                    accentColor = ScribeStarNeonRed,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(0.dp)
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
            val parsedBadge = remember(badgeText) { parseStudioButtonLabel(badgeText) }
            val parsedTitle = remember(title) { parseStudioButtonLabel(title) }
            Surface(
                color = if (isHighlighted) badgeColor.copy(alpha = 0.28f) else (if (isDark) Color(0xFF151923) else Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(1.dp, if (isHighlighted) ScribeStarNeonRed else (if (isDark) Color(0xFF283044) else Color(0xFFCBD5E1))),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (parsedBadge.iconType != StudioIconType.NONE) {
                        StudioMonoIcon(
                            type = parsedBadge.iconType,
                            tint = if (isHighlighted) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)),
                            size = 20.dp
                        )
                    } else {
                        Text(
                            text = badgeText,
                            fontSize = 20.sp,
                            color = if (isHighlighted) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Titles
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parsedTitle.cleanLabel.ifEmpty { title },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp,
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
    onSelectScreenplayBranch: () -> Unit,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onSelectSparkScreenplayBranch: () -> Unit,
    onSelectSparkFormat: (ProjectFormat, ScreenplayType?, Boolean) -> Unit,
    onSelectSparkUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenSparkMode: () -> Unit,
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
                onSelectScreenplayBranch = onSelectScreenplayBranch,
                onSelectFormat = onSelectFormat,
                onSelectUniverse = onSelectUniverse,
                onSelectSparkScreenplayBranch = onSelectSparkScreenplayBranch,
                onSelectSparkFormat = onSelectSparkFormat,
                onSelectSparkUniverse = onSelectSparkUniverse,
                onNewProject = onNewProject,
                onOpenSparkMode = onOpenSparkMode,
                onOpenLibrary = onOpenLibrary,
                onContinue = handleContinue
            )
        } else if (availableWidth >= 480.dp) {
            GlassStartMenuHubMedium(
                availableWidth = availableWidth,
                onSelectScreenplayBranch = onSelectScreenplayBranch,
                onSelectFormat = onSelectFormat,
                onSelectUniverse = onSelectUniverse,
                onSelectSparkScreenplayBranch = onSelectSparkScreenplayBranch,
                onSelectSparkFormat = onSelectSparkFormat,
                onSelectSparkUniverse = onSelectSparkUniverse,
                onNewProject = onNewProject,
                onOpenSparkMode = onOpenSparkMode,
                onOpenLibrary = onOpenLibrary,
                onContinue = handleContinue
            )
        } else {
            GlassStartMenuHubCompact(
                availableWidth = availableWidth,
                onSelectScreenplayBranch = onSelectScreenplayBranch,
                onSelectFormat = onSelectFormat,
                onSelectUniverse = onSelectUniverse,
                onSelectSparkScreenplayBranch = onSelectSparkScreenplayBranch,
                onSelectSparkFormat = onSelectSparkFormat,
                onSelectSparkUniverse = onSelectSparkUniverse,
                onNewProject = onNewProject,
                onOpenSparkMode = onOpenSparkMode,
                onOpenLibrary = onOpenLibrary,
                onContinue = handleContinue
            )
        }
    }
}

@Composable
private fun SplashColumnHeaderPill(
    title: String,
    subtitle: String,
    isSpark: Boolean,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val headerAccent = if (isSpark) ElectrumGold else ScribeStarNeonRed

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isPressedOrHeld by remember { mutableStateOf(false) }
    var showPopupManual by remember { mutableStateOf(false) }

    val isPopupVisible = isHovered || isPressedOrHeld || showPopupManual

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isPopupVisible) 12.dp else 8.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.72f else 0.18f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.82f else 0.26f)
            )
            .neonBloom(
                color = if (isPopupVisible) headerAccent else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = if (isPopupVisible) 10.dp else 5.dp,
                coreAlpha = if (isPopupVisible) 0.90f else 0.40f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(opticalGlassBrush(glassOpacity = if (isDark) 0.86f else 0.92f))
            .border(
                prismaticGlassBorder(
                    width = if (isPopupVisible) 1.5.dp else 1.1.dp,
                    isLit = isPopupVisible,
                    accentColor = headerAccent,
                    isDark = isDark
                ),
                RoundedCornerShape(0.dp)
            )
            .hoverable(interactionSource = interactionSource)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressedOrHeld = true
                        tryAwaitRelease()
                        isPressedOrHeld = false
                    },
                    onLongPress = {
                        showPopupManual = !showPopupManual
                    },
                    onTap = {
                        showPopupManual = !showPopupManual
                    }
                )
            }
            .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 6.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    fontSize = if (isCompact) 10.sp else 11.5.sp,
                    color = if (isSpark) ElectrumGold else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A))
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Subtle (i) badge indicating interactive info popup on hover or press & hold
            Surface(
                color = if (isSpark) ElectrumGold.copy(alpha = 0.18f) else (if (isDark) Color.White.copy(alpha = 0.12f) else Color(0x180F172A)),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(0.6.dp, if (isSpark) ElectrumGold.copy(alpha = 0.5f) else (if (isDark) Color.White.copy(alpha = 0.3f) else Color(0x300F172A)))
            ) {
                Text(
                    text = "ⓘ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpark) ElectrumGold else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        if (isPopupVisible) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, 48),
                onDismissRequest = { showPopupManual = false },
                properties = PopupProperties(focusable = false)
            ) {
                Surface(
                    color = if (isDark) Color(0xF80A0E17) else Color(0xF8FFFFFF),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (isSpark) ElectrumGold.copy(alpha = 0.9f) else ScribeStarNeonRed.copy(alpha = 0.85f)
                    ),
                    shadowElevation = 18.dp,
                    modifier = Modifier
                        .widthIn(min = 260.dp, max = 340.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isSpark) "🏆 WRITING CHALLENGES (FIRST DRAFT ENGINE)" else "🏛️ STANDARD STUDIO",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (isSpark) ElectrumGold else (if (isDark) Color.White else Color(0xFF0F172A))
                            )
                        )

                        Surface(
                            color = if (isSpark) ElectrumGold.copy(alpha = 0.18f) else ScribeStarNeonRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(0.8.dp, if (isSpark) ElectrumGold.copy(alpha = 0.6f) else ScribeStarNeonRed.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = if (isSpark) "🎮 RPG GAME + ANTI-PROCRASTINATION ENGINE" else "🔓 WRITE AT YOUR OWN PACE • ZERO TIMERS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.5.sp,
                                    color = if (isSpark) ElectrumGold else Color(0xFF38BDF8),
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }

                        Text(
                            text = if (isSpark) {
                                "Part immersive writing RPG, part strict anti-procrastination engine. Pick your medium and choose from 8 playable genres (Survival, Protocol, Quest, Heist, Overdrive, Roast, Virtuoso, or Detective).\n\nYou'll conquer 4 timed stages (Spark → Story Bible → Outline → Draft) with forward-only draft locks and 48-hour word quotas. Outlast the Genre AI Bouncer, earn XP currency, buy shields & power-ups, and unlock full Studio mode when you reach THE END."
                            } else {
                                "Write at your own pace with every creative tool unlocked from day one. Full unrestricted access to Lore Bible, Corkboard Outline, and Drafting Editor with zero stage timers, word quotas, or editing locks."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                            )
                        )

                        Text(
                            text = if (isSpark) "Hover away or tap to dismiss • Pick any challenge to play" else "Hover away or tap to dismiss • Click any format to start",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontSize = 9.sp,
                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassStartMenuHubWide(
    availableWidth: Dp,
    onSelectScreenplayBranch: () -> Unit,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onSelectSparkScreenplayBranch: () -> Unit,
    onSelectSparkFormat: (ProjectFormat, ScreenplayType?, Boolean) -> Unit,
    onSelectSparkUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenSparkMode: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Center justified with smooth outward expansion as window expands
    val cardWidth = (255.dp + (availableWidth - 680.dp) * 0.14f).coerceIn(255.dp, 330.dp)
    val centerGap = (90.dp + (availableWidth - 680.dp) * 0.16f).coerceIn(90.dp, 220.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Midsection: Left Column (All 5 Standard Projects) vs Right Column (All 5 Writing Challenges)
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(centerGap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top
        ) {
            // LEFT COLUMN: STANDARD STUDIO (Screenplay -> Novel -> Comic Book -> Stage Play -> Create A Universe)
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                SplashColumnHeaderPill(
                    title = "🏛️ STANDARD STUDIO",
                    subtitle = "",
                    isSpark = false
                )
                GlassFormatCard(
                    title = "Screenplay",
                    subtitle = "Film or Television Script",
                    iconType = 0,
                    onClick = onSelectScreenplayBranch,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Novel",
                    subtitle = "Book Manuscript & Chapters",
                    iconType = 2,
                    onClick = { onSelectFormat(ProjectFormat.NOVEL, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Comic Book",
                    subtitle = "Pages, Panels & SFX",
                    iconType = 4,
                    onClick = { onSelectFormat(ProjectFormat.COMIC_BOOK, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Stage Play",
                    subtitle = "Theatrical Script & Acts",
                    iconType = 3,
                    onClick = { onSelectFormat(ProjectFormat.STAGE_PLAY, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Create A Universe",
                    subtitle = "Film, TV, Novel or Blended",
                    iconType = 5,
                    onClick = onSelectUniverse,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // RIGHT COLUMN: WRITING CHALLENGES (FIRST DRAFT ENGINE)
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                SplashColumnHeaderPill(
                    title = "🏆 WRITING CHALLENGES",
                    subtitle = "",
                    isSpark = true
                )
                GlassFormatCard(
                    title = "Screenplay Challenge",
                    subtitle = "Film or TV Quest • Earn XP & Loot",
                    iconType = 0,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = onSelectSparkScreenplayBranch,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Novel Challenge",
                    subtitle = "75k–100k Word RPG • Chapter Quests",
                    iconType = 2,
                    isSparkCard = true,
                    badgeTag = "75k-100k",
                    onClick = { onSelectSparkFormat(ProjectFormat.NOVEL, null, false) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Comic Book Challenge",
                    subtitle = "Issue Boss-Run • Timed Panel Locks",
                    iconType = 4,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = { onSelectSparkFormat(ProjectFormat.COMIC_BOOK, null, false) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Stage Play Challenge",
                    subtitle = "Theatrical Gauntlet • Timed Airlocks",
                    iconType = 3,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = { onSelectSparkFormat(ProjectFormat.STAGE_PLAY, null, false) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Universe Challenge",
                    subtitle = "Cinematic, TV, Novel or Blended",
                    iconType = 5,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = onSelectSparkUniverse,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Center Dock: Open/Import, Continue Studio Project, Continue Writing Challenge
        ConceptBActionDock(
            isCompact = false,
            onNewProject = onNewProject,
            onOpenSparkMode = onOpenSparkMode,
            onOpenLibrary = onOpenLibrary,
            onContinue = onContinue
        )
    }
}

@Composable
private fun GlassStartMenuHubMedium(
    availableWidth: Dp,
    onSelectScreenplayBranch: () -> Unit,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onSelectSparkScreenplayBranch: () -> Unit,
    onSelectSparkFormat: (ProjectFormat, ScreenplayType?, Boolean) -> Unit,
    onSelectSparkUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenSparkMode: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = ((availableWidth - 48.dp) * 0.45f).coerceIn(195.dp, 255.dp)
    val centerGap = (20.dp + (availableWidth - 480.dp) * 0.14f).coerceIn(20.dp, 64.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(centerGap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top
        ) {
            // Left Column: Standard Studio
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SplashColumnHeaderPill(
                    title = "🏛️ STANDARD STUDIO",
                    subtitle = "",
                    isSpark = false,
                    isCompact = true
                )
                GlassFormatCard(
                    title = "Screenplay",
                    subtitle = "Film or TV Script",
                    iconType = 0,
                    onClick = onSelectScreenplayBranch,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Novel",
                    subtitle = "Book Manuscript",
                    iconType = 2,
                    onClick = { onSelectFormat(ProjectFormat.NOVEL, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Comic Book",
                    subtitle = "Pages & Panels",
                    iconType = 4,
                    onClick = { onSelectFormat(ProjectFormat.COMIC_BOOK, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Stage Play",
                    subtitle = "Theatrical Script",
                    iconType = 3,
                    onClick = { onSelectFormat(ProjectFormat.STAGE_PLAY, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Create A Universe",
                    subtitle = "Film, TV, Novel or Blended",
                    iconType = 5,
                    onClick = onSelectUniverse,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right Column: Writing Challenges
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SplashColumnHeaderPill(
                    title = "🏆 WRITING CHALLENGES",
                    subtitle = "",
                    isSpark = true,
                    isCompact = true
                )
                GlassFormatCard(
                    title = "Screenplay Challenge",
                    subtitle = "Film or TV • XP & Loot",
                    iconType = 0,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = onSelectSparkScreenplayBranch,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Novel Challenge",
                    subtitle = "75k–100k Word RPG",
                    iconType = 2,
                    isSparkCard = true,
                    badgeTag = "75k-100k",
                    onClick = { onSelectSparkFormat(ProjectFormat.NOVEL, null, false) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Comic Book Challenge",
                    subtitle = "Issue Boss-Run",
                    iconType = 4,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = { onSelectSparkFormat(ProjectFormat.COMIC_BOOK, null, false) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Stage Play Challenge",
                    subtitle = "Theatrical Gauntlet",
                    iconType = 3,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = { onSelectSparkFormat(ProjectFormat.STAGE_PLAY, null, false) },
                    modifier = Modifier.fillMaxWidth()
                )
                GlassFormatCard(
                    title = "Universe Challenge",
                    subtitle = "Film, TV, Novel or Blended",
                    iconType = 5,
                    isSparkCard = true,
                    badgeTag = "Play →",
                    onClick = onSelectSparkUniverse,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        ConceptBActionDock(
            isCompact = false,
            onNewProject = onNewProject,
            onOpenSparkMode = onOpenSparkMode,
            onOpenLibrary = onOpenLibrary,
            onContinue = onContinue
        )
    }
}

@Composable
private fun GlassStartMenuHubCompact(
    availableWidth: Dp,
    onSelectScreenplayBranch: () -> Unit,
    onSelectFormat: (ProjectFormat, ScreenplayType?) -> Unit,
    onSelectUniverse: () -> Unit,
    onSelectSparkScreenplayBranch: () -> Unit,
    onSelectSparkFormat: (ProjectFormat, ScreenplayType?, Boolean) -> Unit,
    onSelectSparkUniverse: () -> Unit,
    onNewProject: () -> Unit,
    onOpenSparkMode: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = ((availableWidth - 32.dp) / 2).coerceIn(142.dp, 200.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Column Headers
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            SplashColumnHeaderPill(
                title = "🏛️ STANDARD",
                subtitle = "",
                isSpark = false,
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
            SplashColumnHeaderPill(
                title = "🏆 CHALLENGES",
                subtitle = "",
                isSpark = true,
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }

        // Row 1: Screenplay vs Screenplay Challenge
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            GlassFormatCard(
                title = "Screenplay",
                iconType = 0,
                onClick = onSelectScreenplayBranch,
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
            GlassFormatCard(
                title = "Screenplay Challenge",
                iconType = 0,
                isSparkCard = true,
                onClick = onSelectSparkScreenplayBranch,
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }

        // Row 2: Novel vs Novel Challenge
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            GlassFormatCard(
                title = "Novel",
                iconType = 2,
                onClick = { onSelectFormat(ProjectFormat.NOVEL, null) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
            GlassFormatCard(
                title = "Novel Challenge",
                iconType = 2,
                isSparkCard = true,
                onClick = { onSelectSparkFormat(ProjectFormat.NOVEL, null, false) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }

        // Row 3: Comic Book vs Comic Book Challenge
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            GlassFormatCard(
                title = "Comic Book",
                iconType = 4,
                onClick = { onSelectFormat(ProjectFormat.COMIC_BOOK, null) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
            GlassFormatCard(
                title = "Comic Challenge",
                iconType = 4,
                isSparkCard = true,
                onClick = { onSelectSparkFormat(ProjectFormat.COMIC_BOOK, null, false) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }

        // Row 4: Stage Play vs Stage Play Challenge
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
                title = "Stage Challenge",
                iconType = 3,
                isSparkCard = true,
                onClick = { onSelectSparkFormat(ProjectFormat.STAGE_PLAY, null, false) },
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }

        // Row 5 (Last): Create A Universe vs Universe Challenge
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            GlassFormatCard(
                title = "Create A Universe",
                iconType = 5,
                onClick = onSelectUniverse,
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
            GlassFormatCard(
                title = "Universe Challenge",
                iconType = 5,
                isSparkCard = true,
                onClick = onSelectSparkUniverse,
                isCompact = true,
                modifier = Modifier.width(cardWidth)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ConceptBActionDock(
            isCompact = true,
            onNewProject = onNewProject,
            onOpenSparkMode = onOpenSparkMode,
            onOpenLibrary = onOpenLibrary,
            onContinue = onContinue
        )
    }
}

/**
 * Concept B Floating Action Pill Dock (Centered at Bottom).
 * Displays Open / Import Project, Continue Studio Project, and Continue Writing Challenge.
 */
@Composable
private fun ConceptBActionDock(
    isCompact: Boolean,
    onNewProject: () -> Unit,
    onOpenSparkMode: () -> Unit,
    onOpenLibrary: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    Box(
        modifier = modifier
            .floatingGlassIsland(isDark = isDark, elevation = 18.dp)
            .padding(horizontal = if (isCompact) 6.dp else 8.dp, vertical = if (isCompact) 5.dp else 6.dp)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConceptBGlassPill(
                text = if (isCompact) "📂 Open / Import" else "📂 Open / Import Project",
                onClick = onOpenLibrary,
                isCompact = isCompact
            )

            ConceptBGlassPill(
                text = if (isCompact) "▶️ Continue Studio" else "▶️ Continue Studio Project",
                onClick = onContinue,
                isCompact = isCompact,
                accentColor = ElectrumGold
            )

            ConceptBPrimaryPill(
                text = if (isCompact) "🏆 Continue Challenge" else "🏆 Continue Writing Challenge",
                onClick = onOpenSparkMode,
                isCompact = isCompact
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

    val isDark = LocalSeshatDarkTheme.current
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isLit) 12.dp else 7.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.72f else 0.20f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.85f else 0.28f)
            )
            .neonBloom(
                color = ScribeStarNeonRed,
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = 1.4.dp,
                outerAlpha = if (isLit) 0.55f else 0.30f,
                midAlpha = if (isLit) 0.90f else 0.65f,
                coreAlpha = 1.0f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                opticalPillBrush(isDark = isDark, isPrimary = true, isHighlighted = isLit)
            )
            .border(
                prismaticGlassBorder(
                    width = if (isLit) 1.4.dp else 1.2.dp,
                    isLit = true,
                    isDark = isDark
                ),
                RoundedCornerShape(0.dp)
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
            .shadow(
                elevation = if (isLit) 10.dp else 5.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.68f else 0.16f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.78f else 0.24f)
            )
            .neonBloom(
                color = if (isLit) accentColor else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = if (isLit) glowRadius else 4.dp,
                strokeWidth = 1.1.dp,
                outerAlpha = if (isLit) 0.35f else 0.08f,
                midAlpha = if (isLit) 0.70f else 0.16f,
                coreAlpha = if (isLit) 0.90f else 0.35f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isLit) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = accentColor)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.82f else 0.92f)
            )
            .border(
                prismaticGlassBorder(
                    width = 1.1.dp,
                    isLit = isLit,
                    accentColor = accentColor,
                    isDark = isDark
                ),
                RoundedCornerShape(0.dp)
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
 * Designed with squared-off architectural corners and dynamic palette accents.
 */
@Composable
private fun GlassFormatCard(
    title: String,
    iconType: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    isSparkCard: Boolean = false,
    badgeTag: String = "",
    isCompact: Boolean = false
) {
    val isDark = LocalSeshatDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighlighted = isHovered || isPressed

    val primaryGlowColor = if (isSparkCard) ElectrumGold else ScribeStarNeonRed

    val glowRadius by animateDpAsState(
        targetValue = if (isHighlighted) 18.dp else 4.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "FormatCardGlowRadius"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.60f else (if (isSparkCard) 0.16f else 0.10f),
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "FormatCardOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.90f else (if (isSparkCard) 0.30f else 0.22f),
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "FormatCardMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.0.dp else 1.3.dp,
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
            .height(if (isCompact) 48.dp else 56.dp)
            .shadow(
                elevation = if (isHighlighted) 18.dp else 10.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.78f else 0.20f),
                spotColor = if (isHighlighted) primaryGlowColor else Color.Black.copy(alpha = if (isDark) 0.88f else 0.28f)
            )
            .neonBloom(
                color = if (isHighlighted) primaryGlowColor else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 0.95f else 0.40f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isHighlighted) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = primaryGlowColor)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.82f else 0.91f)
            )
            .border(
                prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isHighlighted,
                    accentColor = primaryGlowColor,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (isCompact) 10.dp else 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSparkCard) {
                    Text(
                        text = "🎮",
                        fontSize = if (isCompact) 14.sp else 17.sp
                    )
                    Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 10.dp))
                } else {
                    FormatCardIcon(
                        type = iconType,
                        color = if (isHighlighted) Color.White else ScribeStarNeonRed,
                        modifier = Modifier.size(if (isCompact) 22.dp else 26.dp)
                    )
                    Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isSparkCard) {
                                if (isHighlighted) Color.White else ElectrumGold
                            } else {
                                if (isHighlighted || isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            },
                            fontSize = if (isCompact) 12.sp else 14.5.sp,
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isCompact && subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (isHighlighted) Color.White.copy(alpha = 0.90f)
                                else if (isDark) Color(0xFF94A3B8)
                                else Color(0xFF475569),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (!isCompact && badgeTag.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = if (isHighlighted) Color.White.copy(alpha = 0.16f) else ElectrumGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        0.8.dp,
                        if (isHighlighted) Color.White.copy(alpha = 0.65f) else ElectrumGold.copy(alpha = 0.55f)
                    )
                ) {
                    Text(
                        text = badgeTag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isHighlighted) Color.White else ElectrumGold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
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
            .shadow(
                elevation = if (isHighlighted) 16.dp else 9.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.74f else 0.18f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.86f else 0.28f)
            )
            .neonBloom(
                color = if (isHighlighted) ScribeStarNeonRed else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 0.95f else 0.40f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isHighlighted) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = ScribeStarNeonRed)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.82f else 0.91f)
            )
            .border(
                prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isHighlighted,
                    accentColor = ScribeStarNeonRed,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(0.dp)
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
                    fontWeight = FontWeight.SemiBold,
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
                    shape = RoundedCornerShape(0.dp)
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
                    color = if (isDark) ObsidianSurface else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(0.dp),
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
 * Features squared-off architectural frame and dynamic theme bloom on hover/press.
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
            .shadow(
                elevation = if (isHighlighted) 16.dp else 9.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.74f else 0.18f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.86f else 0.28f)
            )
            .neonBloom(
                color = if (isHighlighted) ScribeStarNeonRed else (if (isDark) Color.White else Color(0xFF64748B)),
                cornerRadius = 0.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isHighlighted) 0.95f else 0.40f
            )
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isHighlighted) opticalPillBrush(isDark = isDark, isHighlighted = true, highlightColor = ScribeStarNeonRed)
                else opticalGlassBrush(glassOpacity = if (isDark) 0.82f else 0.91f)
            )
            .border(
                prismaticGlassBorder(
                    width = strokeWidth,
                    isLit = isHighlighted,
                    accentColor = ScribeStarNeonRed,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(0.dp)
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
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else (if (isHighlighted) Color.White else Color(0xFF0F172A)),
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
