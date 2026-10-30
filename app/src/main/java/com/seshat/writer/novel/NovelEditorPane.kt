package com.seshat.writer.novel

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.res.painterResource
import com.seshat.writer.R
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.util.ProjectBackupManager
import com.seshat.writer.util.DeviceEnvironment
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.DeskBackground
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import com.seshat.writer.ui.theme.opticalGlassBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.smokedGlassBrush
import com.seshat.writer.ui.FloatingThemeTogglePill

@Composable
fun NovelEditorPane(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    manuscript: NovelManuscript,
    activeChapterIndex: Int,
    onSelectChapter: (Int) -> Unit,
    onUpdateManuscript: (NovelManuscript) -> Unit,
    onOpenCompiler: () -> Unit,
    onToggleCorkboard: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onOpenStartMenu: () -> Unit,
    onToggleDictation: () -> Unit,
    onSaveProject: () -> Unit = {},
    onOpenStudioDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isCompactScreen = screenWidthDp < 600
    val keyboardController = LocalSoftwareKeyboardController.current
    var isKeyboardActive by remember { mutableStateOf(false) }

    val currentChapter = manuscript.chapters.getOrNull(activeChapterIndex)
        ?: manuscript.chapters.firstOrNull()
        ?: NovelChapter()

    var isBinderVisible by remember { mutableStateOf(!isCompactScreen) }
    var focusedParagraphId by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    var highlightedParagraphIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var highlightAnchorParagraphId by remember { mutableStateOf<String?>(null) }
    val paragraphBounds = remember { mutableStateMapOf<String, Rect>() }
    var cardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var isCoverCreatorOpen by remember { mutableStateOf(false) }
    var showFindReplaceDialog by remember { mutableStateOf(false) }
    var showSprintDialog by remember { mutableStateOf(false) }
    var showScrapsDialog by remember { mutableStateOf(false) }
    var showSnapshotDialog by remember { mutableStateOf(false) }
    var isTypewriterMode by remember { mutableStateOf(manuscript.isTypewriterMode) }

    LaunchedEffect(activeChapterIndex, currentChapter.id) {
        highlightedParagraphIds = emptySet()
        highlightAnchorParagraphId = null
        if (focusedParagraphId == null || currentChapter.paragraphs.none { it.id == focusedParagraphId }) {
            focusedParagraphId = currentChapter.paragraphs.firstOrNull()?.id
        }
    }

    fun toggleParagraphHighlight(paragraphId: String, isShift: Boolean) {
        if (isShift && highlightAnchorParagraphId != null) {
            val anchorId = highlightAnchorParagraphId!!
            val idx1 = currentChapter.paragraphs.indexOfFirst { it.id == anchorId }
            val idx2 = currentChapter.paragraphs.indexOfFirst { it.id == paragraphId }
            if (idx1 != -1 && idx2 != -1) {
                val start = minOf(idx1, idx2)
                val end = maxOf(idx1, idx2)
                val rangeIds = currentChapter.paragraphs.subList(start, end + 1).map { it.id }.toSet()
                highlightedParagraphIds = rangeIds
                return
            }
        }
        if (highlightedParagraphIds.contains(paragraphId)) {
            val nextSet = highlightedParagraphIds - paragraphId
            highlightedParagraphIds = nextSet
            highlightAnchorParagraphId = nextSet.firstOrNull()
        } else {
            highlightedParagraphIds = highlightedParagraphIds + paragraphId
            highlightAnchorParagraphId = paragraphId
        }
    }

    fun highlightParagraphRange(fromId: String, toId: String) {
        val idx1 = currentChapter.paragraphs.indexOfFirst { it.id == fromId }
        val idx2 = currentChapter.paragraphs.indexOfFirst { it.id == toId }
        if (idx1 == -1 || idx2 == -1) return
        val start = minOf(idx1, idx2)
        val end = maxOf(idx1, idx2)
        val rangeIds = currentChapter.paragraphs.subList(start, end + 1).map { it.id }.toSet()
        highlightedParagraphIds = rangeIds
        highlightAnchorParagraphId = fromId
    }

    fun findParagraphAtY(rootY: Float): String? {
        if (paragraphBounds.isEmpty() || currentChapter.paragraphs.isEmpty()) return null
        for (p in currentChapter.paragraphs) {
            val rect = paragraphBounds[p.id] ?: continue
            if (rootY >= rect.top && rootY <= rect.bottom) {
                return p.id
            }
        }
        val firstP = currentChapter.paragraphs.firstOrNull { paragraphBounds.containsKey(it.id) }
        if (firstP != null) {
            val firstRect = paragraphBounds[firstP.id]!!
            if (rootY < firstRect.top) return firstP.id
        }
        val lastP = currentChapter.paragraphs.lastOrNull { paragraphBounds.containsKey(it.id) }
        if (lastP != null) {
            val lastRect = paragraphBounds[lastP.id]!!
            if (rootY > lastRect.bottom) return lastP.id
        }
        return currentChapter.paragraphs
            .filter { paragraphBounds.containsKey(it.id) }
            .minByOrNull { p ->
                val rect = paragraphBounds[p.id]!!
                if (rootY < rect.top) rect.top - rootY else rootY - rect.bottom
            }?.id
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ambientWorkspaceBrush(LocalSeshatDarkTheme.current))
    ) {
        // ==========================================
        // 1. TOP TOOLBAR
        // ==========================================
        val onToggleTypewriterAction: () -> Unit = {
            isTypewriterMode = !isTypewriterMode
            val updated = manuscript.copy(isTypewriterMode = isTypewriterMode)
            onUpdateManuscript(updated)
            NovelRepository.saveManuscript(context, updated)
            Unit
        }
        val onBackupAction = {
            val jsonStr = manuscript.toJson().toString()
            val file = ProjectBackupManager.createBackup(context, manuscript.title, jsonStr)
            if (file != null && file.exists()) {
                Toast.makeText(context, "📦 Backup saved: ${file.name}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Backup failed.", Toast.LENGTH_SHORT).show()
            }
        }

        if (isCompactScreen) {
            CompactNovelTopBar(
                manuscript = manuscript,
                isBinderVisible = isBinderVisible,
                isTypewriterMode = isTypewriterMode,
                isArcadeMode = uiState.firstDraftState.isArcadeLocked,
                onToggleBinder = { isBinderVisible = !isBinderVisible },
                onOpenStudioDrawer = onOpenStudioDrawer,
                onToggleCorkboard = onToggleCorkboard,
                onOpenStoryBible = onOpenStoryBible,
                onOpenFindReplace = { showFindReplaceDialog = true },
                onOpenSprint = { showSprintDialog = true },
                onToggleTypewriter = onToggleTypewriterAction,
                onOpenScraps = { showScrapsDialog = true },
                onOpenCompiler = onOpenCompiler,
                onSaveProject = onSaveProject
            )
        } else {
            DesktopNovelTopBar(
                manuscript = manuscript,
                isDictating = uiState.isDictating,
                isCorkboardOpen = uiState.isCorkboardOpen,
                isTypewriterMode = isTypewriterMode,
                isArcadeMode = uiState.firstDraftState.isArcadeLocked,
                onOpenStudioDrawer = onOpenStudioDrawer,
                onToggleDictation = { onToggleDictation() },
                onOpenStoryBible = onOpenStoryBible,
                onToggleCorkboard = onToggleCorkboard,
                onOpenFindReplace = { showFindReplaceDialog = true },
                onOpenSprint = { showSprintDialog = true },
                onToggleTypewriter = onToggleTypewriterAction,
                onOpenScraps = { showScrapsDialog = true },
                onOpenCompiler = onOpenCompiler,
                onSaveProject = onSaveProject
            )
        }

        // ==========================================
        // 2. MAIN WORKSPACE (BINDER + MANUSCRIPT)
        // ==========================================
        if (isCompactScreen && isBinderVisible) {
            NovelBinderPane(
                manuscript = manuscript,
                selectedChapterIndex = activeChapterIndex,
                onSelectChapter = { idx ->
                    onSelectChapter(idx)
                    isBinderVisible = false
                },
                onAddChapter = {
                    val newNum = manuscript.chapters.size + 1
                    val newChapter = NovelChapter(
                        chapterNumber = newNum,
                        title = "Chapter $newNum",
                        actIndex = 0,
                        paragraphs = NovelChapter.defaultParagraphs(newNum)
                    )
                    val updated = manuscript.copy(chapters = manuscript.chapters + newChapter)
                    onUpdateManuscript(updated)
                    onSelectChapter(manuscript.chapters.size)
                    isBinderVisible = false
                },
                onMoveChapterUp = { idx ->
                    if (idx > 0) {
                        val list = manuscript.chapters.toMutableList()
                        val temp = list[idx]
                        list[idx] = list[idx - 1]
                        list[idx - 1] = temp
                        onUpdateManuscript(manuscript.copy(chapters = list))
                        onSelectChapter(idx - 1)
                    }
                },
                onMoveChapterDown = { idx ->
                    if (idx < manuscript.chapters.lastIndex) {
                        val list = manuscript.chapters.toMutableList()
                        val temp = list[idx]
                        list[idx] = list[idx + 1]
                        list[idx + 1] = temp
                        onUpdateManuscript(manuscript.copy(chapters = list))
                        onSelectChapter(idx + 1)
                    }
                },
                onCloseBinder = { isBinderVisible = false },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // SCRIVENER-STYLE LEFT BINDER (Tablet / Desktop side-by-side floating card)
                AnimatedVisibility(visible = isBinderVisible && !isCompactScreen) {
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .padding(start = 14.dp, top = 8.dp, bottom = 12.dp, end = 6.dp)
                            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = ScribeStarNeonRed.copy(alpha = 0.25f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(opticalGlassBrush(glassOpacity = 0.88f))
                            .border(prismaticGlassBorder(width = 1.2.dp), RoundedCornerShape(20.dp))
                    ) {
                        NovelBinderPane(
                            manuscript = manuscript,
                            selectedChapterIndex = activeChapterIndex,
                            onSelectChapter = onSelectChapter,
                            onAddChapter = {
                                val newNum = manuscript.chapters.size + 1
                                val newChapter = NovelChapter(
                                    chapterNumber = newNum,
                                    title = "Chapter $newNum",
                                    actIndex = 0,
                                    paragraphs = NovelChapter.defaultParagraphs(newNum)
                                )
                                val updated = manuscript.copy(chapters = manuscript.chapters + newChapter)
                                onUpdateManuscript(updated)
                                onSelectChapter(manuscript.chapters.size)
                            },
                            onMoveChapterUp = { idx ->
                                if (idx > 0) {
                                    val list = manuscript.chapters.toMutableList()
                                    val temp = list[idx]
                                    list[idx] = list[idx - 1]
                                    list[idx - 1] = temp
                                    onUpdateManuscript(manuscript.copy(chapters = list))
                                    onSelectChapter(idx - 1)
                                }
                            },
                            onMoveChapterDown = { idx ->
                                if (idx < manuscript.chapters.lastIndex) {
                                    val list = manuscript.chapters.toMutableList()
                                    val temp = list[idx]
                                    list[idx] = list[idx + 1]
                                    list[idx + 1] = temp
                                    onUpdateManuscript(manuscript.copy(chapters = list))
                                    onSelectChapter(idx + 1)
                                }
                            },
                            onCloseBinder = { isBinderVisible = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // CENTER MANUSCRIPT EDITOR CANVAS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(ambientWorkspaceBrush(LocalSeshatDarkTheme.current)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(if (isCompactScreen) (screenWidthDp - 12).coerceAtLeast(280).dp else 760.dp)
                            .padding(
                                horizontal = if (isCompactScreen) 6.dp else 16.dp,
                                vertical = if (isCompactScreen) 8.dp else 20.dp
                            )
                    ) {
                    val isChapterEditable = !uiState.firstDraftState.isArcadeLocked ||
                        uiState.firstDraftState.isMulliganActive() ||
                        activeChapterIndex == uiState.firstDraftState.novelUnlockedChapterIndex

                    if (uiState.firstDraftState.isArcadeLocked) {
                        Surface(
                            color = if (isChapterEditable) Color(0xFF0F172A) else Color(0xFF450A0A),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isChapterEditable) ScribeStarNeonRed.copy(alpha = 0.55f) else Color(0xFFEF4444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            val bannerText = if (isChapterEditable) {
                                "📖 SEQUENTIAL CHAPTER AIRLOCK: Chapter ${activeChapterIndex + 1} (${currentChapter.wordCount()} / 3,500 words to seal & unlock Chapter ${activeChapterIndex + 2})"
                            } else if (activeChapterIndex < uiState.firstDraftState.novelUnlockedChapterIndex) {
                                "🔒 CHAPTER ${activeChapterIndex + 1} SEALED (READ-ONLY) — Purchase 20-min Mulligan (5,000 XP) in XP Shop to edit"
                            } else {
                                "🔒 CHAPTER ${activeChapterIndex + 1} LOCKED — Reach 3,500 words in Chapter ${uiState.firstDraftState.novelUnlockedChapterIndex + 1} first"
                            }
                            Text(
                                text = bannerText,
                                color = Color.White,
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Chapter Header Bar
                    NovelChapterHeaderBar(
                        chapter = currentChapter,
                        isTypewriterMode = isTypewriterMode,
                        isArcadeMode = uiState.firstDraftState.isArcadeLocked,
                        onOpenSnapshots = { showSnapshotDialog = true },
                        onToggleTypewriter = onToggleTypewriterAction,
                        onInsertSceneBreak = {
                            if (!isChapterEditable) return@NovelChapterHeaderBar
                            val breakP = NovelParagraph(
                                type = NovelParagraphType.SCENE_BREAK,
                                text = "* * *"
                            )
                            val nextP = NovelParagraph(
                                type = NovelParagraphType.NARRATIVE,
                                text = ""
                            )
                            val updatedList = currentChapter.paragraphs + listOf(breakP, nextP)
                            val updatedCh = currentChapter.copy(paragraphs = updatedList)
                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                if (i == activeChapterIndex) updatedCh else ch
                            }
                            onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                        }
                    )

                    // Book Page Sheet (Soft cream book paper background, or Obsidian Arcade Canvas)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.firstDraftState.isArcadeLocked) Color(0xFF0B0F19) else Color(0xFFFAFAF7)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .onGloballyPositioned { cardCoordinates = it }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Left Margin Gutter Drag Strip for whole page
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .width(40.dp)
                                    .fillMaxHeight()
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                val rootY = cardCoordinates?.localToRoot(offset)?.y ?: 0f
                                                val targetId = findParagraphAtY(rootY)
                                                if (targetId != null) {
                                                    highlightAnchorParagraphId = targetId
                                                    highlightedParagraphIds = setOf(targetId)
                                                }
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                val anchorId = highlightAnchorParagraphId
                                                if (anchorId != null) {
                                                    val rootY = cardCoordinates?.localToRoot(change.position)?.y ?: 0f
                                                    val targetId = findParagraphAtY(rootY)
                                                    if (targetId != null) {
                                                        highlightParagraphRange(anchorId, targetId)
                                                    }
                                                }
                                            },
                                            onDragEnd = {},
                                            onDragCancel = {}
                                        )
                                    }
                            )

                            val highestQualifiedIdx = if (uiState.firstDraftState.isArcadeLocked && !uiState.firstDraftState.isMulliganActive()) {
                                currentChapter.paragraphs.indexOfLast { it.text.length >= 20 }
                            } else -1

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = if (isCompactScreen) 16.dp else 28.dp,
                                        end = if (isCompactScreen) 16.dp else 48.dp,
                                        top = if (isCompactScreen) 16.dp else 36.dp,
                                        bottom = if (isTypewriterMode) 380.dp else if (isCompactScreen) 24.dp else 36.dp
                                    )
                            ) {
                                itemsIndexed(currentChapter.paragraphs, key = { _, p -> p.id }) { pIdx, paragraph ->
                                    val isHighlighted = highlightedParagraphIds.contains(paragraph.id)
                                    val hasHeader = currentChapter.paragraphs.firstOrNull()?.type == NovelParagraphType.CHAPTER_HEADER
                                    val isFirstNarrative = if (hasHeader) pIdx == 1 else pIdx == 0
                                    val isParaEditable = isChapterEditable && (highestQualifiedIdx < 2 || pIdx > highestQualifiedIdx - 2)
                                    NovelParagraphItem(
                                        paragraph = paragraph,
                                        isFocused = focusedParagraphId == paragraph.id,
                                        isFirstParagraphInChapter = isFirstNarrative,
                                        isHighlighted = isHighlighted,
                                        isEditable = isParaEditable,
                                        isArcadeMode = uiState.firstDraftState.isArcadeLocked,
                                        onInsertTkToken = { viewModel.insertTkFixLaterToken(context) },
                                        onToggleHighlight = { isShift ->
                                            toggleParagraphHighlight(paragraph.id, isShift)
                                        },
                                        onDragHighlightStart = {
                                            highlightAnchorParagraphId = paragraph.id
                                            highlightedParagraphIds = setOf(paragraph.id)
                                        },
                                        onDragHighlightMove = { rootY ->
                                            val anchorId = highlightAnchorParagraphId ?: paragraph.id
                                            val targetId = findParagraphAtY(rootY)
                                            if (targetId != null) {
                                                highlightParagraphRange(anchorId, targetId)
                                            }
                                        },
                                        onDragHighlightEnd = {},
                                        onCoordinatesChanged = { coords ->
                                            paragraphBounds[paragraph.id] = coords.boundsInRoot()
                                        },
                                        onTextChange = { newText ->
                                            val updatedList = currentChapter.paragraphs.map {
                                                if (it.id == paragraph.id) it.copy(text = newText) else it
                                            }
                                            val updatedCh = currentChapter.copy(paragraphs = updatedList)
                                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                                if (i == activeChapterIndex) updatedCh else ch
                                            }
                                            onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                                        },
                                        onTypeChange = { newType ->
                                            val updatedList = currentChapter.paragraphs.map {
                                                if (it.id == paragraph.id) it.copy(type = newType) else it
                                            }
                                            val updatedCh = currentChapter.copy(paragraphs = updatedList)
                                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                                if (i == activeChapterIndex) updatedCh else ch
                                            }
                                            onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                                        },
                                        onEnterPressed = {
                                            // ENTER: Inserts a new indented narrative paragraph directly after
                                            val newP = NovelParagraph(
                                                type = NovelParagraphType.NARRATIVE,
                                                text = ""
                                            )
                                            val currentList = currentChapter.paragraphs.toMutableList()
                                            currentList.add(pIdx + 1, newP)
                                            val updatedCh = currentChapter.copy(paragraphs = currentList)
                                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                                if (i == activeChapterIndex) updatedCh else ch
                                            }
                                            onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                                            focusedParagraphId = newP.id
                                        },
                                        onBackspaceOnEmpty = {
                                            if (currentChapter.paragraphs.size > 1) {
                                                val currentList = currentChapter.paragraphs.toMutableList()
                                                currentList.removeAt(pIdx)
                                                val updatedCh = currentChapter.copy(paragraphs = currentList)
                                                val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                                    if (i == activeChapterIndex) updatedCh else ch
                                                }
                                                onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                                                focusedParagraphId = currentList.getOrNull((pIdx - 1).coerceAtLeast(0))?.id
                                            } else if (paragraph.type == NovelParagraphType.CHAPTER_HEADER) {
                                                val updatedCh = currentChapter.copy(
                                                    paragraphs = listOf(NovelParagraph(type = NovelParagraphType.NARRATIVE, text = ""))
                                                )
                                                val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                                    if (i == activeChapterIndex) updatedCh else ch
                                                }
                                                onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                                                focusedParagraphId = updatedCh.paragraphs.first().id
                                            }
                                        },
                                        onNavigateUp = {
                                            if (pIdx > 0) {
                                                focusedParagraphId = currentChapter.paragraphs[pIdx - 1].id
                                            }
                                        },
                                        onNavigateDown = {
                                            if (pIdx < currentChapter.paragraphs.lastIndex) {
                                                focusedParagraphId = currentChapter.paragraphs[pIdx + 1].id
                                            }
                                        },
                                        onFocused = {
                                            focusedParagraphId = paragraph.id
                                        },
                                        onInsertDialogueVerb = { verb ->
                                            // Verb inserted
                                        },
                                        isCompactScreen = isCompactScreen,
                                        isKeyboardActive = isKeyboardActive
                                    )
                                }
                            }
                        }
                    }
                }

                // FLOATING NOVEL CONTEXTUAL ACTION BAR
                androidx.compose.animation.AnimatedVisibility(
                    visible = highlightedParagraphIds.isNotEmpty(),
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    val selParagraphs = currentChapter.paragraphs.filter { highlightedParagraphIds.contains(it.id) }
                    val selWords = selParagraphs.sumOf { it.wordCount() }
                    NovelHighlightActionBar(
                        highlightedCount = highlightedParagraphIds.size,
                        wordCount = selWords,
                        onCopy = {
                            val textToCopy = selParagraphs.joinToString("\n\n") { it.text }
                            clipboardManager.setText(AnnotatedString(textToCopy))
                        },
                        onConvertToDialogue = {
                            val updatedList = currentChapter.paragraphs.map { p ->
                                if (highlightedParagraphIds.contains(p.id)) {
                                    var t = p.text.trim()
                                    if (!t.startsWith("“")) t = "“$t"
                                    if (!t.endsWith("”")) t = "$t”"
                                    p.copy(type = NovelParagraphType.DIALOGUE, text = t)
                                } else p
                            }
                            val updatedCh = currentChapter.copy(paragraphs = updatedList)
                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                if (i == activeChapterIndex) updatedCh else ch
                            }
                            onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                        },
                        onConvertToNarration = {
                            val updatedList = currentChapter.paragraphs.map { p ->
                                if (highlightedParagraphIds.contains(p.id)) {
                                    var t = p.text.trim()
                                    if (t.startsWith("“") || t.startsWith("\"")) t = t.substring(1).trimStart()
                                    if (t.endsWith("”") || t.endsWith("\"")) t = t.substring(0, t.length - 1).trimEnd()
                                    p.copy(type = NovelParagraphType.NARRATIVE, text = t)
                                } else p
                            }
                            val updatedCh = currentChapter.copy(paragraphs = updatedList)
                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                if (i == activeChapterIndex) updatedCh else ch
                            }
                            onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                        },
                        onSendToGraveyard = {
                            val textToScrap = selParagraphs.joinToString("\n\n") { it.text }
                            val newScrap = NovelScrap(
                                title = "Cut from ${currentChapter.title}",
                                content = textToScrap,
                                originalChapterTitle = currentChapter.title,
                                wordCount = selWords,
                                deletedAt = System.currentTimeMillis()
                            )
                            val remaining = currentChapter.paragraphs.filter { !highlightedParagraphIds.contains(it.id) }
                            val finalList = if (remaining.isEmpty()) {
                                listOf(NovelParagraph(type = NovelParagraphType.NARRATIVE, text = ""))
                            } else remaining
                            val updatedCh = currentChapter.copy(paragraphs = finalList)
                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                if (i == activeChapterIndex) updatedCh else ch
                            }
                            val updated = manuscript.copy(chapters = updatedChapters, scraps = listOf(newScrap) + manuscript.scraps)
                            onUpdateManuscript(updated)
                            NovelRepository.saveManuscript(context, updated)
                            highlightedParagraphIds = emptySet()
                            highlightAnchorParagraphId = null
                            Toast.makeText(context, "Saved to Graveyard 🪦", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            val textToScrap = selParagraphs.joinToString("\n\n") { it.text }
                            val newScrap = NovelScrap(
                                title = "Deleted from ${currentChapter.title}",
                                content = textToScrap,
                                originalChapterTitle = currentChapter.title,
                                wordCount = selWords,
                                deletedAt = System.currentTimeMillis()
                            )
                            val remaining = currentChapter.paragraphs.filter { !highlightedParagraphIds.contains(it.id) }
                            val finalList = if (remaining.isEmpty()) {
                                listOf(NovelParagraph(type = NovelParagraphType.NARRATIVE, text = ""))
                            } else remaining
                            val updatedCh = currentChapter.copy(paragraphs = finalList)
                            val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                                if (i == activeChapterIndex) updatedCh else ch
                            }
                            val updated = manuscript.copy(chapters = updatedChapters, scraps = listOf(newScrap) + manuscript.scraps)
                            onUpdateManuscript(updated)
                            NovelRepository.saveManuscript(context, updated)
                            highlightedParagraphIds = emptySet()
                            highlightAnchorParagraphId = null
                        },
                        onClear = {
                            highlightedParagraphIds = emptySet()
                            highlightAnchorParagraphId = null
                        }
                    )
                }
            }
        }
    }

        // ==========================================
        // 3. NOVEL SMART BOTTOM HUD
        // ==========================================
        NovelBottomHud(
            isCompactScreen = isCompactScreen,
            currentChapter = currentChapter,
            focusedParagraph = currentChapter.paragraphs.find { it.id == focusedParagraphId } ?: currentChapter.paragraphs.firstOrNull(),
            isDictating = uiState.isDictating,
            isKeyboardActive = isKeyboardActive,
            onToggleKeyboard = {
                isKeyboardActive = !isKeyboardActive
                if (isKeyboardActive) {
                    keyboardController?.show()
                } else {
                    keyboardController?.hide()
                }
            },
            onToggleDictation = onToggleDictation,
            onSetParagraphType = { newType ->
                val targetId = focusedParagraphId ?: currentChapter.paragraphs.firstOrNull()?.id
                if (targetId != null) {
                    val updatedList = currentChapter.paragraphs.map {
                        if (it.id == targetId) it.copy(type = newType) else it
                    }
                    val updatedCh = currentChapter.copy(paragraphs = updatedList)
                    val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                        if (i == activeChapterIndex) updatedCh else ch
                    }
                    onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                }
            },
            onInsertSceneBreak = {
                val breakP = NovelParagraph(
                    type = NovelParagraphType.SCENE_BREAK,
                    text = "* * *"
                )
                val nextP = NovelParagraph(
                    type = NovelParagraphType.NARRATIVE,
                    text = ""
                )
                val idx = currentChapter.paragraphs.indexOfFirst { it.id == focusedParagraphId }
                val insertAt = if (idx >= 0) idx + 1 else currentChapter.paragraphs.size
                val currentList = currentChapter.paragraphs.toMutableList()
                currentList.add(insertAt, breakP)
                currentList.add(insertAt + 1, nextP)
                val updatedCh = currentChapter.copy(paragraphs = currentList)
                val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                    if (i == activeChapterIndex) updatedCh else ch
                }
                onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                focusedParagraphId = nextP.id
            },
            onInsertParagraph = {
                val newP = NovelParagraph(
                    type = NovelParagraphType.NARRATIVE,
                    text = ""
                )
                val idx = currentChapter.paragraphs.indexOfFirst { it.id == focusedParagraphId }
                val insertAt = if (idx >= 0) idx + 1 else currentChapter.paragraphs.size
                val currentList = currentChapter.paragraphs.toMutableList()
                currentList.add(insertAt, newP)
                val updatedCh = currentChapter.copy(paragraphs = currentList)
                val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                    if (i == activeChapterIndex) updatedCh else ch
                }
                onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                focusedParagraphId = newP.id
            },
            onBackspace = {
                val idx = currentChapter.paragraphs.indexOfFirst { it.id == focusedParagraphId }
                if (idx >= 0 && currentChapter.paragraphs.size > 1) {
                    val currentList = currentChapter.paragraphs.toMutableList()
                    currentList.removeAt(idx)
                    val updatedCh = currentChapter.copy(paragraphs = currentList)
                    val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                        if (i == activeChapterIndex) updatedCh else ch
                    }
                    onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                    focusedParagraphId = currentList.getOrNull((idx - 1).coerceAtLeast(0))?.id
                }
            },
            onTabPressed = {
                val targetId = focusedParagraphId ?: currentChapter.paragraphs.firstOrNull()?.id
                if (targetId != null) {
                    val p = currentChapter.paragraphs.find { it.id == targetId }
                    if (p != null) {
                        val newType = if (p.type == NovelParagraphType.DIALOGUE) NovelParagraphType.NARRATIVE else NovelParagraphType.DIALOGUE
                        val newText = if (newType == NovelParagraphType.DIALOGUE && !p.text.startsWith("“")) {
                            "“" + p.text
                        } else if (newType == NovelParagraphType.NARRATIVE && p.text.startsWith("“")) {
                            p.text.removePrefix("“")
                        } else {
                            p.text
                        }
                        val updatedList = currentChapter.paragraphs.map {
                            if (it.id == targetId) it.copy(type = newType, text = newText) else it
                        }
                        val updatedCh = currentChapter.copy(paragraphs = updatedList)
                        val updatedChapters = manuscript.chapters.mapIndexed { i, ch ->
                            if (i == activeChapterIndex) updatedCh else ch
                        }
                        onUpdateManuscript(manuscript.copy(chapters = updatedChapters))
                    }
                }
            },
            onSaveProject = onSaveProject,
            onToggleTheme = { viewModel.toggleThemeMode(context) }
        )
    }

    if (isCoverCreatorOpen) {
        NovelCoverCreatorDialog(
            manuscript = manuscript,
            onSaveDesign = { updatedDesign ->
                val updated = manuscript.copy(coverDesign = updatedDesign)
                onUpdateManuscript(updated)
                NovelRepository.saveManuscript(context, updated)
            },
            onDismiss = { isCoverCreatorOpen = false }
        )
    }

    if (showFindReplaceDialog) {
        NovelFindReplaceDialog(
            manuscript = manuscript,
            activeChapterIndex = activeChapterIndex,
            onUpdateManuscript = onUpdateManuscript,
            onDismiss = { showFindReplaceDialog = false }
        )
    }

    if (showSprintDialog) {
        NovelSprintTrackerDialog(
            manuscript = manuscript,
            onUpdateManuscript = onUpdateManuscript,
            onDismiss = { showSprintDialog = false }
        )
    }

    if (showSnapshotDialog) {
        ChapterSnapshotDialog(
            chapter = currentChapter,
            chapterIndex = activeChapterIndex,
            manuscript = manuscript,
            onUpdateManuscript = onUpdateManuscript,
            onDismiss = { showSnapshotDialog = false }
        )
    }

    if (showScrapsDialog) {
        NovelScrapsGraveyardDialog(
            manuscript = manuscript,
            activeChapterIndex = activeChapterIndex,
            onUpdateManuscript = onUpdateManuscript,
            onDismiss = { showScrapsDialog = false }
        )
    }
}

@Composable
private fun CompactNovelTopBar(
    manuscript: NovelManuscript,
    isBinderVisible: Boolean,
    isTypewriterMode: Boolean,
    isArcadeMode: Boolean = false,
    onToggleBinder: () -> Unit,
    onOpenStudioDrawer: () -> Unit,
    onToggleCorkboard: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onOpenFindReplace: () -> Unit,
    onOpenSprint: () -> Unit,
    onToggleTypewriter: () -> Unit,
    onOpenScraps: () -> Unit,
    onOpenCompiler: () -> Unit,
    onSaveProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Floating Circle Drawer & Dedicated Chapter Binder Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(8.dp, CircleShape, spotColor = ScribeStarNeonRed.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(opticalPillBrush())
                    .border(prismaticGlassBorder(width = 1.dp), CircleShape)
                    .clickable(onClick = onOpenStudioDrawer)
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

            NovelGlassPillButton(
                text = "📑 Binder (${manuscript.chapters.size})",
                onClick = onToggleBinder,
                isHighlight = isBinderVisible
            )
        }

        // Center: Workspace Switcher [📚 Novel] [📌 Board] [📖 Lore]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NovelGlassPillButton(
                text = "📚 Novel",
                onClick = {},
                isPrimary = true
            )
            NovelGlassPillButton(
                text = "📌 Board",
                onClick = onToggleCorkboard
            )
            NovelGlassPillButton(
                text = "📖 Lore",
                onClick = onOpenStoryBible
            )
        }

        CompactNovelTopBarTools(
            isTypewriterMode = isTypewriterMode,
            isArcadeMode = isArcadeMode,
            onOpenFindReplace = onOpenFindReplace,
            onOpenSprint = onOpenSprint,
            onToggleTypewriter = onToggleTypewriter,
            onOpenScraps = onOpenScraps,
            onOpenCompiler = onOpenCompiler,
            onSaveProject = onSaveProject
        )
    }
}

@Composable
private fun CompactNovelTopBarTools(
    isTypewriterMode: Boolean,
    isArcadeMode: Boolean = false,
    onOpenFindReplace: () -> Unit,
    onOpenSprint: () -> Unit,
    onToggleTypewriter: () -> Unit,
    onOpenScraps: () -> Unit,
    onOpenCompiler: () -> Unit,
    onSaveProject: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NovelGlassPillButton(text = "🔍", onClick = onOpenFindReplace)
        NovelGlassPillButton(text = "🎯", onClick = onOpenSprint)
        NovelGlassPillButton(
            text = "📜",
            onClick = onToggleTypewriter,
            isHighlight = isTypewriterMode
        )
        NovelGlassPillButton(text = "🪦", onClick = onOpenScraps)
        if (!isArcadeMode) {
            NovelGlassPillButton(text = "🚀", onClick = onOpenCompiler)
        }
        NovelGlassPillButton(
            text = "💾",
            onClick = onSaveProject,
            isAccent = true
        )
    }
}

@Composable
private fun DesktopNovelTopBar(
    manuscript: NovelManuscript,
    isDictating: Boolean,
    isCorkboardOpen: Boolean,
    isTypewriterMode: Boolean,
    isArcadeMode: Boolean = false,
    onOpenStudioDrawer: () -> Unit,
    onToggleDictation: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onToggleCorkboard: () -> Unit,
    onOpenFindReplace: () -> Unit,
    onOpenSprint: () -> Unit,
    onToggleTypewriter: () -> Unit,
    onOpenScraps: () -> Unit,
    onOpenCompiler: () -> Unit,
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
        // Left: Floating Circle Hamburger + Floating Logo+Title Chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .shadow(8.dp, CircleShape, spotColor = ScribeStarNeonRed.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(opticalPillBrush())
                    .border(prismaticGlassBorder(width = 1.dp), CircleShape)
                    .clickable(onClick = onOpenStudioDrawer)
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
                    .background(opticalPillBrush())
                    .border(prismaticGlassBorder(width = 1.dp), RoundedCornerShape(16.dp))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 42.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.seshat_emblem_logo),
                            contentDescription = "ScribeStar Logo",
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = manuscript.title.ifBlank { "UNTITLED NOVEL" }.uppercase(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }

        // Center: Floating Telemetry Glass Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(opticalPillBrush())
                .border(prismaticGlassBorder(width = 1.dp), RoundedCornerShape(16.dp))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .defaultMinSize(minHeight = 42.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${manuscript.totalWordCount()} Words",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                    Text(text = "•", color = Color(0xFF64748B))
                    Text(
                        text = "~${manuscript.estimatedPages()} Pages",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.5.sp
                        )
                    )
                    Text(text = "•", color = Color(0xFF64748B))
                    Text(
                        text = "~${manuscript.estimatedReadingMinutes()}m Read",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }

        // Right Actions: Floating Glass Pills
        DesktopNovelTopBarActions(
            scrapsCount = manuscript.scraps.size,
            isDictating = isDictating,
            isCorkboardOpen = isCorkboardOpen,
            isTypewriterMode = isTypewriterMode,
            isArcadeMode = isArcadeMode,
            onToggleDictation = onToggleDictation,
            onOpenStoryBible = onOpenStoryBible,
            onToggleCorkboard = onToggleCorkboard,
            onOpenFindReplace = onOpenFindReplace,
            onOpenSprint = onOpenSprint,
            onToggleTypewriter = onToggleTypewriter,
            onOpenScraps = onOpenScraps,
            onOpenCompiler = onOpenCompiler,
            onSaveProject = onSaveProject
        )
    }
}

@Composable
private fun DesktopNovelTopBarActions(
    scrapsCount: Int,
    isDictating: Boolean,
    isCorkboardOpen: Boolean,
    isTypewriterMode: Boolean,
    isArcadeMode: Boolean = false,
    onToggleDictation: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onToggleCorkboard: () -> Unit,
    onOpenFindReplace: () -> Unit,
    onOpenSprint: () -> Unit,
    onToggleTypewriter: () -> Unit,
    onOpenScraps: () -> Unit,
    onOpenCompiler: () -> Unit,
    onSaveProject: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NovelGlassPillButton(
            text = if (isDictating) "Recording" else "Dictate",
            icon = if (isDictating) "🔴" else "🎙️",
            onClick = onToggleDictation,
            isHighlight = isDictating,
            isBig = true
        )
        NovelGlassPillButton(
            text = "Story Bible",
            icon = "📖",
            onClick = onOpenStoryBible,
            isBig = true
        )
        NovelGlassPillButton(
            text = if (isCorkboardOpen) "Corkboard ON" else "Corkboard",
            icon = "📌",
            onClick = onToggleCorkboard,
            isHighlight = isCorkboardOpen,
            isBig = true
        )
        NovelGlassPillButton(
            text = "Find",
            icon = "🔍",
            onClick = onOpenFindReplace,
            isBig = true
        )
        NovelGlassPillButton(
            text = "Goal / Sprint",
            icon = "🎯",
            onClick = onOpenSprint,
            isBig = true
        )
        NovelGlassPillButton(
            text = if (isTypewriterMode) "Typewriter ON" else "Typewriter",
            icon = "📜",
            onClick = onToggleTypewriter,
            isHighlight = isTypewriterMode,
            isBig = true
        )
        NovelGlassPillButton(
            text = "Scraps ($scrapsCount)",
            icon = "🪦",
            onClick = onOpenScraps,
            isBig = true
        )
        if (!isArcadeMode) {
            NovelGlassPillButton(
                text = "Compile",
                icon = "🚀",
                onClick = onOpenCompiler,
                isBig = true
            )
        }
        NovelGlassPillButton(
            text = "Save",
            icon = "💾",
            onClick = onSaveProject,
            isAccent = true,
            isBig = true
        )
    }
}

@Composable
private fun NovelChapterHeaderBar(
    chapter: NovelChapter,
    isTypewriterMode: Boolean,
    isArcadeMode: Boolean = false,
    onOpenSnapshots: () -> Unit,
    onToggleTypewriter: () -> Unit,
    onInsertSceneBreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0x331E293B),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "${chapter.wordCount()} words",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
            if (!isArcadeMode) {
                Spacer(modifier = Modifier.width(6.dp))
                NovelGlassPillButton(
                    text = "Snapshots (${chapter.snapshots.size})",
                    icon = "📸",
                    onClick = onOpenSnapshots
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NovelGlassPillButton(
                text = if (isTypewriterMode) "Typewriter ON" else "Typewriter",
                icon = "📜",
                onClick = onToggleTypewriter,
                isHighlight = isTypewriterMode
            )

            NovelGlassPillButton(
                text = "Scene Break (* * *)",
                icon = "+",
                onClick = onInsertSceneBreak,
                isAccent = true
            )
        }
    }
}

@Composable
private fun NovelGlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isAccent: Boolean = false,
    isHighlight: Boolean = false,
    isBig: Boolean = false,
    icon: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed || isHighlight

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) (if (isBig) 8.dp else 6.dp) else if (isAccent || isPrimary) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "NovelBtnGlow"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.35f else if (isAccent || isPrimary) 0.18f else 0.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "NovelBtnOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.65f else if (isAccent || isPrimary) 0.35f else 0.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "NovelBtnMidAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "NovelBtnScale"
    )

    val cornerRadius = if (isBig) 16.dp else 14.dp
    val hPadding = if (isBig) 16.dp else 12.dp
    val vPadding = if (isBig) 8.dp else 6.dp
    val textFontSize = if (isBig) 12.5.sp else 11.5.sp
    val iconFontSize = if (isBig) 12.5.sp else 11.sp

    val isDark = LocalSeshatDarkTheme.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .defaultMinSize(
                minHeight = if (isBig) 42.dp else 36.dp,
                minWidth = if (isBig) 54.dp else 42.dp
            )
            .shadow(
                elevation = if (isLit || isPrimary) 10.dp else 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = if (isLit) ScribeStarNeonRed else if (isDark) Color.Black else Color(0x3064748B)
            )
            .then(
                if (isLit || isAccent || isPrimary) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = cornerRadius,
                        glowRadius = glowRadius,
                        strokeWidth = if (isLit) 1.4.dp else 1.dp,
                        outerAlpha = outerAlpha,
                        midAlpha = midAlpha,
                        coreAlpha = if (isLit) 1.0f else 0.75f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                opticalPillBrush(
                    isLit = isLit,
                    isPrimary = isPrimary,
                    isAccent = isAccent,
                    isDark = isDark
                )
            )
            .border(
                prismaticGlassBorder(
                    width = if (isLit) 1.4.dp else 0.8.dp,
                    isLit = isLit,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = hPadding, vertical = vPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = iconFontSize)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isHighlight || isAccent || isLit || isPrimary) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)),
                    fontWeight = if (isHighlight || isLit || isPrimary) FontWeight.Bold else FontWeight.Medium,
                    fontSize = textFontSize
                )
            )
        }
    }
}

@Composable
private fun NovelChapterCardItem(
    chapter: NovelChapter,
    index: Int,
    isSelected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSelect: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isSelected || isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isSelected) 5.dp else if (isHovered) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "ChapterGlow"
    )

    val isDark = LocalSeshatDarkTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .then(
                if (isLit) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 6.dp,
                        glowRadius = glowRadius,
                        strokeWidth = if (isSelected) 1.2.dp else 0.9.dp,
                        outerAlpha = if (isSelected) 0.32f else 0.16f,
                        midAlpha = if (isSelected) 0.60f else 0.32f,
                        coreAlpha = if (isSelected) 1.0f else 0.70f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(6.dp))
            .background(
                opticalPillBrush(
                    isLit = isSelected,
                    isAccent = isHovered,
                    isDark = isDark
                )
            )
            .border(
                prismaticGlassBorder(
                    width = if (isSelected) 1.2.dp else 0.8.dp,
                    isLit = isLit,
                    isDark = isDark
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title.ifBlank { "Untitled Chapter" },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A))
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${chapter.wordCount()} words",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                )
            }

            if (isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canMoveUp) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                tint = ScribeStarNeonRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (canMoveDown) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                tint = ScribeStarNeonRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scrivener-style Left Binder panel showing parts, chapters, word counts, and reordering.
 */
@Composable
private fun NovelBinderPane(
    manuscript: NovelManuscript,
    selectedChapterIndex: Int,
    onSelectChapter: (Int) -> Unit,
    onAddChapter: () -> Unit,
    onMoveChapterUp: (Int) -> Unit,
    onMoveChapterDown: (Int) -> Unit,
    onCloseBinder: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onCloseBinder != null) {
                    IconButton(
                        onClick = onCloseBinder,
                        modifier = Modifier.size(28.dp).padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Binder",
                            tint = ScribeStarNeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "MANUSCRIPT BINDER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                )
            }

            NovelGlassPillButton(
                text = "Chapter",
                icon = "+",
                onClick = onAddChapter,
                isAccent = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = ScribeStarNeonRed.copy(alpha = 0.35f))
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(manuscript.chapters) { idx, chapter ->
                NovelChapterCardItem(
                    chapter = chapter,
                    index = idx,
                    isSelected = idx == selectedChapterIndex,
                    canMoveUp = idx > 0,
                    canMoveDown = idx < manuscript.chapters.lastIndex,
                    onSelect = { onSelectChapter(idx) },
                    onMoveUp = { onMoveChapterUp(idx) },
                    onMoveDown = { onMoveChapterDown(idx) }
                )
            }
        }
    }
}

/**
 * Floating Contextual Action Bar that appears when novel paragraphs are highlighted.
 * Supports Copying, Converting to Dialogue, Converting to Narration, Deletion, and Deselection.
 */
@Composable
private fun NovelHighlightActionBar(
    highlightedCount: Int,
    wordCount: Int,
    onCopy: () -> Unit,
    onConvertToDialogue: () -> Unit,
    onConvertToNarration: () -> Unit,
    onSendToGraveyard: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(opticalGlassBrush(glassOpacity = 0.85f))
            .border(prismaticGlassBorder(width = 1.2.dp, isLit = true), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Text(
                text = "📌 $highlightedCount paragraphs ($wordCount words)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                )
            )

            NovelGlassPillButton(text = "COPY", icon = "📋", onClick = onCopy)
            NovelGlassPillButton(text = "TO DIALOGUE", icon = "💬", onClick = onConvertToDialogue)
            NovelGlassPillButton(text = "TO NARRATION", icon = "📖", onClick = onConvertToNarration)
            NovelGlassPillButton(text = "TO GRAVEYARD", icon = "🪦", onClick = onSendToGraveyard, isAccent = true)
            NovelGlassPillButton(text = "DELETE", icon = "🗑️", onClick = onDelete, isHighlight = true)

            IconButton(
                onClick = onClear,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Highlight",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Smart Bottom HUD for Novel Writing.
 * - On compact screen (Pixel 11 Pro): 2-tier touch layout with element selector pills,
 *   one-touch Dictation, on-demand Keyboard toggle, Enter, Del, and live word counter.
 * - On tablet / desktop (Pixel Tablet): Spacious 1-tier dock with element chips,
 *   keyboard toggle, chapter metrics, and typography info.
 */
@Composable
private fun NovelBottomHud(
    isCompactScreen: Boolean,
    currentChapter: NovelChapter,
    focusedParagraph: NovelParagraph?,
    isDictating: Boolean,
    isKeyboardActive: Boolean,
    onToggleKeyboard: () -> Unit,
    onToggleDictation: () -> Unit,
    onSetParagraphType: (NovelParagraphType) -> Unit,
    onInsertSceneBreak: () -> Unit,
    onInsertParagraph: () -> Unit,
    onBackspace: () -> Unit,
    onTabPressed: () -> Unit = {},
    onSaveProject: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentType = focusedParagraph?.type ?: NovelParagraphType.NARRATIVE

    if (isCompactScreen) {
        NovelCompactBottomHud(
            currentType = currentType,
            currentChapter = currentChapter,
            isDictating = isDictating,
            isKeyboardActive = isKeyboardActive,
            onToggleKeyboard = onToggleKeyboard,
            onToggleDictation = onToggleDictation,
            onSetParagraphType = onSetParagraphType,
            onInsertSceneBreak = onInsertSceneBreak,
            onInsertParagraph = onInsertParagraph,
            onBackspace = onBackspace,
            onTabPressed = onTabPressed,
            onSaveProject = onSaveProject,
            onToggleTheme = onToggleTheme,
            modifier = modifier
                .navigationBarsPadding()
                .imePadding()
                .fillMaxWidth()
        )
    } else {
        NovelDesktopBottomHud(
            currentType = currentType,
            currentChapter = currentChapter,
            isKeyboardActive = isKeyboardActive,
            onToggleKeyboard = onToggleKeyboard,
            onSetParagraphType = onSetParagraphType,
            onInsertSceneBreak = onInsertSceneBreak,
            onTabPressed = onTabPressed,
            onSaveProject = onSaveProject,
            onToggleTheme = onToggleTheme,
            modifier = modifier
                .navigationBarsPadding()
                .fillMaxWidth()
        )
    }
}

@Composable
private fun NovelCompactBottomHud(
    currentType: NovelParagraphType,
    currentChapter: NovelChapter,
    isDictating: Boolean,
    isKeyboardActive: Boolean,
    onToggleKeyboard: () -> Unit,
    onToggleDictation: () -> Unit,
    onSetParagraphType: (NovelParagraphType) -> Unit,
    onInsertSceneBreak: () -> Unit,
    onInsertParagraph: () -> Unit,
    onBackspace: () -> Unit,
    onTabPressed: () -> Unit,
    onSaveProject: () -> Unit,
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        // Tier 1: Scrollable Element Selection Pills
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp)
        ) {
            val isNarrative = currentType == NovelParagraphType.NARRATIVE
            NovelGlassPillButton(
                text = "Narration",
                icon = "📖",
                onClick = { onSetParagraphType(NovelParagraphType.NARRATIVE) },
                isHighlight = isNarrative
            )

            val isDialogue = currentType == NovelParagraphType.DIALOGUE
            NovelGlassPillButton(
                text = "Dialogue",
                icon = "💬",
                onClick = { onSetParagraphType(NovelParagraphType.DIALOGUE) },
                isHighlight = isDialogue
            )

            NovelGlassPillButton(
                text = "* * * Break",
                onClick = onInsertSceneBreak
            )
        }

        // Tier 2: Controls & Dictation & Keyboard Toggle
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
                    .weight(1f, fill = false)
                    .horizontalScroll(rememberScrollState())
            ) {
                NovelGlassPillButton(
                    text = if (isDictating) "Recording" else "Dictate",
                    icon = if (isDictating) "🔴" else "🎙️",
                    onClick = onToggleDictation,
                    isHighlight = isDictating
                )

                NovelGlassPillButton(
                    text = if (isKeyboardActive) "On" else "Off",
                    icon = "⌨️",
                    onClick = onToggleKeyboard,
                    isHighlight = isKeyboardActive
                )

                NovelGlassPillButton(
                    text = "Tab",
                    icon = "⇥",
                    onClick = onTabPressed
                )

                val enterLabel = when (currentType) {
                    NovelParagraphType.CHAPTER_HEADER -> "↵ Narrative"
                    NovelParagraphType.DIALOGUE -> "↵ Quote"
                    else -> "↵ Para"
                }
                NovelGlassPillButton(
                    text = enterLabel,
                    onClick = onInsertParagraph
                )

                NovelGlassPillButton(
                    text = "Del",
                    icon = "⌫",
                    onClick = onBackspace
                )

                NovelGlassPillButton(
                    text = "Save",
                    icon = "💾",
                    onClick = onSaveProject,
                    isAccent = true
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

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
                    text = "Ch.${currentChapter.chapterNumber} • ${currentChapter.wordCount()}w",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            FloatingThemeTogglePill(
                isDark = isDark,
                onToggleTheme = onToggleTheme
            )
        }
    }
}

@Composable
private fun NovelDesktopBottomHud(
    currentType: NovelParagraphType,
    currentChapter: NovelChapter,
    isKeyboardActive: Boolean,
    onToggleKeyboard: () -> Unit,
    onSetParagraphType: (NovelParagraphType) -> Unit,
    onInsertSceneBreak: () -> Unit,
    onTabPressed: () -> Unit,
    onSaveProject: () -> Unit,
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalSeshatDarkTheme.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isNarrative = currentType == NovelParagraphType.NARRATIVE
            NovelGlassPillButton(
                text = "Narration",
                icon = "📖",
                onClick = { onSetParagraphType(NovelParagraphType.NARRATIVE) },
                isHighlight = isNarrative,
                isBig = true
            )

            val isDialogue = currentType == NovelParagraphType.DIALOGUE
            NovelGlassPillButton(
                text = "Dialogue",
                icon = "💬",
                onClick = { onSetParagraphType(NovelParagraphType.DIALOGUE) },
                isHighlight = isDialogue,
                isBig = true
            )

            NovelGlassPillButton(
                text = "Scene Break (* * *)",
                icon = "+",
                onClick = onInsertSceneBreak,
                isBig = true
            )

            NovelGlassPillButton(
                text = "Tab",
                icon = "⇥",
                onClick = onTabPressed,
                isBig = true
            )

            NovelGlassPillButton(
                text = "Save",
                icon = "💾",
                onClick = onSaveProject,
                isAccent = true,
                isBig = true
            )

            NovelGlassPillButton(
                text = if (isKeyboardActive) "Keyboard ON" else "Keyboard",
                icon = "⌨️",
                onClick = onToggleKeyboard,
                isHighlight = isKeyboardActive,
                isBig = true
            )
        }

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
                    Text(
                        text = "Chapter ${currentChapter.chapterNumber}: ${currentChapter.wordCount()} words",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 11.5.sp
                        )
                    )
                    Text(text = "•", color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                    Text(
                        text = "Garamond 12pt",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.Medium,
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

/**
 * Project-Wide and Chapter-Level Find & Replace modal for novelists.
 */
@Composable
fun NovelFindReplaceDialog(
    manuscript: NovelManuscript,
    activeChapterIndex: Int,
    onUpdateManuscript: (NovelManuscript) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var matchWholeWord by remember { mutableStateOf(false) }
    var currentChapterOnly by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    val currentChapter = manuscript.chapters.getOrNull(activeChapterIndex) ?: NovelChapter()

    fun countMatches(targetChapters: List<NovelChapter>): Int {
        if (searchQuery.isBlank()) return 0
        val pattern = try {
            val q = Regex.escape(searchQuery)
            if (matchWholeWord) {
                if (matchCase) Regex("\\b$q\\b") else Regex("\\b$q\\b", RegexOption.IGNORE_CASE)
            } else {
                if (matchCase) Regex(q) else Regex(q, RegexOption.IGNORE_CASE)
            }
        } catch (_: Exception) {
            return 0
        }
        return targetChapters.sumOf { ch ->
            ch.paragraphs.sumOf { p -> pattern.findAll(p.text).count() }
        }
    }

    val matchCount = countMatches(if (currentChapterOnly) listOf(currentChapter) else manuscript.chapters)

    fun performReplace(allAcrossProject: Boolean) {
        if (searchQuery.isBlank()) return
        val pattern = try {
            val q = Regex.escape(searchQuery)
            if (matchWholeWord) {
                if (matchCase) Regex("\\b$q\\b") else Regex("\\b$q\\b", RegexOption.IGNORE_CASE)
            } else {
                if (matchCase) Regex(q) else Regex(q, RegexOption.IGNORE_CASE)
            }
        } catch (_: Exception) {
            return
        }

        var totalReplaced = 0
        val updatedChapters = manuscript.chapters.mapIndexed { idx, ch ->
            if (allAcrossProject || idx == activeChapterIndex) {
                val updatedParas = ch.paragraphs.map { p ->
                    val occurrences = pattern.findAll(p.text).count()
                    if (occurrences > 0) {
                        totalReplaced += occurrences
                        p.copy(text = p.text.replace(pattern, replaceQuery))
                    } else p
                }
                ch.copy(paragraphs = updatedParas)
            } else ch
        }

        if (totalReplaced > 0) {
            val updated = manuscript.copy(chapters = updatedChapters)
            onUpdateManuscript(updated)
            NovelRepository.saveManuscript(context, updated)
            statusMessage = "✓ Replaced $totalReplaced occurrence${if (totalReplaced > 1) "s" else ""}."
            Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
        } else {
            statusMessage = "No occurrences found to replace."
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = ObsidianBlack,
            border = BorderStroke(1.5.dp, ElectrumGold.copy(alpha = 0.6f)),
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍 FIND & REPLACE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ScreenplayTextMuted)
                    }
                }

                HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))

                Text("Search For:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; statusMessage = "" },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(ElectrumGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Replace With:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = replaceQuery,
                    onValueChange = { replaceQuery = it; statusMessage = "" },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(ElectrumGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = matchCase,
                            onCheckedChange = { matchCase = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Match Case", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = matchWholeWord,
                            onCheckedChange = { matchWholeWord = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Whole Word", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = currentChapterOnly,
                            onCheckedChange = { currentChapterOnly = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("This Chapter Only", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (searchQuery.isNotBlank()) {
                    Text(
                        text = if (matchCount > 0) "Found $matchCount match${if (matchCount > 1) "es" else ""} ${if (currentChapterOnly) "in Chapter ${activeChapterIndex + 1}" else "across entire manuscript"}" else "No matches found",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (matchCount > 0) ElectrumGold else ScreenplayTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF4ADE80),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, ScreenplayTextMuted.copy(alpha = 0.5f))
                    ) {
                        Text("Close", color = ScreenplayTextMuted)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedButton(
                        onClick = { performReplace(allAcrossProject = false) },
                        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.7f)),
                        enabled = searchQuery.isNotBlank() && matchCount > 0
                    ) {
                        Text("Replace in Chapter", color = ElectrumGold)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { performReplace(allAcrossProject = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(8.dp),
                        enabled = searchQuery.isNotBlank() && matchCount > 0
                    ) {
                        Text("Replace All in Project", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Daily Word Count Goal and NaNoWriMo Sprint Pomodoro Tracker.
 */
@Composable
fun NovelSprintTrackerDialog(
    manuscript: NovelManuscript,
    onUpdateManuscript: (NovelManuscript) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var dailyGoal by remember { mutableStateOf(manuscript.dailyWordCountGoal) }
    val totalWords = manuscript.totalWordCount()
    val wordsToday = (totalWords - (totalWords - manuscript.sessionWordCount)).coerceAtLeast(totalWords)

    var sprintMinutes by remember { mutableStateOf(15) }
    var remainingSeconds by remember { mutableStateOf(15 * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var sprintStartWords by remember { mutableStateOf(totalWords) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        }
        if (remainingSeconds == 0 && isTimerRunning) {
            isTimerRunning = false
            Toast.makeText(context, "🔔 Word Sprint Complete! Great sprint!", Toast.LENGTH_LONG).show()
        }
    }

    val progress = if (dailyGoal > 0) (wordsToday.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    val sprintWordsAdded = (totalWords - sprintStartWords).coerceAtLeast(0)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = ObsidianBlack,
            border = BorderStroke(1.5.dp, ElectrumGold.copy(alpha = 0.6f)),
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 DAILY GOAL & SPRINT TRACKER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ScreenplayTextMuted)
                    }
                }

                HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))

                Text(
                    text = "DAILY WORD COUNT TARGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500, 1000, 1667, 2000, 2500).forEach { preset ->
                        val isSel = dailyGoal == preset
                        Surface(
                            color = if (isSel) ElectrumGold.copy(alpha = 0.2f) else ObsidianSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isSel) ElectrumGold else ObsidianSurfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    dailyGoal = preset
                                    val updated = manuscript.copy(dailyWordCountGoal = preset)
                                    onUpdateManuscript(updated)
                                    NovelRepository.saveManuscript(context, updated)
                                }
                        ) {
                            Text(
                                text = if (preset == 1667) "1,667\n(NaNo)" else "$preset",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) ElectrumGold else ScreenplayTextMuted,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$wordsToday / $dailyGoal words today",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = ElectrumGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ElectrumGold,
                            trackColor = ObsidianBlack
                        )

                        if (wordsToday >= dailyGoal) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🏆 NaNoWriMo Daily Goal Reached! Outstanding work, author!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WORD SPRINT TIMER (POMODORO)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = if (isTimerRunning) ElectrumGold else Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        )

                        Text(
                            text = "Words drafted this sprint: +$sprintWordsAdded",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = ScreenplayTextMuted
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15, 20, 25).forEach { m ->
                                Surface(
                                    color = if (sprintMinutes == m) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (sprintMinutes == m) ElectrumGold else ObsidianSurfaceVariant),
                                    modifier = Modifier.clickable(enabled = !isTimerRunning) {
                                        sprintMinutes = m
                                        remainingSeconds = m * 60
                                        sprintStartWords = totalWords
                                    }
                                ) {
                                    Text(
                                        text = "${m}m",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (sprintMinutes == m) ElectrumGold else ScreenplayTextMuted,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (!isTimerRunning) {
                                        sprintStartWords = totalWords
                                    }
                                    isTimerRunning = !isTimerRunning
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTimerRunning) Color(0xFFEF4444) else ElectrumGold,
                                    contentColor = ObsidianBlack
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isTimerRunning) "⏸ Pause" else "▶ Start Sprint",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    isTimerRunning = false
                                    remainingSeconds = sprintMinutes * 60
                                    sprintStartWords = totalWords
                                },
                                border = BorderStroke(1.dp, ObsidianSurfaceVariant)
                            ) {
                                Text("Reset", color = ScreenplayTextMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Chapter Snapshot & Version History Dialog.
 */
@Composable
fun ChapterSnapshotDialog(
    chapter: NovelChapter,
    chapterIndex: Int,
    manuscript: NovelManuscript,
    onUpdateManuscript: (NovelManuscript) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var snapshotLabel by remember { mutableStateOf("") }

    fun takeSnapshot() {
        val label = snapshotLabel.ifBlank {
            "Draft • ${SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date())}"
        }
        val newSnapshot = ChapterSnapshot(
            label = label,
            paragraphs = chapter.paragraphs,
            wordCount = chapter.wordCount(),
            timestamp = System.currentTimeMillis()
        )
        val updatedCh = chapter.copy(snapshots = listOf(newSnapshot) + chapter.snapshots)
        val updatedChapters = manuscript.chapters.mapIndexed { idx, ch ->
            if (idx == chapterIndex) updatedCh else ch
        }
        val updated = manuscript.copy(chapters = updatedChapters)
        onUpdateManuscript(updated)
        NovelRepository.saveManuscript(context, updated)
        snapshotLabel = ""
        Toast.makeText(context, "📸 Snapshot saved!", Toast.LENGTH_SHORT).show()
    }

    fun restoreSnapshot(snapshot: ChapterSnapshot) {
        val updatedCh = chapter.copy(paragraphs = snapshot.paragraphs)
        val updatedChapters = manuscript.chapters.mapIndexed { idx, ch ->
            if (idx == chapterIndex) updatedCh else ch
        }
        val updated = manuscript.copy(chapters = updatedChapters)
        onUpdateManuscript(updated)
        NovelRepository.saveManuscript(context, updated)
        Toast.makeText(context, "Reverted Chapter ${chapterIndex + 1} to '${snapshot.label}'", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    fun deleteSnapshot(snapId: String) {
        val updatedCh = chapter.copy(snapshots = chapter.snapshots.filter { it.id != snapId })
        val updatedChapters = manuscript.chapters.mapIndexed { idx, ch ->
            if (idx == chapterIndex) updatedCh else ch
        }
        val updated = manuscript.copy(chapters = updatedChapters)
        onUpdateManuscript(updated)
        NovelRepository.saveManuscript(context, updated)
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp)),
            color = ObsidianBlack,
            border = BorderStroke(1.5.dp, ElectrumGold.copy(alpha = 0.6f)),
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📸 CHAPTER SNAPSHOTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ElectrumGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "${chapter.title} • Version Control & Revert History",
                            style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ScreenplayTextMuted)
                    }
                }

                HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = snapshotLabel,
                        onValueChange = { snapshotLabel = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = SolidColor(ElectrumGold),
                        modifier = Modifier
                            .weight(1f)
                            .background(ObsidianSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, ElectrumGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )

                    Button(
                        onClick = { takeSnapshot() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📸 Take Snapshot", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SAVED SNAPSHOTS (${chapter.snapshots.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (chapter.snapshots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No snapshots taken yet for this chapter.\nTake a snapshot before major rewrites to easily revert anytime.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextMuted),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chapter.snapshots) { snap ->
                            val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(Date(snap.timestamp))
                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = snap.label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "$dateStr • ${snap.wordCount} words • ${snap.paragraphs.size} paragraphs",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = ScreenplayTextMuted,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { restoreSnapshot(snap) },
                                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.7f))
                                        ) {
                                            Text("↺ Restore", color = ElectrumGold, style = MaterialTheme.typography.labelSmall)
                                        }

                                        IconButton(
                                            onClick = { deleteSnapshot(snap.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
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
}

/**
 * The "Graveyard" / Scraps Bin Dialog for preserving trimmed prose, dialogue, and ideas.
 */
@Composable
fun NovelScrapsGraveyardDialog(
    manuscript: NovelManuscript,
    activeChapterIndex: Int,
    onUpdateManuscript: (NovelManuscript) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentChapter = manuscript.chapters.getOrNull(activeChapterIndex) ?: NovelChapter()

    fun restoreScrap(scrap: NovelScrap) {
        val newParas = scrap.content.split(Regex("\n\n+")).filter { it.isNotBlank() }.map {
            NovelParagraph(type = NovelParagraphType.NARRATIVE, text = it.trim())
        }
        val updatedCh = currentChapter.copy(paragraphs = currentChapter.paragraphs + newParas)
        val updatedChapters = manuscript.chapters.mapIndexed { idx, ch ->
            if (idx == activeChapterIndex) updatedCh else ch
        }
        val updatedScraps = manuscript.scraps.filter { it.id != scrap.id }
        val updated = manuscript.copy(chapters = updatedChapters, scraps = updatedScraps)
        onUpdateManuscript(updated)
        NovelRepository.saveManuscript(context, updated)
        Toast.makeText(context, "Restored scrap to ${currentChapter.title}!", Toast.LENGTH_SHORT).show()
    }

    fun deleteScrap(scrapId: String) {
        val updated = manuscript.copy(scraps = manuscript.scraps.filter { it.id != scrapId })
        onUpdateManuscript(updated)
        NovelRepository.saveManuscript(context, updated)
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp)),
            color = ObsidianBlack,
            border = BorderStroke(1.5.dp, ElectrumGold.copy(alpha = 0.6f)),
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🪦 THE GRAVEYARD (SCRAPS BIN)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ElectrumGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Preserved Cut Scenes, Trimmed Dialogue & Ideas (${manuscript.scraps.size} scraps)",
                            style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ScreenplayTextMuted)
                    }
                }

                HorizontalDivider(color = ObsidianSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))

                if (manuscript.scraps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "The Graveyard is empty.\nWhen you cut or delete paragraphs from your manuscript,\nthey are preserved here so you never lose writing.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextMuted),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(manuscript.scraps) { scrap ->
                            val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(Date(scrap.deletedAt))
                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = scrap.title,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = ElectrumGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${scrap.wordCount} words • $dateStr",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = ScreenplayTextMuted,
                                                fontSize = 10.5.sp
                                            )
                                        )
                                    }

                                    if (scrap.originalChapterTitle.isNotBlank()) {
                                        Text(
                                            text = "Cut from: ${scrap.originalChapterTitle}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = ScreenplayTextMuted,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        color = ObsidianBlack,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = scrap.content,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Serif
                                            ),
                                            maxLines = 6,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(scrap.content))
                                                Toast.makeText(context, "Copied scrap to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            border = BorderStroke(1.dp, ScreenplayTextMuted.copy(alpha = 0.5f))
                                        ) {
                                            Text("📋 Copy", color = ScreenplayTextMuted, style = MaterialTheme.typography.labelSmall)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { restoreScrap(scrap) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = ObsidianBlack),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("↺ Restore to Chapter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { deleteScrap(scrap.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
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
}


