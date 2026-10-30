package com.seshat.writer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.seshat.writer.R
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.ai.AiConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.seshat.writer.model.DualDialogueSide
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.screenplay.CursorTarget
import com.seshat.writer.screenplay.CursorTargetType
import com.seshat.writer.screenplay.FindMode
import com.seshat.writer.screenplay.SceneItem
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.screenplay.ScreenplayViewMode
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.util.DeviceEnvironment
import com.seshat.writer.ui.theme.CharacterGold
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.DeskBackground
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.SluglineBlue
import com.seshat.writer.ui.theme.TransitionPurple
import androidx.compose.ui.graphics.Brush
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import com.seshat.writer.ui.theme.opticalGlassBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.smokedGlassBrush
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.novel.NovelCompilerDialog
import com.seshat.writer.novel.NovelCoverCreatorDialog
import com.seshat.writer.novel.NovelEditorPane
import com.seshat.writer.tv.TvEpisodeBinderPane
import com.seshat.writer.collaboration.WritersRoomDialog
import com.seshat.writer.firstdraft.AbandonedDraftWatermarkOverlay
import com.seshat.writer.firstdraft.AccountabilityReceiptModal
import com.seshat.writer.firstdraft.FirstDraftControlStrip
import com.seshat.writer.firstdraft.FirstDraftStage
import com.seshat.writer.firstdraft.IdleInstigatorBanner
import com.seshat.writer.firstdraft.PenaltySprintModal
import com.seshat.writer.firstdraft.ReadOnlyReferenceDrawer
import com.seshat.writer.firstdraft.ReceiptGenerator
import com.seshat.writer.firstdraft.SparkStageEditorView
import com.seshat.writer.firstdraft.WorkShiftReverseLockoutOverlay
import com.seshat.writer.firstdraft.WorkShiftRosterDialog
import com.seshat.writer.firstdraft.XpShopDialog
import java.util.Locale

@Composable
fun ScreenplayEditorScreen(
    viewModel: ScreenplayViewModel,
    onOpenTitlePage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isCompactScreen = screenWidthDp < 600
    val keyboardController = LocalSoftwareKeyboardController.current
    var isKeyboardActive by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }
    var aiConfig by remember { mutableStateOf(AiPreferences.load(context)) }

    LaunchedEffect(Unit) {
        viewModel.bindAppContext(context)
        viewModel.initTheme(context)
    }

    LaunchedEffect(uiState.firstDraftState.lastLootToastMessage) {
        val msg = uiState.firstDraftState.lastLootToastMessage
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearLootToastMessage()
        }
    }

    LaunchedEffect(uiState.title) {
        viewModel.loadStoryBibleIfPresent(context)
        viewModel.loadCorkboardIfPresent(context)
        viewModel.loadNovelIfPresent(context)
        viewModel.loadTvSeriesIfPresent(context)
    }

    if (uiState.isStartMenuOpen) {
        StartMenuScreen(
            viewModel = viewModel,
            uiState = uiState,
            onCloseMenu = { viewModel.closeStartMenu() },
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        )
        return
    }

    val onSaveProject: () -> Unit = {
        val success = viewModel.saveCurrentProject(context)
        if (success) {
            Toast.makeText(context, "Saved successfully ✓", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val isDarkTheme = LocalSeshatDarkTheme.current
            // Persistent Concept B Studio Background & Seshat Statue across the entire app
            Image(
                painter = painterResource(id = R.drawable.seshat_studio_splash),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isDarkTheme) 0.22f else 0.10f
                    }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                val totalProjectWords = if (uiState.projectFormat == ProjectFormat.NOVEL) {
                    uiState.novelManuscript.totalWordCount()
                } else {
                    uiState.wordCount
                }

                // Persistent First Draft Anti-Procrastination HUD Strip
                FirstDraftControlStrip(
                    state = uiState.firstDraftState,
                    currentWordCount = totalProjectWords,
                    onAdvanceStage = { viewModel.advanceFirstDraftStage(context) },
                    onToggleSprint = { viewModel.toggleFirstDraftSprint() },
                    onOpenXpShop = { viewModel.openXpShop() },
                    onOpenDutyRoster = { viewModel.openDutyRoster() },
                    onToggleReferenceDrawer = { viewModel.toggleReferenceDrawer() },
                    onOpenDailyReceipt = { viewModel.openDailyReceiptModal(context) },
                    onInsertTkToken = { viewModel.insertTkFixLaterToken(context) }
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (uiState.firstDraftState.isFirstDraftMode &&
                        uiState.firstDraftState.stage == FirstDraftStage.SPARK
                    ) {
                        SparkStageEditorView(
                            state = uiState.firstDraftState,
                            projectTitle = uiState.title,
                            onUpdateLogline = { viewModel.updateSparkLogline(context, it) },
                            onUpdatePilotLogline = { viewModel.updateSparkPilotLogline(context, it) },
                            onSealSparkAndAdvance = { viewModel.advanceFirstDraftStage(context) },
                            onOpenDutyRoster = { viewModel.openDutyRoster() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (isCompactScreen && uiState.isSidebarExpanded && !uiState.isZenMode) {
            // Full-screen sidebar drawer on mobile
            if (uiState.projectFormat == ProjectFormat.TV_SERIES && uiState.tvSeriesProject != null) {
                TvEpisodeBinderPane(
                    viewModel = viewModel,
                    uiState = uiState,
                    seriesProject = uiState.tvSeriesProject!!,
                    onCloseBinder = { viewModel.toggleSidebar() },
                    onOpenStoryBible = { viewModel.openStoryBible(context) },
                    onOpenWritersRoom = { viewModel.openWritersRoom() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                SidebarPane(
                    uiState = uiState,
                    onSceneClick = {
                        viewModel.selectScene(it)
                        viewModel.toggleSidebar()
                    },
                    onCloseSidebar = { viewModel.toggleSidebar() },
                    onOpenTitlePage = onOpenTitlePage,
                    onOpenFileExport = { showExportDialog = true },
                    onOpenReports = { viewModel.openReportsDialog() },
                    onOpenRevisions = { viewModel.openRevisionDialog() },
                    onOpenLibrary = { viewModel.openScriptLibrary() },
                    onOpenDraftDiff = { viewModel.openDraftDiff() },
                    onToggleReorderMode = { viewModel.toggleReorderMode() },
                    onMoveSceneUp = { idx -> viewModel.moveSceneUp(idx) },
                    onMoveSceneDown = { idx -> viewModel.moveSceneDown(idx) },
                    onToggleOmitScene = { blockId -> viewModel.toggleSceneOmitted(blockId) },
                    onOpenSettings = { viewModel.openSettingsDialog() },
                    onOpenScriptDoctor = { viewModel.openScriptDoctorDialog() },
                    onOpenTableRead = { viewModel.openTableRead(context) },
                    onOpenStoryBible = { viewModel.openStoryBible(context) },
                    onOpenCorkboard = { viewModel.openCorkboard(context) },
                    onOpenStartMenu = { viewModel.openStartMenu() },
                    onOpenCompiler = { viewModel.openNovelCompiler() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (isCompactScreen && uiState.isCorkboardOpen) {
            // Full-screen corkboard on mobile
            CorkboardPane(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Split-Pane Read-Only Reference Drawer for sealed SPARK / BIBLE / OUTLINE
                if (uiState.firstDraftState.isReferenceDrawerOpen) {
                    ReadOnlyReferenceDrawer(
                        state = uiState.firstDraftState,
                        bible = uiState.storyBible,
                        sceneCards = uiState.sceneCards,
                        onCloseDrawer = { viewModel.toggleReferenceDrawer() }
                    )
                }

                // Adaptive Left Sidebar (Tablet / Desktop side-by-side)
                AnimatedVisibility(
                    visible = uiState.isSidebarExpanded && !uiState.isZenMode && !isCompactScreen
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 14.dp, top = 8.dp, bottom = 12.dp, end = 6.dp)
                            .width(280.dp)
                            .fillMaxHeight()
                            .shadow(16.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(opticalGlassBrush())
                            .border(prismaticGlassBorder(), RoundedCornerShape(20.dp))
                    ) {
                        if (uiState.projectFormat == ProjectFormat.TV_SERIES && uiState.tvSeriesProject != null) {
                            TvEpisodeBinderPane(
                                viewModel = viewModel,
                                uiState = uiState,
                                seriesProject = uiState.tvSeriesProject!!,
                                onCloseBinder = { viewModel.toggleSidebar() },
                                onOpenStoryBible = { viewModel.openStoryBible(context) },
                                onOpenWritersRoom = { viewModel.openWritersRoom() },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            SidebarPane(
                                uiState = uiState,
                                onSceneClick = { viewModel.selectScene(it) },
                                onCloseSidebar = { viewModel.toggleSidebar() },
                                onOpenTitlePage = onOpenTitlePage,
                                onOpenFileExport = { showExportDialog = true },
                                onOpenReports = { viewModel.openReportsDialog() },
                                onOpenRevisions = { viewModel.openRevisionDialog() },
                                onOpenLibrary = { viewModel.openScriptLibrary() },
                                onOpenDraftDiff = { viewModel.openDraftDiff() },
                                onToggleReorderMode = { viewModel.toggleReorderMode() },
                                onMoveSceneUp = { idx -> viewModel.moveSceneUp(idx) },
                                onMoveSceneDown = { idx -> viewModel.moveSceneDown(idx) },
                                onToggleOmitScene = { blockId -> viewModel.toggleSceneOmitted(blockId) },
                                onOpenSettings = { viewModel.openSettingsDialog() },
                                onOpenScriptDoctor = { viewModel.openScriptDoctorDialog() },
                                onOpenTableRead = { viewModel.openTableRead(context) },
                                onOpenStoryBible = { viewModel.openStoryBible(context) },
                                onOpenCorkboard = { viewModel.openCorkboard(context) },
                                onOpenStartMenu = { viewModel.openStartMenu() },
                                onOpenCompiler = { viewModel.openNovelCompiler() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Screenplay Canvas & Corkboard Outline Area
                val showCorkboard = uiState.isCorkboardOpen
                val showEditorCanvas = if (isCompactScreen) !uiState.isCorkboardOpen else (!uiState.isCorkboardOpen || uiState.isCorkboardSideBySide)

                if (showEditorCanvas) {
                    if (uiState.projectFormat == ProjectFormat.NOVEL) {
                        NovelEditorPane(
                            viewModel = viewModel,
                            uiState = uiState,
                            manuscript = uiState.novelManuscript,
                            activeChapterIndex = uiState.activeChapterIndex,
                            onSelectChapter = { viewModel.selectNovelChapter(it) },
                            onUpdateManuscript = { viewModel.updateNovelManuscript(context, it) },
                            onOpenCompiler = { viewModel.openNovelCompiler() },
                            onToggleCorkboard = { viewModel.toggleCorkboard(context) },
                            onOpenStoryBible = { viewModel.openStoryBible(context) },
                            onOpenStartMenu = { viewModel.openStartMenu() },
                            onToggleDictation = { viewModel.toggleDictation(context) },
                            onSaveProject = onSaveProject,
                            onOpenStudioDrawer = { viewModel.openStudioDrawer() },
                            modifier = Modifier
                                .weight(if (showCorkboard) 1.1f else 1f)
                                .fillMaxHeight()
                        )
                    } else {
                        EditorCanvas(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenTitlePage = onOpenTitlePage,
                            onOpenFileExport = { showExportDialog = true },
                            onSaveProject = onSaveProject,
                            isAiPolishEnabled = aiConfig.isEnabled && aiConfig.enableInlineAssistant,
                            isCompactScreen = isCompactScreen,
                            isKeyboardActive = isKeyboardActive,
                            onToggleKeyboard = {
                                isKeyboardActive = !isKeyboardActive
                                if (isKeyboardActive) {
                                    keyboardController?.show()
                                } else {
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier
                                .weight(if (showCorkboard) 1.1f else 1f)
                                .fillMaxHeight()
                        )
                    }
                }

                if (showEditorCanvas && showCorkboard) {
                    VerticalDivider(color = ObsidianSurfaceVariant)
                }

                if (showCorkboard) {
                    CorkboardPane(
                        viewModel = viewModel,
                        uiState = uiState,
                        isCompactScreen = isCompactScreen,
                        onSaveProject = onSaveProject,
                        modifier = Modifier
                            .weight(if (showEditorCanvas) 0.9f else 1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
                }

                // Gemini AI Bouncer / 60s Idle Instigator Bottom Banner
                IdleInstigatorBanner(
                    promptText = uiState.firstDraftState.idleInstigatorPrompt,
                    isLoading = uiState.firstDraftState.isIdleInstigatorLoading,
                    onDismiss = { viewModel.dismissIdleInstigator() }
                )
            }

            // Red Diagonal "ABANDONED DRAFT" Watermark when 48-Hour Decay is active
            AbandonedDraftWatermarkOverlay(
                isDecayed = uiState.firstDraftState.isDecayed
            )

            // Reverse-Lockout Overlay when a scheduled Day Job WorkShiftBlock is active
            val activeWorkShift = uiState.firstDraftState.workShiftFreeze.activeShiftAt()
            if (activeWorkShift != null && uiState.firstDraftState.isArcadeLocked) {
                WorkShiftReverseLockoutOverlay(activeShift = activeWorkShift)
            }
    }
}

    // 48-Hour Decay Penalty Sprint Modal (500 net-new words in 25 minutes)
    if (uiState.firstDraftState.isDecayed && uiState.firstDraftState.isArcadeLocked) {
        PenaltySprintModal(
            state = uiState.firstDraftState,
            onUpdateScratchpad = { viewModel.updatePenaltyScratchpad(context, it) },
            onUseFreezeToken = { viewModel.purchaseXpShopItem(context, com.seshat.writer.firstdraft.XpShopItem.FREEZE_TOKEN) },
            onOpenDecayReceipt = { viewModel.openMandatoryDecayReceiptModal() }
        )
    }

    // Forward-Momentum XP Shop Modal
    if (uiState.isXpShopOpen) {
        XpShopDialog(
            state = uiState.firstDraftState,
            onPurchaseItem = { item -> viewModel.purchaseXpShopItem(context, item) },
            onDismiss = { viewModel.closeXpShop() }
        )
    }

    // Day Job Duty Roster (WorkShiftFreeze) Modal
    if (uiState.isDutyRosterOpen) {
        WorkShiftRosterDialog(
            freeze = uiState.firstDraftState.workShiftFreeze,
            onSaveAndLockRoster = { shifts -> viewModel.saveWorkShiftRoster(context, shifts) },
            onDismiss = { viewModel.closeDutyRoster() }
        )
    }

    // Social Accountability Receipt Modal (1080x1920 9:16 PNG + Native OS Share Sheet)
    val activeReceipt = uiState.activeReceiptModal
    if (activeReceipt != null) {
        AccountabilityReceiptModal(
            receipt = activeReceipt,
            onShareClicked = { ReceiptGenerator.shareReceiptViaNativeOs(context, activeReceipt) },
            onDismiss = { viewModel.dismissReceiptModal() }
        )
    }

    if (uiState.isStudioDrawerOpen) {
        StudioNavigationDrawer(
            uiState = uiState,
            onCloseDrawer = { viewModel.closeStudioDrawer() },
            onSaveProject = onSaveProject,
            onOpenStartMenu = { viewModel.openStartMenu() },
            onOpenLibrary = { viewModel.openScriptLibrary() },
            onOpenEditorCanvas = {
                if (uiState.isCorkboardOpen) viewModel.closeCorkboard()
            },
            onOpenCorkboard = { viewModel.openCorkboard(context) },
            onOpenStoryBible = { viewModel.openStoryBible(context) },
            onOpenWritersRoom = { viewModel.openWritersRoom() },
            onOpenDraftDiff = { viewModel.openDraftDiff() },
            onOpenFileExport = { showExportDialog = true },
            onOpenCompiler = { viewModel.openNovelCompiler() },
            onOpenCoverCreator = { viewModel.openCoverCreator() },
            onOpenTitlePage = onOpenTitlePage,
            onOpenReports = { viewModel.openReportsDialog() },
            onOpenRevisions = { viewModel.openRevisionDialog() },
            onOpenTableRead = { viewModel.openTableRead(context) },
            onOpenScriptDoctor = { viewModel.openScriptDoctorDialog() },
            onOpenFind = { viewModel.openFind() },
            onToggleDictation = { viewModel.toggleDictation(context) },
            onOpenSettings = { viewModel.openSettingsDialog() }
        )
    }

    if (uiState.isCoverCreatorOpen) {
        NovelCoverCreatorDialog(
            manuscript = uiState.novelManuscript,
            onSaveDesign = { updatedDesign ->
                viewModel.updateNovelManuscript(context, uiState.novelManuscript.copy(coverDesign = updatedDesign))
            },
            onDismiss = { viewModel.closeCoverCreator() }
        )
    }

    if (showExportDialog || uiState.isExportDialogOpen) {
        ExportDialog(
            viewModel = viewModel,
            onDismiss = {
                showExportDialog = false
                viewModel.closeExportDialog()
            }
        )
    }

    if (uiState.isTitlePageOpen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.closeTitlePage() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val titlePageVm = remember { com.seshat.writer.titlepage.TitlePageViewModel() }
            com.seshat.writer.titlepage.TitlePageScreen(
                viewModel = titlePageVm,
                onCloseWindow = { viewModel.closeTitlePage() }
            )
        }
    }

    if (uiState.isShortcutsDialogOpen) {
        ShortcutsDialog(
            onDismiss = { viewModel.closeShortcutsDialog() }
        )
    }

    if (uiState.isAboutDialogOpen) {
        AboutDialog(
            onDismiss = { viewModel.closeAboutDialog() }
        )
    }

    if (uiState.isReportsDialogOpen) {
        CharacterReportDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeReportsDialog() }
        )
    }

    if (uiState.isRevisionDialogOpen) {
        ProductionRevisionDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeRevisionDialog() }
        )
    }

    if (uiState.isDraftDiffOpen) {
        DraftComparisonDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeDraftDiff() }
        )
    }

    if (uiState.isLibraryOpen) {
        ScriptLibraryDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeScriptLibrary() }
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
                aiConfig = AiPreferences.load(context)
            },
            onConfigChanged = { newConfig ->
                aiConfig = newConfig
            }
        )
    }

    if (uiState.isScriptDoctorDialogOpen) {
        val currentSceneHeading = uiState.scenes.getOrNull(uiState.selectedSceneIndex)?.heading ?: "CURRENT SCENE"
        val sceneBlocks = remember(uiState.blocks, uiState.selectedSceneIndex) {
            val (lead, scenes) = ScreenplayViewModel.partitionIntoScenes(uiState.blocks)
            if (uiState.selectedSceneIndex in scenes.indices) scenes[uiState.selectedSceneIndex] else uiState.blocks.take(20)
        }
        val charsInScene = sceneBlocks.filter { it.type == BlockType.CHARACTER }.map { it.text.trim().uppercase(java.util.Locale.US) }.distinct()
        ScriptDoctorDialog(
            currentSceneHeading = currentSceneHeading,
            currentSceneBlocks = sceneBlocks,
            charactersInScene = charsInScene,
            storyBibleContext = viewModel.getStoryBibleAiSnippet(),
            onDismiss = { viewModel.closeScriptDoctorDialog() }
        )
    }

    if (uiState.isStoryBibleOpen) {
        val activeCharacters = uiState.characters
        val activeLocations = remember(uiState.scenes) {
            uiState.scenes.map { it.heading.trim() }.distinct()
        }
        StoryBibleDialog(
            initialBible = uiState.storyBible,
            activeCharactersInScript = activeCharacters,
            activeLocationsInScript = activeLocations,
            projectFormat = uiState.projectFormat,
            screenplayType = uiState.screenplayType,
            firstDraftState = uiState.firstDraftState,
            onSaveBible = { updated -> viewModel.saveStoryBible(context, updated) },
            onDismiss = { viewModel.closeStoryBible() },
            onToggleTheme = { viewModel.toggleThemeMode(context) }
        )
    }

    if (uiState.isInlineAiPolishOpen) {
        val targetBlock = uiState.blocks.find { it.id == uiState.inlineAiTargetBlockId }
        if (targetBlock != null) {
            val blockIndex = uiState.blocks.indexOf(targetBlock)
            var speaker: String? = null
            if (targetBlock.type == BlockType.DIALOGUE) {
                for (i in (blockIndex - 1) downTo 0) {
                    if (uiState.blocks[i].type == BlockType.CHARACTER) {
                        speaker = uiState.blocks[i].text
                        break
                    }
                    if (uiState.blocks[i].type == BlockType.SCENE_HEADING) break
                }
            }
            InlineAiPolishDialog(
                targetBlock = targetBlock,
                speakerName = speaker,
                sceneContext = uiState.scenes.getOrNull(uiState.selectedSceneIndex)?.heading ?: "",
                onApplyReplacement = { replacement ->
                    viewModel.applyAiReplacement(targetBlock.id, replacement)
                },
                onDismiss = { viewModel.closeInlineAiPolish() }
            )
        }
    }

    if (uiState.isCompilerDialogOpen) {
        NovelCompilerDialog(
            manuscript = uiState.novelManuscript,
            onDismiss = { viewModel.closeNovelCompiler() }
        )
    }

    if (uiState.isWritersRoomOpen) {
        WritersRoomDialog(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { viewModel.closeWritersRoom() }
        )
    }

    if (uiState.isTableReadActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            TableReadPlayerBar(
                tableReadManager = viewModel.getTableReadManager(context),
                characters = uiState.characters,
                onClose = { viewModel.closeTableRead() }
            )
        }
    }

    if (uiState.isDictating || uiState.isDictationFormatting || uiState.dictationLiveText.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            VoiceDictationBar(
                isListening = uiState.isDictating,
                isFormatting = uiState.isDictationFormatting,
                transcribedText = uiState.dictationLiveText,
                audioRmsLevel = uiState.dictationAudioRms,
                aiConfig = aiConfig,
                errorMessage = uiState.dictationError,
                onFormatAndInsert = { viewModel.insertDictatedSpeech(context) },
                onInsertAsType = { type -> viewModel.insertDictatedSpeechAsType(type) },
                onStopListening = { viewModel.stopDictation() },
                onStartListening = { viewModel.startDictation(context) },
                onCancel = { viewModel.cancelDictation() }
            )
        }
    }
}
/**
 * Tactile glass action pill button with red border, high transparency,
 * and bright radiant neon bloom on hover or touch press.
 */
@Composable
private fun GlassEditorPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isHighlight: Boolean = false,
    accentColor: Color = ScribeStarNeonRed,
    badgeText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed || isHighlight

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 8.dp else if (isPrimary) 5.dp else 0.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "PillGlow"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.50f else if (isPrimary) 0.28f else 0.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "PillOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.85f else if (isPrimary) 0.52f else 0.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "PillMidAlpha"
    )
    val strokeWidth by animateDpAsState(
        targetValue = if (isLit) 1.8.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "PillStroke"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "PillScale"
    )

    val isDark = LocalSeshatDarkTheme.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .defaultMinSize(minHeight = 42.dp, minWidth = 52.dp)
            .shadow(
                elevation = if (isLit || isPrimary) 10.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
            )
            .neonBloom(
                color = accentColor,
                cornerRadius = 16.dp,
                glowRadius = glowRadius,
                strokeWidth = strokeWidth,
                outerAlpha = outerAlpha,
                midAlpha = midAlpha,
                coreAlpha = if (isLit) 1.0f else 0.85f
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isPrimary) {
                    SolidColor(accentColor)
                } else {
                    opticalPillBrush(isLit = isLit, accentColor = accentColor, isDark = isDark)
                }
            )
            .border(
                if (isPrimary) BorderStroke(strokeWidth, Color.Transparent)
                else prismaticGlassBorder(width = strokeWidth, isLit = isLit, accentColor = accentColor, isDark = isDark),
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
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isDark) Color.White else (if (isPrimary || isLit) Color.White else Color(0xFF0F172A)),
                    fontWeight = if (isPrimary || isLit) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 12.5.sp
                ),
                maxLines = 1,
                softWrap = false
            )
            if (badgeText != null) {
                Surface(
                    color = if (isDark) Color(0x33FFFFFF) else Color(0x150F172A),
                    shape = RoundedCornerShape(7.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarPane(
    uiState: ScreenplayUiState,
    onSceneClick: (SceneItem) -> Unit,
    onCloseSidebar: () -> Unit,
    onOpenTitlePage: () -> Unit,
    onOpenFileExport: () -> Unit,
    onOpenReports: () -> Unit = {},
    onOpenRevisions: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenDraftDiff: () -> Unit = {},
    onToggleReorderMode: () -> Unit = {},
    onMoveSceneUp: (Int) -> Unit = {},
    onMoveSceneDown: (Int) -> Unit = {},
    onToggleOmitScene: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenScriptDoctor: () -> Unit = {},
    onOpenTableRead: () -> Unit = {},
    onOpenStoryBible: () -> Unit = {},
    onOpenCorkboard: () -> Unit = {},
    onOpenStartMenu: () -> Unit = {},
    onOpenCompiler: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiConfig = remember { AiPreferences.load(context) }

    Column(
        modifier = modifier.padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .neonBloom(
                            color = ScribeStarNeonRed,
                            cornerRadius = 8.dp,
                            glowRadius = 4.dp,
                            strokeWidth = 1.dp,
                            outerAlpha = 0.20f,
                            midAlpha = 0.40f
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(ScribeStarNeonRed.copy(alpha = 0.22f))
                        .border(BorderStroke(1.dp, ScribeStarNeonRed), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "BINDER ⌵",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SESHAT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = if (uiState.projectFormat == ProjectFormat.NOVEL) "NOVEL STUDIO" else "SCREENPLAY STUDIO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = ScribeStarNeonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
            IconButton(onClick = onCloseSidebar) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Collapse Sidebar",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val pagesStat = if (uiState.projectFormat == ProjectFormat.NOVEL) {
            uiState.novelManuscript.estimatedPages().toString()
        } else {
            uiState.pageCount.toString()
        }
        val scenesStat = if (uiState.projectFormat == ProjectFormat.NOVEL) {
            uiState.novelManuscript.chapters.size.toString()
        } else {
            uiState.sceneCount.toString()
        }
        val wordsStat = if (uiState.projectFormat == ProjectFormat.NOVEL) {
            uiState.novelManuscript.totalWordCount().toString()
        } else {
            uiState.wordCount.toString()
        }
        val scenesLabel = if (uiState.projectFormat == ProjectFormat.NOVEL) "Chapters" else "Scenes"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(label = "Pages", value = pagesStat, modifier = Modifier.weight(1f))
            StatCard(label = scenesLabel, value = scenesStat, modifier = Modifier.weight(1f))
            StatCard(label = "Words", value = wordsStat, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = ScribeStarNeonRed.copy(alpha = 0.25f))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACT I • SCENES (${uiState.scenes.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = Color.White,
                    fontSize = 11.sp
                )
            )
            Surface(
                color = if (uiState.isReorderMode) ScribeStarNeonRed.copy(alpha = 0.25f) else Color(0xFF0F172A),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onToggleReorderMode() }
                    .border(
                        1.dp,
                        if (uiState.isReorderMode) ScribeStarNeonRed else Color(0xFF334155),
                        RoundedCornerShape(6.dp)
                    )
            ) {
                Text(
                    text = if (uiState.isReorderMode) "✓ DONE" else "⇅ REORDER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isReorderMode) Color.White else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(uiState.scenes) { idx, scene ->
                val isSelected = scene.index == uiState.selectedSceneIndex
                val showMoveControls = uiState.isReorderMode || isSelected
                val sceneInteraction = remember { MutableInteractionSource() }
                val sceneHovered by sceneInteraction.collectIsHoveredAsState()
                val scenePressed by sceneInteraction.collectIsPressedAsState()
                val sceneLit = isSelected || sceneHovered || scenePressed

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (sceneLit) {
                                Modifier.neonBloom(
                                    color = ScribeStarNeonRed,
                                    cornerRadius = 8.dp,
                                    glowRadius = if (isSelected) 6.dp else 4.dp,
                                    strokeWidth = 1.2.dp,
                                    outerAlpha = if (isSelected) 0.30f else 0.16f,
                                    midAlpha = if (isSelected) 0.60f else 0.35f
                                )
                            } else Modifier
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (sceneLit) {
                                Brush.horizontalGradient(listOf(Color(0x65450E1A), Color(0x7524050D)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0x281A2333), Color(0x380D121C)))
                            }
                        )
                        .border(
                            BorderStroke(
                                if (sceneLit) 1.3.dp else 1.dp,
                                if (sceneLit) ScribeStarNeonRed else ScribeStarNeonRed.copy(alpha = 0.35f)
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = sceneInteraction,
                            indication = null
                        ) { onSceneClick(scene) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 7.dp)
                    ) {
                        // Glowing vertical neon indicator bar for active scene
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(20.dp)
                                    .neonBloom(
                                        color = ScribeStarNeonRed,
                                        cornerRadius = 2.dp,
                                        glowRadius = 4.dp
                                    )
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ScribeStarNeonRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (uiState.showSceneNumbers) "${scene.sceneNumber}. ${scene.heading}" else "${scene.index}. ${scene.heading}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = MainAppFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (scene.isOmitted) ScreenplayTextMuted else if (isSelected) Color.White else Color(0xFF94A3B8)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (scene.isOmitted) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFFC0392B).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = "OMITTED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = MainAppFontFamily,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE74C3C)
                                        ),
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        if (isSelected && !uiState.isReorderMode) {
                            Surface(
                                color = Color(0xFF090D14),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onToggleOmitScene(scene.blockId) }
                                    .border(0.8.dp, if (scene.isOmitted) ScribeStarNeonRed else Color(0xFF475569), RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = if (scene.isOmitted) "RESTORE" else "OMIT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MainAppFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scene.isOmitted) ScribeStarNeonRed else Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (showMoveControls) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                val canMoveUp = idx > 0
                                Surface(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(enabled = canMoveUp) {
                                            onMoveSceneUp(scene.index)
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "▲",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canMoveUp) ScribeStarNeonRed else ScreenplayTextMuted.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                                val canMoveDown = idx < uiState.scenes.size - 1
                                Surface(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(enabled = canMoveDown) {
                                            onMoveSceneDown(scene.index)
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "▼",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canMoveDown) ScribeStarNeonRed else ScreenplayTextMuted.copy(alpha = 0.3f)
                                        )
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

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(opticalGlassBrush(glassOpacity = 0.85f))
            .border(prismaticGlassBorder(width = 1.dp), RoundedCornerShape(10.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )
            )
        }
    }
}

private sealed class RenderItem {
    data class Single(val block: ScreenplayBlock, val overallIndex: Int) : RenderItem()
    data class Dual(
        val dualId: String,
        val leftBlocks: List<ScreenplayBlock>,
        val rightBlocks: List<ScreenplayBlock>
    ) : RenderItem()
}

private fun groupBlocksForRendering(blocks: List<ScreenplayBlock>, allBlocks: List<ScreenplayBlock>): List<RenderItem> {
    val items = mutableListOf<RenderItem>()
    var i = 0
    while (i < blocks.size) {
        val b = blocks[i]
        if (b.dualDialogueId != null) {
            val dualId = b.dualDialogueId
            val group = mutableListOf<ScreenplayBlock>()
            while (i < blocks.size && blocks[i].dualDialogueId == dualId) {
                group.add(blocks[i])
                i++
            }
            val left = group.filter { it.dualSide == DualDialogueSide.LEFT }
            val right = group.filter { it.dualSide == DualDialogueSide.RIGHT }
            items.add(RenderItem.Dual(dualId, left, right))
        } else {
            val overallIdx = allBlocks.indexOf(b)
            items.add(RenderItem.Single(b, overallIdx))
            i++
        }
    }
    return items
}

private fun buildBlockCallbacks(
    blockId: String,
    actions: EditorCanvasActions,
    onDragStart: (String) -> Unit,
    onDragMove: (String, Float) -> Unit,
    onDragEnd: () -> Unit,
    onCoordsChanged: (String, LayoutCoordinates) -> Unit
): BlockCallbacks {
    return BlockCallbacks(
        onOpenFind = actions::openFind,
        onCloseFind = actions::closeFind,
        onTextChange = { actions.updateBlock(blockId, it) },
        onEnter = { actions.enterPressed(blockId) },
        onTab = { actions.tabPressed(blockId) },
        onBackspaceOnEmpty = { actions.backspaceEmpty(blockId) },
        onNavigateUp = { col -> actions.navigateUp(blockId, col) },
        onNavigateDown = { col -> actions.navigateDown(blockId, col) },
        onNavigateLeft = { actions.navigateLeft(blockId) },
        onNavigateRight = { actions.navigateRight(blockId) },
        onZoomIn = actions::zoomIn,
        onZoomOut = actions::zoomOut,
        onResetZoom = actions::resetZoom,
        onFocused = { actions.focusBlock(blockId) },
        onToggleHighlight = { isShift -> actions.toggleBlockHighlight(blockId, isShift) },
        onDragHighlightStart = { onDragStart(blockId) },
        onDragHighlightMove = { rootY -> onDragMove(blockId, rootY) },
        onDragHighlightEnd = onDragEnd,
        onCoordinatesChanged = { coords -> onCoordsChanged(blockId, coords) },
        onExpandHighlightDown = { actions.expandHighlightDown(blockId) },
        onExpandHighlightUp = { actions.expandHighlightUp(blockId) },
        onClearHighlight = actions::clearHighlight,
        onUpdateNote = { note -> actions.setBlockNote(blockId, note) },
        onDeleteNote = { actions.deleteBlockNote(blockId) },
        onToggleRevised = { actions.toggleBlockRevised(blockId) },
        onOpenAiPolish = { actions.openAiPolish(blockId) },
        onToggleDictation = actions::toggleDictation,
        onCancelDictation = actions::cancelDictation,
        onOpenStoryBible = actions::openStoryBible,
        onToggleCorkboard = actions::toggleCorkboard,
        onInsertTkToken = actions::insertTkToken
    )
}

private fun buildDualBlockCallbacks(
    blockId: String,
    actions: DualDialogueActions
): BlockCallbacks {
    return BlockCallbacks(
        onUpdateNote = { note -> actions.onUpdateNote(blockId, note) },
        onDeleteNote = { actions.onDeleteNote(blockId) },
        onTextChange = { actions.onTextChange(blockId, it) },
        onEnter = { actions.onEnter(blockId) },
        onTab = { actions.onTab(blockId) },
        onBackspaceOnEmpty = { actions.onBackspaceOnEmpty(blockId) },
        onNavigateUp = { col -> actions.onNavigateUp(blockId, col) },
        onNavigateDown = { col -> actions.onNavigateDown(blockId, col) },
        onNavigateLeft = { actions.onNavigateLeft(blockId) },
        onNavigateRight = { actions.onNavigateRight(blockId) },
        onZoomIn = actions.onZoomIn,
        onZoomOut = actions.onZoomOut,
        onResetZoom = actions.onResetZoom,
        onFocused = { actions.onFocused(blockId) },
        onToggleHighlight = { isShift -> actions.onToggleHighlight(blockId, isShift) },
        onExpandHighlightDown = { actions.onExpandHighlightDown(blockId) },
        onExpandHighlightUp = { actions.onExpandHighlightUp(blockId) },
        onClearHighlight = actions.onClearHighlight,
        onToggleRevised = { actions.onToggleRevised(blockId) },
        onToggleDictation = actions.onToggleDictation,
        onCancelDictation = actions.onCancelDictation,
        onOpenStoryBible = actions.onOpenStoryBible,
        onToggleCorkboard = actions.onToggleCorkboard,
        onInsertTkToken = actions.onInsertTkToken
    )
}

@Composable
private fun CompactScreenplayTopBar(
    uiState: ScreenplayUiState,
    actions: EditorCanvasActions,
    onOpenTitlePage: () -> Unit,
    onOpenFileExport: () -> Unit,
    onSaveProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Floating Circle Hamburger & Floating Logo+Title Chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            val isDark = LocalSeshatDarkTheme.current
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(8.dp, CircleShape, spotColor = ScribeStarNeonRed.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(opticalPillBrush(isDark = isDark))
                    .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), CircleShape)
                    .clickable(onClick = actions::openStudioDrawer)
                    .size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Studio Navigation Drawer",
                        tint = ScribeStarNeonRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(opticalPillBrush(isDark = isDark))
                    .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenTitlePage)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 38.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.seshat_emblem_logo),
                            contentDescription = "ScribeStar Logo",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.title.ifBlank { "UNTITLED" }.uppercase(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.9.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 12.5.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 110.dp)
                        )
                    }
                }
            }
        }

        // Right: Horizontally Scrollable Floating Action Pills
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
        ) {
            GlassEditorPillButton(
                text = "💾 Save",
                onClick = onSaveProject,
                isPrimary = true
            )

            val activeSceneNumber = uiState.selectedSceneIndex + 1
            GlassEditorPillButton(
                text = "Scene $activeSceneNumber",
                onClick = actions::toggleSidebar
            )

            GlassEditorPillButton(
                text = "📌 Corkboard",
                onClick = actions::toggleCorkboard,
                isHighlight = uiState.isCorkboardOpen
            )

            GlassEditorPillButton(
                text = "📖 Bible",
                onClick = actions::openStoryBible
            )

            if (uiState.firstDraftState.canUseAiDoctor()) {
                GlassEditorPillButton(
                    text = "🧠 Doctor",
                    onClick = actions::openScriptDoctor
                )
            }

            if (!uiState.firstDraftState.isArcadeLocked) {
                GlassEditorPillButton(
                    text = "🚀 Compile",
                    onClick = onOpenFileExport
                )
            }

            if (uiState.isDictating) {
                GlassEditorPillButton(
                    text = "🔴 Recording",
                    onClick = actions::toggleDictation,
                    isPrimary = true
                )
            } else {
                GlassEditorPillButton(
                    text = "🎙️ Dictate",
                    onClick = actions::toggleDictation
                )
            }

            GlassEditorPillButton(
                text = if (LocalSeshatDarkTheme.current) "🌙 Dark" else "☀️ Light",
                onClick = actions::toggleThemeMode
            )
        }
    }
}

@Composable
private fun DesktopScreenplayTopBar(
    uiState: ScreenplayUiState,
    actions: EditorCanvasActions,
    onOpenTitlePage: () -> Unit,
    onOpenFileExport: () -> Unit,
    onSaveProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Floating Circle Hamburger + Floating Logo+Title Chip + Main Action Pills
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isDark = LocalSeshatDarkTheme.current
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(8.dp, CircleShape, spotColor = ScribeStarNeonRed.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(opticalPillBrush(isDark = isDark))
                    .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), CircleShape)
                    .clickable(onClick = actions::openStudioDrawer)
                    .size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Studio Navigation Drawer",
                        tint = ScribeStarNeonRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(opticalPillBrush(isDark = isDark))
                    .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenTitlePage)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 42.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.seshat_emblem_logo),
                            contentDescription = "ScribeStar Logo",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.title.ifBlank { "UNTITLED SCRIPT" }.uppercase(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            val activeSceneNumber = uiState.selectedSceneIndex + 1
            GlassEditorPillButton(
                text = "Scene $activeSceneNumber",
                onClick = actions::toggleSidebar
            )

            GlassEditorPillButton(
                text = "📌 Corkboard",
                onClick = actions::toggleCorkboard,
                isHighlight = uiState.isCorkboardOpen
            )

            GlassEditorPillButton(
                text = "📖 Bible",
                onClick = actions::openStoryBible
            )

            if (uiState.firstDraftState.canUseAiDoctor()) {
                GlassEditorPillButton(
                    text = "🧠 Doctor",
                    onClick = actions::openScriptDoctor
                )
            }

            if (!uiState.firstDraftState.isArcadeLocked) {
                GlassEditorPillButton(
                    text = "🚀 Compile",
                    onClick = onOpenFileExport
                )
            }

            GlassEditorPillButton(
                text = "💾 Save",
                onClick = onSaveProject,
                isPrimary = true
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right: View Mode, Zoom Pill, Scene #s, Notes, Find, Dictate, Zen, Theme
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!uiState.firstDraftState.isArcadeLocked) {
                GlassEditorPillButton(
                    text = if (uiState.viewMode == ScreenplayViewMode.PAGE_VIEW) "📄 Page View" else "📜 Inline View",
                    onClick = actions::toggleViewMode
                )
            }

            // Floating Zoom Pill Capsule
            val isDarkThemeForZoom = LocalSeshatDarkTheme.current
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(opticalPillBrush(isDark = isDarkThemeForZoom))
                    .border(prismaticGlassBorder(width = 1.dp, isDark = isDarkThemeForZoom), RoundedCornerShape(16.dp))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 42.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "–",
                            color = if (isDarkThemeForZoom) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable(onClick = actions::zoomOut)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "${(uiState.zoomLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isDarkThemeForZoom) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable(onClick = actions::resetZoom)
                                .padding(horizontal = 6.dp)
                        )
                        Text(
                            text = "+",
                            color = if (isDarkThemeForZoom) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable(onClick = actions::zoomIn)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!uiState.firstDraftState.isArcadeLocked) {
                GlassEditorPillButton(
                    text = if (uiState.showSceneNumbers) "# Scene #s ON" else "# Scene #s",
                    onClick = actions::toggleSceneNumbers,
                    isHighlight = uiState.showSceneNumbers
                )
            }

            GlassEditorPillButton(
                text = if (uiState.notesCount > 0) "📝 Notes (${uiState.notesCount})" else "📝 Notes",
                onClick = actions::toggleWriterNotes,
                isHighlight = uiState.showWriterNotes
            )

            GlassEditorPillButton(
                text = "🔍 Find",
                onClick = { if (uiState.isFindReplaceVisible) actions.closeFind() else actions.openFind(false) },
                isHighlight = uiState.isFindReplaceVisible
            )

            GlassEditorPillButton(
                text = if (uiState.isDictating) "🔴 Recording" else "🎙️ Dictate",
                onClick = actions::toggleDictation,
                isPrimary = uiState.isDictating
            )

            GlassEditorPillButton(
                text = if (uiState.isZenMode) "Exit Zen" else "Zen",
                onClick = actions::toggleZenMode,
                isHighlight = uiState.isZenMode
            )

            GlassEditorPillButton(
                text = if (LocalSeshatDarkTheme.current) "🌙 Dark" else "☀️ Light",
                onClick = actions::toggleThemeMode
            )
        }
    }
}

@Composable
private fun EditorCanvas(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    onOpenTitlePage: () -> Unit,
    onOpenFileExport: () -> Unit,
    onSaveProject: () -> Unit,
    isAiPolishEnabled: Boolean = false,
    isCompactScreen: Boolean = false,
    isKeyboardActive: Boolean = false,
    onToggleKeyboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isGooglebook = remember { DeviceEnvironment.isGooglebook(context) }
    val actions = remember(viewModel, context) { EditorCanvasActions(viewModel, context) }

    Column(
        modifier = modifier
            .background(ambientWorkspaceBrush(LocalSeshatDarkTheme.current))
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val isCmdOrCtrl = keyEvent.isCtrlPressed || keyEvent.isMetaPressed
                    if (isCmdOrCtrl && keyEvent.key == Key.S) {
                        onSaveProject()
                        return@onPreviewKeyEvent true
                    }
                    if (isCmdOrCtrl && keyEvent.key == Key.K) {
                        actions.insertTkToken()
                        return@onPreviewKeyEvent true
                    }
                    if ((isCmdOrCtrl && keyEvent.key == Key.D) ||
                        keyEvent.key == Key.F10 || keyEvent.key == Key.F5
                    ) {
                        actions.toggleDictation()
                        return@onPreviewKeyEvent true
                    }
                    if (keyEvent.key == Key.Escape && uiState.isDictating) {
                        actions.cancelDictation()
                        return@onPreviewKeyEvent true
                    }
                    if (keyEvent.isCtrlPressed && keyEvent.key == Key.B) {
                        actions.openStoryBible()
                        return@onPreviewKeyEvent true
                    }
                    if (keyEvent.isCtrlPressed && keyEvent.key == Key.O) {
                        actions.toggleCorkboard()
                        return@onPreviewKeyEvent true
                    }
                    if (keyEvent.isCtrlPressed && keyEvent.key == Key.F) {
                        actions.openFind(false)
                        return@onPreviewKeyEvent true
                    }
                }
                false
            }
    ) {
        // Top Floating Action Bar (Bar-less Spatial Writing Studio)
        if (isCompactScreen) {
            CompactScreenplayTopBar(
                uiState = uiState,
                actions = actions,
                onOpenTitlePage = onOpenTitlePage,
                onOpenFileExport = onOpenFileExport,
                onSaveProject = onSaveProject
            )
        } else {
            DesktopScreenplayTopBar(
                uiState = uiState,
                actions = actions,
                onOpenTitlePage = onOpenTitlePage,
                onOpenFileExport = onOpenFileExport,
                onSaveProject = onSaveProject
            )
        }

        // Floating Find & Replace / Character Rename Bar
        AnimatedVisibility(
            visible = uiState.isFindReplaceVisible,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 }
        ) {
            FindReplacePanel(
                uiState = uiState,
                onClose = actions::closeFind,
                onSetMode = actions::setFindMode,
                onUpdateSearchQuery = actions::updateSearchQuery,
                onUpdateReplaceQuery = actions::updateReplaceQuery,
                onToggleMatchCase = actions::toggleMatchCase,
                onToggleMatchWholeWord = actions::toggleMatchWholeWord,
                onFindNext = actions::findNext,
                onFindPrevious = actions::findPrevious,
                onReplaceCurrent = actions::replaceCurrent,
                onReplaceAll = actions::replaceAll,
                onSetRenameOldName = actions::setRenameOldName,
                onSetRenameNewName = actions::setRenameNewName,
                onSetRenameCuesOnly = actions::setRenameCuesOnly,
                onRenameCharacter = actions::renameCharacter
            )
        }

        // US Letter Screenplay Canvas with Zoom & View Mode Support
        val zoom = uiState.zoomLevel
        val isPageView = uiState.viewMode == ScreenplayViewMode.PAGE_VIEW
        val blockBounds = remember { mutableStateMapOf<String, Rect>() }
        var dragAnchorBlockId by remember { mutableStateOf<String?>(null) }
        var pageCardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
        val dualDialogueActions = remember(actions) {
            DualDialogueActions(
                onTextChange = actions::updateBlock,
                onEnter = actions::enterPressed,
                onTab = actions::tabPressed,
                onBackspaceOnEmpty = actions::backspaceEmpty,
                onNavigateUp = actions::navigateUp,
                onNavigateDown = actions::navigateDown,
                onNavigateLeft = actions::navigateLeft,
                onNavigateRight = actions::navigateRight,
                onZoomIn = actions::zoomIn,
                onZoomOut = actions::zoomOut,
                onResetZoom = actions::resetZoom,
                onFocused = actions::focusBlock,
                onToggleHighlight = actions::toggleBlockHighlight,
                onExpandHighlightDown = actions::expandHighlightDown,
                onExpandHighlightUp = actions::expandHighlightUp,
                onClearHighlight = actions::clearHighlight,
                onUpdateNote = actions::setBlockNote,
                onDeleteNote = actions::deleteBlockNote,
                onToggleRevised = actions::toggleBlockRevised,
                onToggleDictation = actions::toggleDictation,
                onCancelDictation = actions::cancelDictation,
                onOpenStoryBible = actions::openStoryBible,
                onToggleCorkboard = actions::toggleCorkboard,
                onInsertTkToken = actions::insertTkToken
            )
        }

        fun findBlockAtY(rootY: Float): String? {
            if (blockBounds.isEmpty() || uiState.blocks.isEmpty()) return null
            for (b in uiState.blocks) {
                val rect = blockBounds[b.id] ?: continue
                if (rootY >= rect.top && rootY <= rect.bottom) {
                    return b.id
                }
            }
            val firstB = uiState.blocks.firstOrNull { blockBounds.containsKey(it.id) }
            if (firstB != null) {
                val firstRect = blockBounds[firstB.id]!!
                if (rootY < firstRect.top) return firstB.id
            }
            val lastB = uiState.blocks.lastOrNull { blockBounds.containsKey(it.id) }
            if (lastB != null) {
                val lastRect = blockBounds[lastB.id]!!
                if (rootY > lastRect.bottom) return lastB.id
            }
            return uiState.blocks
                .filter { blockBounds.containsKey(it.id) }
                .minByOrNull { b ->
                    val rect = blockBounds[b.id]!!
                    if (rootY < rect.top) rect.top - rootY else rootY - rect.bottom
                }?.id
        }

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = (24 * zoom).dp)
        ) {
            if (isPageView && !uiState.firstDraftState.isArcadeLocked) {
                // PAGE VIEW (Default): Distinct US Letter Paper Sheets separated by realistic gaps
                val totalPages = maxOf(1, uiState.pageCount)
                val blocksByPage = remember(uiState.blocks, uiState.blockPageNumbers) {
                    val map = mutableMapOf<Int, MutableList<ScreenplayBlock>>()
                    for (block in uiState.blocks) {
                        val page = uiState.blockPageNumbers[block.id] ?: 1
                        map.getOrPut(page) { mutableListOf() }.add(block)
                    }
                    map
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((24 * zoom).dp),
                    modifier = Modifier.padding(bottom = (48 * zoom).dp)
                ) {
                    for (pageNum in 1..totalPages) {
                        val pageBlocks = blocksByPage[pageNum] ?: emptyList()
                        val isDarkTheme = LocalSeshatDarkTheme.current
                        val pageBgColor = if (uiState.showColoredDraftTint && uiState.currentRevisionColor != RevisionColor.WHITE) {
                            uiState.currentRevisionColor.composeBgTint
                        } else {
                            Color.White
                        }
                        val cardWidth = if (isCompactScreen) (screenWidthDp - 12).coerceAtLeast(280).dp else (780 * zoom).dp
                        Card(
                            colors = CardDefaults.cardColors(containerColor = pageBgColor),
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .width(cardWidth)
                                .defaultMinSize(minHeight = (980 * zoom).dp)
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(2.dp))
                                .onGloballyPositioned { pageCardCoordinates = it }
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                // Left Margin Gutter Drag-to-Highlight Strip
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .width(if (isCompactScreen) 24.dp else (100 * zoom).dp)
                                        .fillMaxHeight()
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .pointerInput(pageNum) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    val rootY = pageCardCoordinates?.localToRoot(offset)?.y ?: 0f
                                                    val targetId = findBlockAtY(rootY)
                                                    if (targetId != null) {
                                                        dragAnchorBlockId = targetId
                                                        actions.toggleBlockHighlight(targetId, false)
                                                    }
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    val anchorId = dragAnchorBlockId
                                                    if (anchorId != null) {
                                                        val rootY = pageCardCoordinates?.localToRoot(change.position)?.y ?: 0f
                                                        val targetId = findBlockAtY(rootY)
                                                        if (targetId != null) {
                                                            actions.highlightBlockRange(anchorId, targetId)
                                                        }
                                                    }
                                                },
                                                onDragEnd = { dragAnchorBlockId = null },
                                                onDragCancel = { dragAnchorBlockId = null }
                                            )
                                        }
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = if (isCompactScreen) (18 * zoom).dp else (110 * zoom).dp, // Left binding margin
                                            end = if (isCompactScreen) (12 * zoom).dp else (70 * zoom).dp,    // Right margin
                                            top = if (isCompactScreen) (24 * zoom).dp else (54 * zoom).dp,    // Top margin
                                            bottom = if (isCompactScreen) (32 * zoom).dp else (64 * zoom).dp  // Bottom margin
                                        )
                                ) {
                                // Top running page number in header (Page 2+, right-aligned "2.")
                                if (pageNum > 1) {
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = (16 * zoom).dp)
                                    ) {
                                        Text(
                                            text = "$pageNum.",
                                            style = TextStyle(
                                                fontFamily = ScreenplayCourierFontFamily,
                                                fontSize = (13 * zoom).sp,
                                                color = Color(0xFF334155),
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                    }
                                }

                                // Blocks on this page (with dual dialogue support)
                                val renderItems = remember(pageBlocks, uiState.blocks) {
                                    groupBlocksForRendering(pageBlocks, uiState.blocks)
                                }
                                renderItems.forEach { item ->
                                    when (item) {
                                        is RenderItem.Single -> {
                                            val block = item.block
                                            val isFocused = block.id == uiState.focusedBlockId
                                            val isHighlighted = uiState.highlightedBlockIds.contains(block.id)
                                            val sNum = uiState.blockSceneNumbers[block.id] ?: block.sceneNumber
                                            val nextBlock = uiState.blocks.getOrNull(item.overallIndex + 1)
                                            ScreenplayBlockItem(
                                                block = block,
                                                config = BlockDisplayConfig(
                                                    nextBlockType = nextBlock?.type,
                                                    isFocused = isFocused,
                                                    isHighlighted = isHighlighted,
                                                    isDualColumn = false,
                                                    cursorTarget = uiState.cursorTarget,
                                                    isFirstBlock = item.overallIndex == 0,
                                                    zoom = zoom,
                                                    showSceneNumbers = uiState.showSceneNumbers,
                                                    sceneNumber = sNum,
                                                    isFindReplaceVisible = uiState.isFindReplaceVisible,
                                                    showWriterNotes = uiState.showWriterNotes,
                                                    isRevisionTrackingEnabled = uiState.isRevisionTrackingEnabled,
                                                    isAiPolishEnabled = isAiPolishEnabled,
                                                    isDictating = uiState.isDictating,
                                                    isCompactScreen = isCompactScreen,
                                                    isKeyboardActive = isKeyboardActive
                                                ),
                                                callbacks = buildBlockCallbacks(
                                                    blockId = block.id,
                                                    actions = actions,
                                                    onDragStart = { id ->
                                                        dragAnchorBlockId = id
                                                        actions.toggleBlockHighlight(id, false)
                                                    },
                                                    onDragMove = { id, rootY ->
                                                        val anchorId = dragAnchorBlockId ?: id
                                                        val targetId = findBlockAtY(rootY)
                                                        if (targetId != null) {
                                                            actions.highlightBlockRange(anchorId, targetId)
                                                        }
                                                    },
                                                    onDragEnd = { dragAnchorBlockId = null },
                                                    onCoordsChanged = { id, coords ->
                                                        blockBounds[id] = coords.boundsInRoot()
                                                    }
                                                )
                                            )
                                        }
                                        is RenderItem.Dual -> {
                                            DualDialogueRow(
                                                dualId = item.dualId,
                                                leftBlocks = item.leftBlocks,
                                                rightBlocks = item.rightBlocks,
                                                uiState = uiState,
                                                zoom = zoom,
                                                showWriterNotes = uiState.showWriterNotes,
                                                isRevisionTrackingEnabled = uiState.isRevisionTrackingEnabled,
                                                isDictating = uiState.isDictating,
                                                isCompactScreen = isCompactScreen,
                                                isKeyboardActive = isKeyboardActive,
                                                actions = dualDialogueActions
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                        // Subtle sheet footer indicator
                        if (pageNum < totalPages) {
                            Text(
                                text = "─── PAGE $pageNum OF $totalPages ───",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = ScreenplayTextMuted.copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            } else {
                // INLINE VIEW: Continuous flow with page break markers
                val isDarkTheme = LocalSeshatDarkTheme.current
                val inlinePageBgColor = if (uiState.showColoredDraftTint && uiState.currentRevisionColor != RevisionColor.WHITE) {
                    uiState.currentRevisionColor.composeBgTint
                } else {
                    Color.White
                }
                val cardWidth = if (isCompactScreen) (screenWidthDp - 12).coerceAtLeast(280).dp else (780 * zoom).dp
                Card(
                    colors = CardDefaults.cardColors(containerColor = inlinePageBgColor),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .width(cardWidth)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(2.dp))
                        .onGloballyPositioned { pageCardCoordinates = it }
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Left Margin Gutter Drag-to-Highlight Strip
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .width(if (isCompactScreen) 24.dp else (100 * zoom).dp)
                                .fillMaxHeight()
                                .pointerHoverIcon(PointerIcon.Hand)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val rootY = pageCardCoordinates?.localToRoot(offset)?.y ?: 0f
                                            val targetId = findBlockAtY(rootY)
                                            if (targetId != null) {
                                                dragAnchorBlockId = targetId
                                                actions.toggleBlockHighlight(targetId, false)
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val anchorId = dragAnchorBlockId
                                            if (anchorId != null) {
                                                val rootY = pageCardCoordinates?.localToRoot(change.position)?.y ?: 0f
                                                val targetId = findBlockAtY(rootY)
                                                if (targetId != null) {
                                                    actions.highlightBlockRange(anchorId, targetId)
                                                }
                                            }
                                        },
                                        onDragEnd = { dragAnchorBlockId = null },
                                        onDragCancel = { dragAnchorBlockId = null }
                                    )
                                }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = if (isCompactScreen) (18 * zoom).dp else (110 * zoom).dp, // Left binding margin
                                    end = if (isCompactScreen) (12 * zoom).dp else (70 * zoom).dp,    // Right margin
                                    top = if (isCompactScreen) (24 * zoom).dp else (54 * zoom).dp,    // Top margin
                                    bottom = if (isCompactScreen) (32 * zoom).dp else (64 * zoom).dp  // Bottom margin
                                )
                        ) {
                        val renderItems = remember(uiState.blocks) {
                            groupBlocksForRendering(uiState.blocks, uiState.blocks)
                        }
                        renderItems.forEach { item ->
                            when (item) {
                                is RenderItem.Single -> {
                                    val block = item.block
                                    if (!uiState.firstDraftState.isArcadeLocked && uiState.pageBreakBeforeBlockIds.contains(block.id)) {
                                        val pageNum = uiState.blockPageNumbers[block.id] ?: 2
                                        InlinePageBreakMarker(pageNumber = pageNum, zoom = zoom)
                                    }
                                    val isFocused = block.id == uiState.focusedBlockId
                                    val isHighlighted = uiState.highlightedBlockIds.contains(block.id)
                                    val sNum = uiState.blockSceneNumbers[block.id] ?: block.sceneNumber
                                    val nextBlock = uiState.blocks.getOrNull(item.overallIndex + 1)
                                    ScreenplayBlockItem(
                                        block = block,
                                        config = BlockDisplayConfig(
                                            nextBlockType = nextBlock?.type,
                                            isFocused = isFocused,
                                            isHighlighted = isHighlighted,
                                            isDualColumn = false,
                                            cursorTarget = uiState.cursorTarget,
                                            isFirstBlock = item.overallIndex == 0,
                                            zoom = zoom,
                                            showSceneNumbers = uiState.showSceneNumbers && !uiState.firstDraftState.isArcadeLocked,
                                            sceneNumber = sNum,
                                            isFindReplaceVisible = uiState.isFindReplaceVisible,
                                            showWriterNotes = uiState.showWriterNotes,
                                            isRevisionTrackingEnabled = uiState.isRevisionTrackingEnabled && !uiState.firstDraftState.isArcadeLocked,
                                            isAiPolishEnabled = isAiPolishEnabled && uiState.firstDraftState.canUseAiDoctor(),
                                            isDictating = uiState.isDictating,
                                            isCompactScreen = isCompactScreen,
                                            isKeyboardActive = isKeyboardActive,
                                            isEditable = uiState.isBlockEditableInFirstDraft(item.overallIndex),
                                            isArcadeMode = uiState.firstDraftState.isArcadeLocked
                                        ),
                                        callbacks = buildBlockCallbacks(
                                            blockId = block.id,
                                            actions = actions,
                                            onDragStart = { id ->
                                                dragAnchorBlockId = id
                                                actions.toggleBlockHighlight(id, false)
                                            },
                                            onDragMove = { id, rootY ->
                                                val anchorId = dragAnchorBlockId ?: id
                                                val targetId = findBlockAtY(rootY)
                                                if (targetId != null) {
                                                    actions.highlightBlockRange(anchorId, targetId)
                                                }
                                            },
                                            onDragEnd = { dragAnchorBlockId = null },
                                            onCoordsChanged = { id, coords ->
                                                blockBounds[id] = coords.boundsInRoot()
                                            }
                                        )
                                    )
                                }
                                is RenderItem.Dual -> {
                                    val firstBlockId = item.leftBlocks.firstOrNull()?.id ?: item.rightBlocks.firstOrNull()?.id
                                    if (!uiState.firstDraftState.isArcadeLocked && firstBlockId != null && uiState.pageBreakBeforeBlockIds.contains(firstBlockId)) {
                                        val pageNum = uiState.blockPageNumbers[firstBlockId] ?: 2
                                        InlinePageBreakMarker(pageNumber = pageNum, zoom = zoom)
                                    }
                                    DualDialogueRow(
                                        dualId = item.dualId,
                                        leftBlocks = item.leftBlocks,
                                        rightBlocks = item.rightBlocks,
                                        uiState = uiState,
                                        zoom = zoom,
                                        showWriterNotes = uiState.showWriterNotes,
                                        isRevisionTrackingEnabled = uiState.isRevisionTrackingEnabled,
                                        isDictating = uiState.isDictating,
                                        isCompactScreen = isCompactScreen,
                                        isKeyboardActive = isKeyboardActive,
                                        actions = dualDialogueActions
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }

        val focusedBlock = uiState.blocks.find { it.id == uiState.focusedBlockId }

        // Scene Heading Smart Bar appears when focused block is SCENE_HEADING
        AnimatedVisibility(
            visible = focusedBlock?.type == BlockType.SCENE_HEADING
        ) {
            focusedBlock?.let { headingBlock ->
                SceneHeadingSmartBar(
                    currentText = headingBlock.text,
                    locations = uiState.locations,
                    onSelectPrefix = { prefix -> actions.applyScenePrefix(headingBlock.id, prefix) },
                    onSelectLocation = { loc -> actions.applySceneLocation(headingBlock.id, loc) },
                    onSelectTime = { time -> actions.applySceneTime(headingBlock.id, time) },
                    onAdvanceToAction = { actions.enterPressed(headingBlock.id) }
                )
            }
        }

        // Character Smart Bar appears when focused block is CHARACTER
        AnimatedVisibility(
            visible = focusedBlock?.type == BlockType.CHARACTER
        ) {
            focusedBlock?.let { charBlock ->
                CharacterSmartBar(
                    currentText = charBlock.text,
                    characters = uiState.characters,
                    onSelectCharacter = { name -> actions.applyCharacterSuggestion(charBlock.id, name) },
                    onToggleExtension = { ext -> actions.toggleExtension(charBlock.id, ext) }
                )
            }
        }

        // Transition Preset Bar appears when focused block is TRANSITION
        AnimatedVisibility(
            visible = focusedBlock?.type == BlockType.TRANSITION
        ) {
            focusedBlock?.let { transBlock ->
                TransitionPresetBar(
                    currentText = transBlock.text,
                    onSelectPreset = { preset -> actions.setTransition(transBlock.id, preset) }
                )
            }
        }

        // Dual Dialogue Contextual Action Bar appears when text/blocks are highlighted
        val clipboardManager = LocalClipboardManager.current
        AnimatedVisibility(
            visible = uiState.highlightedBlockIds.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            DualDialogueActionBar(
                uiState = uiState,
                canMakeDual = actions.isDualDialogueCandidate(uiState.highlightedBlockIds),
                isAlreadyDual = actions.isAlreadyDualDialogue(uiState.highlightedBlockIds),
                onMakeDual = actions::makeDualDialogue,
                onSeparateDual = actions::separateDualDialogue,
                onCopyHighlight = {
                    val selBlocks = uiState.blocks.filter { uiState.highlightedBlockIds.contains(it.id) }
                    val textToCopy = selBlocks.joinToString("\n\n") { it.text }
                    clipboardManager.setText(AnnotatedString(textToCopy))
                },
                onDeleteHighlight = actions::deleteHighlight,
                onClearHighlight = actions::clearHighlight
            )
        }

        // Bottom HUD Floating Element Capsule Dock (Concept Mockup Big Button Dock)
        BottomHud(
            uiState = uiState,
            onChangeBlockType = actions::changeBlockType,
            onInsertNewBlock = actions::insertNewBlock,
            isCompactScreen = isCompactScreen,
            isKeyboardActive = isKeyboardActive,
            onToggleKeyboard = onToggleKeyboard,
            onEnterPressed = actions::enterPressed,
            onBackspaceEmpty = actions::backspaceEmpty,
            onTabPressed = actions::tabPressed,
            onSaveProject = onSaveProject,
            onOpenScriptDoctor = actions::openScriptDoctor,
            onToggleTheme = actions::toggleThemeMode
        )
    }
}

@Composable
private fun SceneHeadingSmartBar(
    currentText: String,
    locations: List<String>,
    onSelectPrefix: (String) -> Unit,
    onSelectLocation: (String) -> Unit,
    onSelectTime: (String) -> Unit,
    onAdvanceToAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parts = remember(currentText) { ScreenplayViewModel.parseSceneHeading(currentText) }
    val trimmed = currentText.trim().uppercase(Locale.US)

    // Determine current mode and suggestions
    val isPrefixMode = parts.prefix.isEmpty() && (trimmed.isEmpty() || ScreenplayViewModel.STANDARD_PREFIXES.any { it.startsWith(trimmed) } || trimmed.length <= 4)
    val isTimeMode = parts.hasHyphen
    val isLocationMode = !isPrefixMode && !isTimeMode && parts.prefix != "OVER BLACK"

    Surface(
        color = ObsidianSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Prominent Action Button: advance immediately to Action line
            Surface(
                color = ElectrumGold,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable { onAdvanceToAction() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "↵ ACTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ObsidianBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
            when {
                isPrefixMode -> {
                    Text(
                        text = "PREFIX",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    val prefixSuggestions = ScreenplayViewModel.STANDARD_PREFIXES.filter {
                        trimmed.isEmpty() || it.startsWith(trimmed)
                    }
                    prefixSuggestions.forEach { prefix ->
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SluglineBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onSelectPrefix(prefix) }
                        ) {
                            Text(
                                text = prefix,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SluglineBlue,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                isLocationMode -> {
                    Text(
                        text = "LOCATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    val partialLoc = parts.location.trim()
                    val candidateLocations = (locations + ScreenplayViewModel.DEFAULT_LOCATIONS).distinct()
                    val filteredLocations = candidateLocations.filter {
                        partialLoc.isEmpty() || it.startsWith(partialLoc, ignoreCase = true)
                    }.take(8)

                    filteredLocations.forEach { loc ->
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SluglineBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onSelectLocation(loc) }
                        ) {
                            Text(
                                text = loc,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SluglineBlue,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                isTimeMode -> {
                    Text(
                        text = "TIME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    val partialTime = (parts.timeOfDay ?: "").trim()
                    val filteredTimes = ScreenplayViewModel.STANDARD_TIME_TAGS.filter {
                        partialTime.isEmpty() || it.startsWith(partialTime, ignoreCase = true)
                    }

                    filteredTimes.forEach { timeTag ->
                        val isSelected = partialTime.equals(timeTag, ignoreCase = true)
                        Surface(
                            color = if (isSelected) SluglineBlue.copy(alpha = 0.2f) else ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) SluglineBlue else SluglineBlue.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable { onSelectTime(timeTag) }
                        ) {
                            Text(
                                text = timeTag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) SluglineBlue else ScreenplayTextDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterSmartBar(
    currentText: String,
    characters: List<String>,
    onSelectCharacter: (String) -> Unit,
    onToggleExtension: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanCurrent = remember(currentText) {
        ScreenplayViewModel.cleanCharacterName(currentText).uppercase(Locale.US).trim()
    }

    val suggestedCharacters = remember(cleanCurrent, characters) {
        characters.filter {
            cleanCurrent.isEmpty() || (it.startsWith(cleanCurrent, ignoreCase = true) && !it.equals(cleanCurrent, ignoreCase = true))
        }.take(6)
    }

    Surface(
        color = ObsidianSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            if (suggestedCharacters.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "CHARACTERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )

                    suggestedCharacters.forEach { charName ->
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CharacterGold.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onSelectCharacter(charName) }
                        ) {
                            Text(
                                text = charName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CharacterGold,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "EXTENSIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                )

                val extensions = listOf("(V.O.)", "(O.S.)", "(CONT'D)", "(O.C.)")
                extensions.forEach { ext ->
                    val isActive = currentText.contains(ext)
                    Surface(
                        color = if (isActive) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isActive) ElectrumGold else ObsidianSurfaceVariant
                        ),
                        modifier = Modifier.clickable { onToggleExtension(ext) }
                    ) {
                        Text(
                            text = ext,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isActive) ElectrumGold else ScreenplayTextMuted,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitionPresetBar(
    currentText: String,
    onSelectPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "TRANSITIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ScreenplayTextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            )

            val presets = listOf("CUT TO:", "DISSOLVE TO:", "SMASH CUT TO:", "FADE OUT.", "BACK TO:")
            presets.forEach { preset ->
                val isSelected = currentText.equals(preset, ignoreCase = true)
                Surface(
                    color = if (isSelected) TransitionPurple.copy(alpha = 0.2f) else ObsidianBlack,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) TransitionPurple else ObsidianSurfaceVariant
                    ),
                    modifier = Modifier.clickable { onSelectPreset(preset) }
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) TransitionPurple else ScreenplayTextDark,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomHud(
    uiState: ScreenplayUiState,
    onChangeBlockType: (String, BlockType) -> Unit,
    onInsertNewBlock: (String, BlockType) -> Unit,
    isCompactScreen: Boolean = false,
    isKeyboardActive: Boolean = false,
    onToggleKeyboard: () -> Unit = {},
    onEnterPressed: (String) -> Unit = {},
    onBackspaceEmpty: (String) -> Unit = {},
    onTabPressed: (String) -> Unit = {},
    onSaveProject: () -> Unit = {},
    onOpenScriptDoctor: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    val currentBlock = uiState.blocks.find { it.id == uiState.focusedBlockId }
        ?: uiState.blocks.firstOrNull()
    val currentType = currentBlock?.type ?: BlockType.SCENE_HEADING
    var dropdownExpanded by remember { mutableStateOf(false) }

    if (isCompactScreen) {
        Column(
            modifier = modifier
                .navigationBarsPadding()
                .imePadding()
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Tier 1: Scrollable touch element pills (Sleek Modern Capsules)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 3.dp)
            ) {
                BlockType.forFormat(uiState.projectFormat).forEach { type ->
                    val isSelected = type == currentType
                    val blockInteraction = remember { MutableInteractionSource() }
                    val isBlockHovered by blockInteraction.collectIsHoveredAsState()
                    val isBlockPressed by blockInteraction.collectIsPressedAsState()
                    val isBlockLit = isSelected || isBlockHovered || isBlockPressed

                    val iconLabel = when (type) {
                        BlockType.SCENE_HEADING -> "🎬 Scene"
                        BlockType.ACTION -> "🏃 Action"
                        BlockType.CHARACTER -> "👤 Character"
                        BlockType.PARENTHETICAL -> "( ) Paren"
                        BlockType.DIALOGUE -> "💬 Dialogue"
                        BlockType.TRANSITION -> "⚡ Trans"
                        BlockType.STAGE_ACT_SCENE -> "🎭 Act/Scene"
                        BlockType.STAGE_DIRECTION -> "📜 Direction"
                        BlockType.STAGE_CURTAIN -> "🎬 Curtain"
                        BlockType.COMIC_PAGE -> "📄 Page"
                        BlockType.COMIC_PANEL -> "🖼️ Panel"
                        BlockType.COMIC_ART_NOTE -> "🎨 Art Note"
                        BlockType.COMIC_BALLOON -> "💬 Balloon"
                        BlockType.COMIC_CAPTION -> "🏷️ Caption"
                        BlockType.COMIC_SFX -> "⚡ SFX"
                    }
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = if (isBlockLit) 8.dp else 4.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = if (isBlockLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
                            )
                            .then(
                                if (isBlockLit) {
                                    Modifier.neonBloom(
                                        color = ScribeStarNeonRed,
                                        cornerRadius = 14.dp,
                                        glowRadius = if (isSelected) 7.dp else 4.dp,
                                        strokeWidth = 1.4.dp,
                                        outerAlpha = if (isSelected) 0.35f else 0.20f,
                                        midAlpha = if (isSelected) 0.65f else 0.40f
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(opticalPillBrush(isLit = isBlockLit, isPrimary = isSelected, isDark = isDark))
                            .border(
                                prismaticGlassBorder(
                                    width = if (isBlockLit) 1.5.dp else 1.dp,
                                    isLit = isBlockLit,
                                    isDark = isDark
                                ),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = blockInteraction,
                                indication = null
                            ) {
                                currentBlock?.let { onChangeBlockType(it.id, type) }
                            }
                    ) {
                        Text(
                            text = iconLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (isBlockLit) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)),
                                fontWeight = if (isBlockLit) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }

                // SESHAT AI Star Medallion Button (Slide 3)
                Box(
                    modifier = Modifier
                        .neonBloom(
                            color = ScribeStarNeonRed,
                            cornerRadius = 14.dp,
                            glowRadius = 7.dp,
                            strokeWidth = 1.3.dp,
                            outerAlpha = 0.25f,
                            midAlpha = 0.48f
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF2E0D14),
                                    Color(0xFF16060A)
                                )
                            )
                        )
                        .border(BorderStroke(1.4.dp, ScribeStarNeonRed), RoundedCornerShape(14.dp))
                        .clickable { onOpenScriptDoctor() }
                ) {
                    Text(
                        text = "🌟 SESHAT AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }

            // Tier 2: Controls (Dictation, Keyboard Toggle, Tab, Enter, Del, Save, Metrics)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(end = 6.dp)
                ) {
                    // Dictation indicator
                    Surface(
                        color = if (uiState.isDictating) Color(0xFFD32F2F).copy(alpha = 0.25f) else Color(0xFF0E131C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (uiState.isDictating) Color(0xFFEF5350) else Color(0xFF334155))
                    ) {
                        Text(
                            text = if (uiState.isDictating) "🔴 Recording" else "🎙️ Ready",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (uiState.isDictating) Color(0xFFEF5350) else Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Keyboard Toggle Button
                    Surface(
                        color = if (isKeyboardActive) ScribeStarNeonRed.copy(alpha = 0.25f) else Color(0xFF0E131C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isKeyboardActive) ScribeStarNeonRed else Color(0xFF334155)),
                        modifier = Modifier.clickable(onClick = onToggleKeyboard)
                    ) {
                        Text(
                            text = if (isKeyboardActive) "⌨️ On" else "⌨️ Off",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (isKeyboardActive) ScribeStarNeonRed else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Tab Button (cycles / advances elements)
                    Surface(
                        color = Color(0xFF0E131C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.clickable {
                            currentBlock?.let { onTabPressed(it.id) }
                        }
                    ) {
                        Text(
                            text = "⇥ Tab",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Enter Button (Context-aware: ↵ Action from Heading, ↵ Dialogue from Char, etc.)
                    val enterLabel = when (uiState.projectFormat) {
                        com.seshat.writer.library.ProjectFormat.STAGE_PLAY -> when (currentType) {
                            BlockType.STAGE_ACT_SCENE -> "↵ Direction"
                            BlockType.STAGE_DIRECTION -> "↵ Direction"
                            BlockType.CHARACTER -> "↵ Dialogue"
                            BlockType.PARENTHETICAL -> "↵ Dialogue"
                            BlockType.DIALOGUE -> "↵ Character"
                            BlockType.STAGE_CURTAIN -> "↵ Act/Scene"
                            else -> "↵ Enter"
                        }
                        com.seshat.writer.library.ProjectFormat.COMIC_BOOK -> when (currentType) {
                            BlockType.COMIC_PAGE -> "↵ Panel 1"
                            BlockType.COMIC_PANEL -> "↵ Art Note"
                            BlockType.COMIC_ART_NOTE -> "↵ Art Note"
                            BlockType.CHARACTER -> "↵ Balloon"
                            BlockType.COMIC_BALLOON -> "↵ Character"
                            BlockType.COMIC_CAPTION -> "↵ Balloon"
                            BlockType.COMIC_SFX -> "↵ Next Panel"
                            else -> "↵ Enter"
                        }
                        else -> when (currentType) {
                            BlockType.SCENE_HEADING -> "↵ Action"
                            BlockType.CHARACTER -> "↵ Dialogue"
                            BlockType.DIALOGUE -> "↵ Action"
                            BlockType.ACTION -> "↵ Action"
                            BlockType.PARENTHETICAL -> "↵ Dialogue"
                            BlockType.TRANSITION -> "↵ Scene"
                            else -> "↵ Enter"
                        }
                    }
                    val isHeadingCurrent = currentType == BlockType.SCENE_HEADING || currentType == BlockType.STAGE_ACT_SCENE || currentType == BlockType.COMIC_PAGE

                    Surface(
                        color = if (isHeadingCurrent) Color(0xFF1E283A) else Color(0xFF0E131C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isHeadingCurrent) Color(0xFF38BDF8) else Color(0xFF334155)),
                        modifier = Modifier.clickable {
                            currentBlock?.let { onEnterPressed(it.id) }
                        }
                    ) {
                        Text(
                            text = enterLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (isHeadingCurrent) Color(0xFF38BDF8) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Del Button
                    Surface(
                        color = Color(0xFF0E131C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.clickable {
                            currentBlock?.let { onBackspaceEmpty(it.id) }
                        }
                    ) {
                        Text(
                            text = "⌫ Del",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // 1-Tap Save Button
                    Surface(
                        color = ScribeStarNeonRed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onSaveProject() }
                    ) {
                        Text(
                            text = "💾 Save",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                // Compact metrics
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(opticalPillBrush(isDark = isDark))
                        .border(prismaticGlassBorder(width = 0.8.dp, isDark = isDark), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "P.${uiState.pageCount} • ${uiState.wordCount}w",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                FloatingThemeTogglePill(
                    isDark = isDark,
                    onToggleTheme = onToggleTheme
                )
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Left side: Modern Floating Element Capsule Dock (Matching concept mockup)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .horizontalScroll(rememberScrollState())
            ) {
                BlockType.forFormat(uiState.projectFormat).forEach { type ->
                    val isSelected = type == currentType
                    val blockInteraction = remember { MutableInteractionSource() }
                    val isHovered by blockInteraction.collectIsHoveredAsState()
                    val isPressed by blockInteraction.collectIsPressedAsState()
                    val isBlockLit = isSelected || isHovered || isPressed

                    val iconLabel = when (type) {
                        BlockType.SCENE_HEADING -> "🎬 Scene"
                        BlockType.ACTION -> "🏃 Action"
                        BlockType.CHARACTER -> "👤 Character"
                        BlockType.PARENTHETICAL -> "( ) Paren"
                        BlockType.DIALOGUE -> "💬 Dialogue"
                        BlockType.TRANSITION -> "⚡ Trans"
                        BlockType.STAGE_ACT_SCENE -> "🎭 Act/Scene"
                        BlockType.STAGE_DIRECTION -> "📜 Direction"
                        BlockType.STAGE_CURTAIN -> "🎬 Curtain"
                        BlockType.COMIC_PAGE -> "📄 Page"
                        BlockType.COMIC_PANEL -> "🖼️ Panel"
                        BlockType.COMIC_ART_NOTE -> "🎨 Art Note"
                        BlockType.COMIC_BALLOON -> "💬 Balloon"
                        BlockType.COMIC_CAPTION -> "🏷️ Caption"
                        BlockType.COMIC_SFX -> "⚡ SFX"
                    }
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = if (isBlockLit) 10.dp else 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = if (isBlockLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
                            )
                            .then(
                                if (isBlockLit) {
                                    Modifier.neonBloom(
                                        color = ScribeStarNeonRed,
                                        cornerRadius = 16.dp,
                                        glowRadius = if (isSelected) 8.dp else 5.dp,
                                        strokeWidth = 1.4.dp,
                                        outerAlpha = if (isSelected) 0.35f else 0.20f,
                                        midAlpha = if (isSelected) 0.65f else 0.40f
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(opticalPillBrush(isLit = isBlockLit, isPrimary = isSelected, isDark = isDark))
                            .border(
                                prismaticGlassBorder(
                                    width = if (isBlockLit) 1.5.dp else 1.dp,
                                    isLit = isBlockLit,
                                    isDark = isDark
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = blockInteraction,
                                indication = null
                            ) {
                                currentBlock?.let { onChangeBlockType(it.id, type) }
                            }
                    ) {
                        Text(
                            text = iconLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                color = if (isBlockLit) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)),
                                fontWeight = if (isBlockLit) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp)
                        )
                    }
                }

                // SESHAT AI Star Medallion Button (Slide 3)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = ScribeStarNeonRed
                        )
                        .neonBloom(
                            color = ScribeStarNeonRed,
                            cornerRadius = 16.dp,
                            glowRadius = 7.dp,
                            strokeWidth = 1.3.dp,
                            outerAlpha = 0.25f,
                            midAlpha = 0.48f
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF2E0D14),
                                    Color(0xFF16060A)
                                )
                            )
                        )
                        .border(BorderStroke(1.4.dp, ScribeStarNeonRed), RoundedCornerShape(16.dp))
                        .clickable { onOpenScriptDoctor() }
                ) {
                    Text(
                        text = "🌟 SESHAT AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                // Quick "⇥ Tab" button in footer
                val tabInteraction = remember { MutableInteractionSource() }
                val isTabHovered by tabInteraction.collectIsHoveredAsState()
                val isTabPressed by tabInteraction.collectIsPressedAsState()
                val isTabLit = isTabHovered || isTabPressed

                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = if (isTabLit) 8.dp else 5.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = if (isTabLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
                        )
                        .then(
                            if (isTabLit) {
                                Modifier.neonBloom(
                                    color = ScribeStarNeonRed,
                                    cornerRadius = 14.dp,
                                    glowRadius = 5.dp,
                                    strokeWidth = 1.2.dp,
                                    outerAlpha = 0.25f,
                                    midAlpha = 0.45f
                                )
                            } else Modifier
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isTabLit) Brush.verticalGradient(listOf(Color(0x65450E1A), Color(0x7524050D)))
                            else if (isDark) Brush.verticalGradient(listOf(Color(0x301E293B), Color(0x400F172A)))
                            else Brush.verticalGradient(listOf(Color(0xF0FFFFFF), Color(0xE8F1F5F9)))
                        )
                        .border(
                            BorderStroke(
                                if (isTabLit) 1.4.dp else 1.dp,
                                if (isTabLit) ScribeStarNeonRed else if (isDark) ScribeStarNeonRed.copy(alpha = 0.35f) else Color(0xFFCBD5E1)
                            ),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = tabInteraction,
                            indication = null
                        ) {
                            currentBlock?.let { onTabPressed(it.id) }
                        }
                ) {
                    Text(
                        text = "⇥ Tab",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isTabLit) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                    )
                }

                // Quick "💾 Save" button in footer
                val saveInteraction = remember { MutableInteractionSource() }
                val isSaveHovered by saveInteraction.collectIsHoveredAsState()
                val isSavePressed by saveInteraction.collectIsPressedAsState()
                val isSaveLit = isSaveHovered || isSavePressed

                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = if (isSaveLit) 10.dp else 6.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = ScribeStarNeonRed
                        )
                        .then(
                            if (isSaveLit) {
                                Modifier.neonBloom(
                                    color = ScribeStarNeonRed,
                                    cornerRadius = 14.dp,
                                    glowRadius = 6.dp,
                                    strokeWidth = 1.3.dp,
                                    outerAlpha = 0.30f,
                                    midAlpha = 0.55f
                                )
                            } else Modifier
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSaveLit) Brush.verticalGradient(listOf(Color(0x805C1322), Color(0x90380814)))
                            else if (isDark) Brush.verticalGradient(listOf(Color(0x403A101A), Color(0x501C060E)))
                            else Brush.verticalGradient(listOf(Color(0xFFFFECEF), Color(0xFFFFE4E6)))
                        )
                        .border(
                            BorderStroke(
                                if (isSaveLit) 1.5.dp else 1.dp,
                                ScribeStarNeonRed
                            ),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = saveInteraction,
                            indication = null
                        ) { onSaveProject() }
                ) {
                    Text(
                        text = "💾 Save",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isSaveLit || isDark) Color.White else Color(0xFF991B1B),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                    )
                }
            }

            // Right side: Floating Telemetry Glass Pill & Floating Device Environment Chip & Theme Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(opticalPillBrush(isDark = isDark))
                        .border(prismaticGlassBorder(width = 1.dp, isDark = isDark), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!uiState.firstDraftState.isArcadeLocked) {
                            Text(
                                text = "${uiState.pageCount} ${if (uiState.pageCount == 1) "Page" else "Pages"} (~${uiState.pageCount}m)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 11.5.sp
                                )
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall.copy(color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                            )
                        }
                        Text(
                            text = "${uiState.wordCount} Words",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                fontSize = 11.5.sp
                            )
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall.copy(color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                        )
                        Text(
                            text = "${uiState.sceneCount} ${if (uiState.sceneCount == 1) "Scene" else "Scenes"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 11.5.sp
                            )
                        )
                        Text(
                            text = if (uiState.firstDraftState.isArcadeLocked) "• Arcade Scroll" else "• Courier 12pt",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                val context = LocalContext.current
                val isGooglebook = remember { DeviceEnvironment.isGooglebook(context) }
                val deviceLabel = remember { DeviceEnvironment.getDeviceNavLabel(context) }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(opticalPillBrush(isDark = isDark))
                        .border(
                            prismaticGlassBorder(width = 1.dp, accentColor = Color(0xFF38BDF8), isDark = isDark),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = deviceLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                FloatingThemeTogglePill(
                    isDark = isDark,
                    onToggleTheme = onToggleTheme
                )
            }
        }
    }
}

@Composable
private fun InlinePageBreakMarker(
    pageNumber: Int,
    zoom: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = (18 * zoom).dp)
    ) {
        // Subtle divider with centered Page badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                color = ElectrumGold.copy(alpha = 0.35f),
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.45f)),
                modifier = Modifier.padding(horizontal = (12 * zoom).dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = (10 * zoom).dp, vertical = (4 * zoom).dp)
                ) {
                    Text(
                        text = "─── PAGE $pageNumber ───",
                        style = TextStyle(
                            fontFamily = ScreenplayCourierFontFamily,
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = (11 * zoom).sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
            HorizontalDivider(
                color = ElectrumGold.copy(alpha = 0.35f),
                modifier = Modifier.weight(1f)
            )
        }

        // Industry standard screenplay page number in header (right-aligned, e.g. "2.")
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = (8 * zoom).dp, end = (8 * zoom).dp)
        ) {
            Text(
                text = "$pageNumber.",
                style = TextStyle(
                    fontFamily = ScreenplayCourierFontFamily,
                    fontSize = (13 * zoom).sp,
                    color = ScreenplayTextDark.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun DualDialogueRow(
    dualId: String,
    leftBlocks: List<ScreenplayBlock>,
    rightBlocks: List<ScreenplayBlock>,
    uiState: ScreenplayUiState,
    zoom: Float = 1.0f,
    showWriterNotes: Boolean = true,
    isRevisionTrackingEnabled: Boolean = false,
    isDictating: Boolean = false,
    isCompactScreen: Boolean = false,
    isKeyboardActive: Boolean = false,
    actions: DualDialogueActions = DualDialogueActions(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = (14 * zoom).dp)
    ) {
        // Dual Dialogue banner ribbon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = (8 * zoom).dp)
                .clickable {
                    val firstId = leftBlocks.firstOrNull()?.id ?: rightBlocks.firstOrNull()?.id
                    if (firstId != null) actions.onToggleHighlight(firstId, false)
                }
        ) {
            Text(
                text = "─── DUAL DIALOGUE ───",
                style = TextStyle(
                    fontFamily = ScreenplayCourierFontFamily,
                    fontSize = (10 * zoom).sp,
                    color = ElectrumGold.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isCompactScreen) (8 * zoom).dp else (36 * zoom).dp)
        ) {
            // Left Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0x06000000), RoundedCornerShape(6.dp))
                    .border(BorderStroke(0.5.dp, Color(0x15000000)), RoundedCornerShape(6.dp))
                    .padding(horizontal = if (isCompactScreen) (4 * zoom).dp else (10 * zoom).dp, vertical = (8 * zoom).dp)
            ) {
                leftBlocks.forEachIndexed { idx, block ->
                    key(block.id) {
                        val isFocused = block.id == uiState.focusedBlockId
                        val isHighlighted = uiState.highlightedBlockIds.contains(block.id)
                        val nextType = leftBlocks.getOrNull(idx + 1)?.type
                        ScreenplayBlockItem(
                            block = block,
                            config = BlockDisplayConfig(
                                nextBlockType = nextType,
                                isFocused = isFocused,
                                isHighlighted = isHighlighted,
                                isDualColumn = true,
                                cursorTarget = uiState.cursorTarget,
                                isFirstBlock = false,
                                zoom = zoom,
                                showWriterNotes = showWriterNotes,
                                isRevisionTrackingEnabled = isRevisionTrackingEnabled,
                                isDictating = isDictating,
                                isCompactScreen = isCompactScreen,
                                isKeyboardActive = isKeyboardActive
                            ),
                            callbacks = buildDualBlockCallbacks(block.id, actions)
                        )
                    }
                }
            }

            // Right Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0x06000000), RoundedCornerShape(6.dp))
                    .border(BorderStroke(0.5.dp, Color(0x15000000)), RoundedCornerShape(6.dp))
                    .padding(horizontal = if (isCompactScreen) (4 * zoom).dp else (10 * zoom).dp, vertical = (8 * zoom).dp)
            ) {
                rightBlocks.forEachIndexed { idx, block ->
                    key(block.id) {
                        val isFocused = block.id == uiState.focusedBlockId
                        val isHighlighted = uiState.highlightedBlockIds.contains(block.id)
                        val nextType = rightBlocks.getOrNull(idx + 1)?.type
                        ScreenplayBlockItem(
                            block = block,
                            config = BlockDisplayConfig(
                                nextBlockType = nextType,
                                isFocused = isFocused,
                                isHighlighted = isHighlighted,
                                isDualColumn = true,
                                cursorTarget = uiState.cursorTarget,
                                isFirstBlock = false,
                                zoom = zoom,
                                showWriterNotes = showWriterNotes,
                                isRevisionTrackingEnabled = isRevisionTrackingEnabled,
                                isDictating = isDictating,
                                isCompactScreen = isCompactScreen,
                                isKeyboardActive = isKeyboardActive
                            ),
                            callbacks = buildDualBlockCallbacks(block.id, actions)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DualDialogueActionBar(
    uiState: ScreenplayUiState,
    canMakeDual: Boolean,
    isAlreadyDual: Boolean,
    onMakeDual: () -> Unit,
    onSeparateDual: () -> Unit,
    onCopyHighlight: () -> Unit = {},
    onDeleteHighlight: () -> Unit = {},
    onClearHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, if (canMakeDual) ElectrumGold else ObsidianSurfaceVariant),
        shadowElevation = 10.dp,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canMakeDual) {
                    Surface(
                        color = ElectrumGold,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onMakeDual() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⚡ DUAL DIALOGUE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = ObsidianBlack,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                } else if (isAlreadyDual) {
                    Surface(
                        color = ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onSeparateDual() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⇋ SEPARATE DIALOGUE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = ElectrumGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }

                // Copy Selected Blocks
                Surface(
                    color = ObsidianSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { onCopyHighlight() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "📋 COPY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectrumGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Delete Selected Blocks
                Surface(
                    color = Color(0xFFC0392B).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE74C3C).copy(alpha = 0.45f)),
                    modifier = Modifier.clickable { onDeleteHighlight() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "🗑️ DELETE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Text(
                    text = "${uiState.highlightedBlockIds.size} blocks highlighted",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (canMakeDual) ElectrumGold else ScreenplayTextMuted,
                        fontWeight = if (canMakeDual) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }

            IconButton(
                onClick = onClearHighlight,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Highlight",
                    tint = ScreenplayTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun WriterNoteCard(
    noteText: String,
    zoom: Float,
    startIndent: androidx.compose.ui.unit.Dp,
    endIndent: androidx.compose.ui.unit.Dp,
    onUpdateNote: (String) -> Unit,
    onDeleteNote: () -> Unit
) {
    Surface(
        color = Color(0xFFFFFBEA),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = startIndent + (12 * zoom).dp,
                end = endIndent + (12 * zoom).dp,
                top = (2 * zoom).dp,
                bottom = (8 * zoom).dp
            )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📝 WRITER'S NOTE",
                    style = TextStyle(
                        fontSize = (10 * zoom).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        letterSpacing = 0.5.sp
                    )
                )
                IconButton(
                    onClick = onDeleteNote,
                    modifier = Modifier.size((18 * zoom).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Note",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size((12 * zoom).dp)
                    )
                }
            }

            BasicTextField(
                value = noteText,
                onValueChange = onUpdateNote,
                textStyle = TextStyle(
                    fontFamily = ScreenplayCourierFontFamily,
                    fontSize = (12 * zoom).sp,
                    color = Color(0xFF1F2937),
                    lineHeight = (17 * zoom).sp
                ),
                cursorBrush = SolidColor(Color(0xFFB45309)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

private fun getBlockPlaceholder(type: BlockType): String = when (type) {
    BlockType.SCENE_HEADING -> "INT. / EXT. LOCATION - TIME"
    BlockType.ACTION -> "Action..."
    BlockType.CHARACTER -> "CHARACTER NAME"
    BlockType.PARENTHETICAL -> "(parenthetical)"
    BlockType.DIALOGUE -> "Dialogue..."
    BlockType.TRANSITION -> "CUT TO:"
    BlockType.STAGE_ACT_SCENE -> "ACT I, SCENE 1"
    BlockType.STAGE_DIRECTION -> "[Stage direction, setting, entrances & exits...]"
    BlockType.STAGE_CURTAIN -> "BLACKOUT / CURTAIN"
    BlockType.COMIC_PAGE -> "PAGE 1"
    BlockType.COMIC_PANEL -> "PANEL 1 (WIDE)"
    BlockType.COMIC_ART_NOTE -> "Art direction / visual description..."
    BlockType.COMIC_BALLOON -> "Balloon text / speech..."
    BlockType.COMIC_CAPTION -> "CAPTION: Text..."
    BlockType.COMIC_SFX -> "SFX: KRA-KATOOM!"
}

private fun handleBlockPreviewKeyEvent(
    keyEvent: KeyEvent,
    block: ScreenplayBlock,
    textFieldValue: TextFieldValue,
    textLayoutResult: TextLayoutResult?,
    isSingleLine: Boolean,
    config: BlockDisplayConfig,
    callbacks: BlockCallbacks
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    val isCmdOrCtrl = keyEvent.isCtrlPressed || keyEvent.isMetaPressed

    if (isCmdOrCtrl) {
        when (keyEvent.key) {
            Key.D -> { callbacks.onToggleDictation(); return true }
            Key.N -> { callbacks.onUpdateNote(block.note ?: ""); return true }
            Key.Plus, Key.Equals -> { callbacks.onZoomIn(); return true }
            Key.Minus -> { callbacks.onZoomOut(); return true }
            Key.Zero -> { callbacks.onResetZoom(); return true }
            Key.F -> { callbacks.onOpenFind(false); return true }
            Key.H -> { callbacks.onOpenFind(true); return true }
            Key.B -> { callbacks.onOpenStoryBible(); return true }
            Key.O -> { callbacks.onToggleCorkboard(); return true }
            Key.K -> { callbacks.onInsertTkToken(); return true }
            Key.X -> { if (!config.isEditable) return true }
        }
    }
    if (!config.isEditable && (keyEvent.key == Key.Backspace || keyEvent.key == Key.Delete || keyEvent.key == Key.Enter)) {
        return true
    }
    if (keyEvent.key == Key.F10 || keyEvent.key == Key.F5 || (isCmdOrCtrl && keyEvent.key == Key.D)) {
        callbacks.onToggleDictation()
        return true
    }
    if (keyEvent.isShiftPressed) {
        if (keyEvent.key == Key.DirectionDown) {
            callbacks.onExpandHighlightDown()
            return true
        } else if (keyEvent.key == Key.DirectionUp) {
            callbacks.onExpandHighlightUp()
            return true
        }
    }
    if (keyEvent.key == Key.Escape) {
        if (config.isDictating) {
            callbacks.onCancelDictation()
            return true
        }
        if (config.isFindReplaceVisible) {
            callbacks.onCloseFind()
            return true
        }
        if (config.isHighlighted) {
            callbacks.onClearHighlight()
            return true
        }
    }
    return when (keyEvent.key) {
        Key.Tab -> {
            callbacks.onTab()
            true
        }
        Key.Enter -> {
            callbacks.onEnter()
            true
        }
        Key.Backspace -> {
            if (block.text.isEmpty()) {
                callbacks.onBackspaceOnEmpty()
                true
            } else false
        }
        Key.DirectionLeft -> {
            if (textFieldValue.selection.collapsed && textFieldValue.selection.start == 0) {
                callbacks.onNavigateLeft()
                true
            } else false
        }
        Key.DirectionRight -> {
            if (textFieldValue.selection.collapsed && textFieldValue.selection.end == block.text.length) {
                callbacks.onNavigateRight()
                true
            } else false
        }
        Key.DirectionUp -> {
            val cursorOffset = textFieldValue.selection.end
            if (isSingleLine) {
                callbacks.onNavigateUp(cursorOffset)
                true
            } else {
                val layout = textLayoutResult
                if (layout != null) {
                    val currentLine = layout.getLineForOffset(cursorOffset)
                    if (currentLine == 0) {
                        val lineStart = layout.getLineStart(0)
                        val col = cursorOffset - lineStart
                        callbacks.onNavigateUp(col)
                        true
                    } else false
                } else {
                    val hasNewlineBefore = block.text.substring(0, cursorOffset).contains('\n')
                    if (!hasNewlineBefore) {
                        callbacks.onNavigateUp(cursorOffset)
                        true
                    } else false
                }
            }
        }
        Key.DirectionDown -> {
            val cursorOffset = textFieldValue.selection.end
            if (isSingleLine) {
                callbacks.onNavigateDown(cursorOffset)
                true
            } else {
                val layout = textLayoutResult
                if (layout != null) {
                    val currentLine = layout.getLineForOffset(cursorOffset)
                    val lastLine = layout.lineCount - 1
                    if (currentLine >= lastLine) {
                        val lineStart = layout.getLineStart(currentLine)
                        val col = cursorOffset - lineStart
                        callbacks.onNavigateDown(col)
                        true
                    } else false
                } else {
                    val hasNewlineAfter = block.text.substring(cursorOffset).contains('\n')
                    if (!hasNewlineAfter) {
                        callbacks.onNavigateDown(cursorOffset)
                        true
                    } else false
                }
            }
        }
        else -> false
    }
}

@Composable
private fun BlockGutterHandle(
    blockId: String,
    note: String?,
    isHighlighted: Boolean,
    showWriterNotes: Boolean,
    zoom: Float,
    blockCoordinates: LayoutCoordinates?,
    callbacks: BlockCallbacks
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = (6 * zoom).dp, end = (4 * zoom).dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size((14 * zoom).dp)
                .clip(RoundedCornerShape(3.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .background(if (isHighlighted) ElectrumGold else Color(0x15000000))
                .border(
                    BorderStroke(1.dp, if (isHighlighted) ElectrumGold else Color(0x25000000)),
                    RoundedCornerShape(3.dp)
                )
                .clickable { callbacks.onToggleHighlight(false) }
                .pointerInput(blockId) {
                    detectDragGestures(
                        onDragStart = { callbacks.onDragHighlightStart() },
                        onDrag = { change, _ ->
                            change.consume()
                            val rootY = blockCoordinates?.localToRoot(change.position)?.y ?: 0f
                            callbacks.onDragHighlightMove(rootY)
                        },
                        onDragEnd = { callbacks.onDragHighlightEnd() },
                        onDragCancel = { callbacks.onDragHighlightEnd() }
                    )
                }
        ) {
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .size((6 * zoom).dp)
                        .background(ObsidianBlack, RoundedCornerShape(1.dp))
                )
            }
        }

        if (!showWriterNotes && !note.isNullOrBlank()) {
            Spacer(modifier = Modifier.width((3 * zoom).dp))
            Text(
                text = "📝",
                fontSize = (10 * zoom).sp,
                modifier = Modifier.clickable { callbacks.onUpdateNote(note) }
            )
        }
    }
}

@Composable
private fun BlockRevisionMarker(
    isRevised: Boolean,
    revisionColor: String?,
    isRevisionTrackingEnabled: Boolean,
    isFocused: Boolean,
    isDualColumn: Boolean,
    topPadding: Dp,
    zoom: Float,
    onToggleRevised: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isRevised) {
        val revColorObj = RevisionColor.fromTag(revisionColor) ?: RevisionColor.BLUE
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .then(if (!isDualColumn) Modifier.offset(x = (16 * zoom).dp) else Modifier.padding(end = (4 * zoom).dp))
                .padding(top = topPadding)
                .clip(RoundedCornerShape(3.dp))
                .clickable { onToggleRevised() }
                .padding(horizontal = 2.dp, vertical = 1.dp)
        ) {
            Text(
                text = "*",
                style = TextStyle(
                    fontFamily = ScreenplayCourierFontFamily,
                    fontSize = (16f * zoom).sp,
                    fontWeight = FontWeight.Bold,
                    color = revColorObj.composeColor
                )
            )
        }
    } else if (isRevisionTrackingEnabled && isFocused) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .then(if (!isDualColumn) Modifier.offset(x = (16 * zoom).dp) else Modifier.padding(end = (4 * zoom).dp))
                .padding(top = topPadding)
                .clip(RoundedCornerShape(3.dp))
                .clickable { onToggleRevised() }
                .padding(horizontal = 2.dp, vertical = 1.dp)
        ) {
            Text(
                text = "·",
                style = TextStyle(
                    fontFamily = ScreenplayCourierFontFamily,
                    fontSize = (16f * zoom).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray.copy(alpha = 0.35f)
                )
            )
        }
    }
}

@Composable
private fun BlockNoteSection(
    block: ScreenplayBlock,
    config: BlockDisplayConfig,
    startIndent: Dp,
    endIndent: Dp,
    callbacks: BlockCallbacks
) {
    if (config.showWriterNotes && block.note != null) {
        WriterNoteCard(
            noteText = block.note,
            zoom = config.zoom,
            startIndent = startIndent,
            endIndent = endIndent,
            onUpdateNote = callbacks.onUpdateNote,
            onDeleteNote = callbacks.onDeleteNote
        )
    } else if (config.isFocused && block.note == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startIndent + (12 * config.zoom).dp, top = 1.dp, bottom = 4.dp)
        ) {
            Surface(
                color = Color(0x0C000000),
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(0.5.dp, Color(0x20000000)),
                modifier = Modifier.clickable { callbacks.onUpdateNote("") }
            ) {
                Text(
                    text = "+ Note (Ctrl+N)",
                    style = TextStyle(
                        fontSize = (9.5f * config.zoom).sp,
                        color = Color(0xFF888888),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            if (config.isAiPolishEnabled && (block.type == BlockType.ACTION || block.type == BlockType.DIALOGUE) && block.text.isNotBlank()) {
                Spacer(modifier = Modifier.width((6 * config.zoom).dp))
                Surface(
                    color = ElectrumGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(3.dp),
                    border = BorderStroke(0.5.dp, ElectrumGold.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { callbacks.onOpenAiPolish() }
                ) {
                    Text(
                        text = if (block.type == BlockType.DIALOGUE) "✨ Subtext & Voice" else "✨ Punch Up Action",
                        style = TextStyle(
                            fontSize = (9.5f * config.zoom).sp,
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ScreenplayBlockItem(
    block: ScreenplayBlock,
    config: BlockDisplayConfig,
    callbacks: BlockCallbacks,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var blockCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = block.text,
                selection = TextRange(block.text.length)
            )
        )
    }

    LaunchedEffect(block.text, block.type) {
        if (textFieldValue.text != block.text) {
            val newSelection = if (textFieldValue.text.equals(block.text, ignoreCase = true)) {
                val sel = textFieldValue.selection.end.coerceIn(0, block.text.length)
                TextRange(sel)
            } else {
                TextRange(block.text.length)
            }
            textFieldValue = textFieldValue.copy(
                text = block.text,
                selection = newSelection
            )
        }
    }

    LaunchedEffect(config.isFocused, config.cursorTarget, config.isKeyboardActive) {
        if (config.isFocused) {
            val newSelection = when (config.cursorTarget.type) {
                CursorTargetType.START -> TextRange(0)
                CursorTargetType.END -> TextRange(block.text.length)
                CursorTargetType.TOP_LINE_COLUMN -> {
                    val firstLineEnd = block.text.indexOf('\n').let { if (it >= 0) it else block.text.length }
                    val targetOffset = config.cursorTarget.column.coerceIn(0, firstLineEnd)
                    TextRange(targetOffset)
                }
                CursorTargetType.BOTTOM_LINE_COLUMN -> {
                    val lastNewline = block.text.lastIndexOf('\n')
                    if (lastNewline >= 0) {
                        val lineStart = lastNewline + 1
                        val lineLength = block.text.length - lineStart
                        val targetOffset = (lineStart + config.cursorTarget.column.coerceIn(0, lineLength)).coerceIn(0, block.text.length)
                        TextRange(targetOffset)
                    } else {
                        val targetOffset = config.cursorTarget.column.coerceIn(0, block.text.length)
                        TextRange(targetOffset)
                    }
                }
                CursorTargetType.DEFAULT -> textFieldValue.selection
            }
            textFieldValue = textFieldValue.copy(selection = newSelection)
            try {
                focusRequester.requestFocus()
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {}
            if (config.isCompactScreen && !config.isKeyboardActive) {
                keyboardController?.hide()
            }
        }
    }

    val isHeading = block.type == BlockType.SCENE_HEADING || block.type == BlockType.STAGE_ACT_SCENE || block.type == BlockType.COMIC_PAGE || block.type == BlockType.COMIC_PANEL
    val isCharacter = block.type == BlockType.CHARACTER
    val isDialogue = block.type == BlockType.DIALOGUE || block.type == BlockType.COMIC_BALLOON
    val isParenthetical = block.type == BlockType.PARENTHETICAL
    val isTransition = block.type == BlockType.TRANSITION || block.type == BlockType.STAGE_CURTAIN
    val isSingleLine = isHeading || isCharacter || isParenthetical || isTransition || block.type == BlockType.COMIC_SFX
    val isUppercase = isHeading || isCharacter || isTransition || block.type == BlockType.COMIC_SFX
    val isItalic = block.type == BlockType.STAGE_DIRECTION || block.type == BlockType.COMIC_CAPTION
    val textAlign = when {
        config.isDualColumn && (isCharacter || isParenthetical) -> TextAlign.Center
        block.type == BlockType.STAGE_ACT_SCENE -> TextAlign.Center
        isTransition -> TextAlign.End
        else -> TextAlign.Start
    }

    // Vertical spacing scaled by zoom
    val topPadding = when {
        isHeading && !config.isFirstBlock -> (16 * config.zoom).dp
        else -> 0.dp
    }

    val bottomPadding = when {
        config.isDualColumn && isCharacter -> (4 * config.zoom).dp
        config.isDualColumn && isParenthetical -> (3 * config.zoom).dp
        isCharacter -> (2 * config.zoom).dp
        isParenthetical -> (2 * config.zoom).dp
        isDialogue && config.nextBlockType == BlockType.PARENTHETICAL -> (2 * config.zoom).dp
        config.isDualColumn -> (6 * config.zoom).dp
        else -> (16 * config.zoom).dp
    }

    // Horizontal indents based on industry standard 8.5" x 11" page, scaled by zoom
    val indentRatio = if (config.isCompactScreen) 0.38f else 1.0f

    val startIndent = when {
        config.isDualColumn -> 0.dp
        isCharacter -> (150 * config.zoom * indentRatio).dp
        isDialogue -> (75 * config.zoom * indentRatio).dp
        isParenthetical -> (110 * config.zoom * indentRatio).dp
        block.type == BlockType.STAGE_DIRECTION -> (40 * config.zoom * indentRatio).dp
        block.type == BlockType.COMIC_ART_NOTE -> (20 * config.zoom * indentRatio).dp
        block.type == BlockType.COMIC_CAPTION -> (35 * config.zoom * indentRatio).dp
        block.type == BlockType.COMIC_SFX -> (60 * config.zoom * indentRatio).dp
        else -> 0.dp
    }

    val endIndent = when {
        config.isDualColumn -> 0.dp
        isDialogue -> (110 * config.zoom * indentRatio).dp
        isCharacter -> (60 * config.zoom * indentRatio).dp
        isParenthetical -> (160 * config.zoom * indentRatio).dp
        block.type == BlockType.STAGE_DIRECTION -> (40 * config.zoom * indentRatio).dp
        block.type == BlockType.COMIC_CAPTION -> (40 * config.zoom * indentRatio).dp
        else -> 0.dp
    }

    val placeholderText = getBlockPlaceholder(block.type)

    // Hollywood Standard Typography calibrated for crisp, authentic display (14.5sp base at 100% zoom)
    val isDark = LocalSeshatDarkTheme.current
    val baseFontSize = (14.5f * config.zoom).sp
    val baseLineHeight = (22f * config.zoom).sp
    val blockFontFamily = if (config.isArcadeMode) MainAppFontFamily else ScreenplayCourierFontFamily

    val defaultTextColor = Color(0xFF111111)
    val headingTextColor = Color(0xFF000000)
    val charTextColor = Color(0xFF111111)
    val sceneNumColor = if (isDark) ScribeStarNeonRed else Color(0xFF111111)
    val placeholderColor = Color(0xFF94A3B8)
    val cursorColor = if (isDark) ScribeStarNeonRed else Color(0xFF111111)

    val highlightBg = if (config.isHighlighted) {
        if (isDark) ScribeStarNeonRed.copy(alpha = 0.18f) else Color(0xFFFFF3CD).copy(alpha = 0.9f)
    } else Color.Transparent
    val highlightBorder = if (config.isHighlighted) {
        BorderStroke(1.dp, if (isDark) ScribeStarNeonRed else ElectrumGold.copy(alpha = 0.6f))
    } else null

    Column(modifier = modifier.fillMaxWidth()) {
        if (isHeading && block.isOmitted) {
            Surface(
                color = Color(0xFFC0392B).copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(0.5.dp, Color(0xFFE74C3C).copy(alpha = 0.4f)),
                modifier = Modifier
                    .padding(start = startIndent, bottom = (4 * config.zoom).dp)
            ) {
                Text(
                    text = "🔒 SCENE OMITTED FROM SHOOTING SCRIPT",
                    style = TextStyle(
                        fontFamily = ScreenplayCourierFontFamily,
                        fontSize = (10f * config.zoom).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE74C3C)
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onGloballyPositioned { coords ->
                        blockCoordinates = coords
                        callbacks.onCoordinatesChanged(coords)
                    }
                    .padding(
                        start = startIndent,
                        end = endIndent,
                        top = topPadding,
                        bottom = bottomPadding
                    )
                    .background(highlightBg, RoundedCornerShape(4.dp))
                    .then(if (highlightBorder != null) Modifier.border(highlightBorder, RoundedCornerShape(4.dp)) else Modifier)
                    .padding(if (config.isHighlighted) 3.dp else 0.dp)
            ) {
                // Left scene number (in left margin gutter, 1.0" from paper edge)
                if (isHeading && config.showSceneNumbers && config.sceneNumber != null) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier.width(0.dp)
                    ) {
                        Text(
                            text = config.sceneNumber,
                            style = TextStyle(
                                fontFamily = ScreenplayCourierFontFamily,
                                fontSize = baseFontSize,
                                fontWeight = FontWeight.Bold,
                                color = sceneNumColor
                            ),
                            modifier = Modifier.offset(x = (-42 * config.zoom).dp)
                        )
                    }
                }

                // Subtle Gutter highlight selector handle (omitted in dual column to preserve full width and true centering)
                if (!config.isDualColumn) {
                    if (!config.isEditable) {
                        Text(
                            text = "🔒",
                            fontSize = (11 * config.zoom).sp,
                            modifier = Modifier.padding(top = (6 * config.zoom).dp, end = (6 * config.zoom).dp)
                        )
                    } else {
                        BlockGutterHandle(
                            blockId = block.id,
                            note = block.note,
                            isHighlighted = config.isHighlighted,
                            showWriterNotes = config.showWriterNotes,
                            zoom = config.zoom,
                            blockCoordinates = blockCoordinates,
                            callbacks = callbacks
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = placeholderText,
                            textAlign = textAlign,
                            style = TextStyle(
                                fontFamily = blockFontFamily,
                                fontSize = baseFontSize,
                                fontWeight = if (isHeading || isCharacter || isTransition) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                lineHeight = baseLineHeight,
                                textAlign = textAlign,
                                color = placeholderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    BasicTextField(
                        value = textFieldValue,
                        readOnly = !config.isEditable,
                        onValueChange = { newValue ->
                            if (!config.isEditable) return@BasicTextField
                            if (newValue.text.contains('\n')) {
                                val stripped = newValue.text.replace("\n", "")
                                if (stripped != block.text) {
                                    callbacks.onTextChange(stripped)
                                }
                                callbacks.onEnter()
                            } else {
                                textFieldValue = newValue
                                if (newValue.text != block.text) {
                                    callbacks.onTextChange(newValue.text)
                                }
                            }
                        },
                        onTextLayout = { textLayoutResult = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { if (it.isFocused) callbacks.onFocused() }
                            .onPreviewKeyEvent { keyEvent ->
                                handleBlockPreviewKeyEvent(
                                    keyEvent = keyEvent,
                                    block = block,
                                    textFieldValue = textFieldValue,
                                    textLayoutResult = textLayoutResult,
                                    isSingleLine = isSingleLine,
                                    config = config,
                                    callbacks = callbacks
                                )
                            },
                        textStyle = TextStyle(
                            fontFamily = blockFontFamily,
                            fontSize = baseFontSize,
                            fontWeight = if (isHeading || isCharacter || isTransition) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                            lineHeight = baseLineHeight,
                            textAlign = textAlign,
                            color = if (!config.isEditable) Color(0xFF475569) else if (block.isOmitted) Color.Gray.copy(alpha = 0.5f) else if (isHeading) headingTextColor else if (isCharacter) charTextColor else defaultTextColor,
                            textDecoration = if (block.isOmitted && !isHeading) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = if (isUppercase) KeyboardCapitalization.Characters else KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { callbacks.onEnter() },
                            onDone = { callbacks.onEnter() }
                        ),
                        cursorBrush = SolidColor(cursorColor),
                        singleLine = isSingleLine
                    )
                }

                // Glowing ruby-neon [TK - FIX LATER] badge when present in block
                if (block.text.contains("[TK - FIX LATER]")) {
                    Surface(
                        color = Color(0xFF280811),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, ScribeStarNeonRed),
                        modifier = Modifier.padding(start = (6 * config.zoom).dp, top = (2 * config.zoom).dp)
                    ) {
                        Text(
                            text = "TK",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                fontSize = (9.5f * config.zoom).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ScribeStarNeonRed
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                // Right scene number (aligned flush at right margin)
                if (isHeading && config.showSceneNumbers && config.sceneNumber != null) {
                    Text(
                        text = config.sceneNumber,
                        style = TextStyle(
                            fontFamily = ScreenplayCourierFontFamily,
                            fontSize = baseFontSize,
                            fontWeight = FontWeight.Bold,
                            color = sceneNumColor
                        ),
                        modifier = Modifier.padding(start = (8 * config.zoom).dp)
                    )
                }
            }

            // Right-margin Hollywood Revision Asterisk (*)
            if (!config.isArcadeMode) {
                BlockRevisionMarker(
                    isRevised = block.isRevised,
                    revisionColor = block.revisionColor,
                    isRevisionTrackingEnabled = config.isRevisionTrackingEnabled,
                    isFocused = config.isFocused,
                    isDualColumn = config.isDualColumn,
                    topPadding = topPadding,
                    zoom = config.zoom,
                    onToggleRevised = callbacks.onToggleRevised,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }

        // Writer's Note Section
        BlockNoteSection(
            block = block,
            config = config,
            startIndent = startIndent,
            endIndent = endIndent,
            callbacks = callbacks
        )
    }
}

@Composable
private fun FindReplacePanel(
    uiState: ScreenplayUiState,
    onClose: () -> Unit,
    onSetMode: (FindMode) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onUpdateReplaceQuery: (String) -> Unit,
    onToggleMatchCase: () -> Unit,
    onToggleMatchWholeWord: () -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onSetRenameOldName: (String) -> Unit,
    onSetRenameNewName: (String) -> Unit,
    onSetRenameCuesOnly: (Boolean) -> Unit,
    onRenameCharacter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.4f)),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Mode Switcher Tabs + Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Find & Replace Tab
                    Surface(
                        color = if (uiState.findMode == FindMode.FIND_REPLACE) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (uiState.findMode == FindMode.FIND_REPLACE) ElectrumGold else ObsidianSurfaceVariant),
                        modifier = Modifier.clickable { onSetMode(FindMode.FIND_REPLACE) }
                    ) {
                        Text(
                            text = "Find & Replace",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (uiState.findMode == FindMode.FIND_REPLACE) ElectrumGold else ScreenplayTextMuted,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Rename Character Tab
                    Surface(
                        color = if (uiState.findMode == FindMode.RENAME_CHARACTER) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (uiState.findMode == FindMode.RENAME_CHARACTER) ElectrumGold else ObsidianSurfaceVariant),
                        modifier = Modifier.clickable { onSetMode(FindMode.RENAME_CHARACTER) }
                    ) {
                        Text(
                            text = "Rename Character",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (uiState.findMode == FindMode.RENAME_CHARACTER) ElectrumGold else ScreenplayTextMuted,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Find (Esc)",
                        tint = ScreenplayTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (uiState.findMode) {
                FindMode.FIND_REPLACE -> {
                    // Row 1: Search Input + Match Count + Prev/Next + Options
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Search text field
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (uiState.searchQuery.isNotEmpty()) ElectrumGold.copy(alpha = 0.6f) else ObsidianSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Find in screenplay...",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                                    )
                                }
                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = onUpdateSearchQuery,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                    cursorBrush = SolidColor(ElectrumGold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Matches count badge
                        val matchText = when {
                            uiState.searchQuery.isEmpty() -> ""
                            uiState.searchMatches.isEmpty() -> "No match"
                            else -> "${uiState.currentMatchIndex + 1}/${uiState.searchMatches.size}"
                        }
                        if (matchText.isNotEmpty()) {
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (uiState.searchMatches.isNotEmpty()) ElectrumGold.copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = matchText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (uiState.searchMatches.isNotEmpty()) ElectrumGold else Color(0xFFFF6B6B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Prev / Next buttons
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                            modifier = Modifier.clickable(onClick = onFindPrevious)
                        ) {
                            Text(
                                text = "▲",
                                color = if (uiState.searchMatches.isNotEmpty()) ElectrumGold else ScreenplayTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                            modifier = Modifier.clickable(onClick = onFindNext)
                        ) {
                            Text(
                                text = "▼",
                                color = if (uiState.searchMatches.isNotEmpty()) ElectrumGold else ScreenplayTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Match Case chip
                        Surface(
                            color = if (uiState.matchCase) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (uiState.matchCase) ElectrumGold else ObsidianSurfaceVariant),
                            modifier = Modifier.clickable(onClick = onToggleMatchCase)
                        ) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (uiState.matchCase) ElectrumGold else ScreenplayTextMuted,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        // Whole Word chip
                        Surface(
                            color = if (uiState.matchWholeWord) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (uiState.matchWholeWord) ElectrumGold else ObsidianSurfaceVariant),
                            modifier = Modifier.clickable(onClick = onToggleMatchWholeWord)
                        ) {
                            Text(
                                text = "Word",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (uiState.matchWholeWord) ElectrumGold else ScreenplayTextMuted,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Replace Input + Replace / Replace All buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                if (uiState.replaceQuery.isEmpty()) {
                                    Text(
                                        text = "Replace with...",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                                    )
                                }
                                BasicTextField(
                                    value = uiState.replaceQuery,
                                    onValueChange = onUpdateReplaceQuery,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                    cursorBrush = SolidColor(ElectrumGold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Replace current button
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (uiState.searchMatches.isNotEmpty()) ElectrumGold.copy(alpha = 0.5f) else ObsidianSurfaceVariant),
                            modifier = Modifier.clickable(onClick = onReplaceCurrent)
                        ) {
                            Text(
                                text = "Replace",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (uiState.searchMatches.isNotEmpty()) ElectrumGold else ScreenplayTextMuted,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Replace All button
                        Surface(
                            color = if (uiState.searchMatches.isNotEmpty()) ElectrumGold.copy(alpha = 0.25f) else ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (uiState.searchMatches.isNotEmpty()) ElectrumGold else ObsidianSurfaceVariant),
                            modifier = Modifier.clickable(onClick = onReplaceAll)
                        ) {
                            Text(
                                text = "Replace All",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (uiState.searchMatches.isNotEmpty()) ElectrumGold else ScreenplayTextMuted,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                FindMode.RENAME_CHARACTER -> {
                    // Character rename layout
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Character selector chips
                        if (uiState.characters.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "SELECT:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                uiState.characters.forEach { charName ->
                                    val isSelected = uiState.renameOldName.equals(charName, ignoreCase = true)
                                    Surface(
                                        color = if (isSelected) ElectrumGold.copy(alpha = 0.25f) else ObsidianBlack,
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, if (isSelected) ElectrumGold else ObsidianSurfaceVariant),
                                        modifier = Modifier.clickable { onSetRenameOldName(charName) }
                                    ) {
                                        Text(
                                            text = charName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) ElectrumGold else ScreenplayTextDark,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Inputs: Current Name & New Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Old Name
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    if (uiState.renameOldName.isEmpty()) {
                                        Text(
                                            text = "Old Character Name",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                                        )
                                    }
                                    BasicTextField(
                                        value = uiState.renameOldName,
                                        onValueChange = onSetRenameOldName,
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                        cursorBrush = SolidColor(ElectrumGold),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Text(text = "➔", color = ElectrumGold, fontSize = 14.sp)

                            // New Name
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (uiState.renameNewName.isNotEmpty()) ElectrumGold.copy(alpha = 0.7f) else ObsidianSurfaceVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    if (uiState.renameNewName.isEmpty()) {
                                        Text(
                                            text = "NEW NAME",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                                        )
                                    }
                                    BasicTextField(
                                        value = uiState.renameNewName,
                                        onValueChange = { onSetRenameNewName(it.uppercase(Locale.US)) },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Bold),
                                        cursorBrush = SolidColor(ElectrumGold),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Scope & Rename Action Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onSetRenameCuesOnly(!uiState.renameCuesOnly) }
                            ) {
                                Checkbox(
                                    checked = uiState.renameCuesOnly,
                                    onCheckedChange = onSetRenameCuesOnly,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ElectrumGold,
                                        checkmarkColor = Color(0xFF0F1216)
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.renameCuesOnly) "Rename Character Cues Only" else "Rename Everywhere (Cues, Action & Dialogue)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.renameCuesOnly) ScreenplayTextMuted else ElectrumGold,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            val canRename = uiState.renameOldName.isNotBlank() &&
                                    uiState.renameNewName.isNotBlank() &&
                                    !uiState.renameOldName.equals(uiState.renameNewName, ignoreCase = true)

                            Surface(
                                color = if (canRename) ElectrumGold else ObsidianBlack,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (canRename) ElectrumGold else ObsidianSurfaceVariant),
                                modifier = Modifier.clickable(enabled = canRename, onClick = onRenameCharacter)
                            ) {
                                Text(
                                    text = "⚡ Rename Character",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (canRename) Color(0xFF0F1216) else ScreenplayTextMuted,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
