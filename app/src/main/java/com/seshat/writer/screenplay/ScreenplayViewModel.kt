package com.seshat.writer.screenplay

import androidx.lifecycle.ViewModel
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.DualDialogueSide
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.analytics.ScreenplayAnalyticsEngine
import com.seshat.writer.analytics.ScriptAnalytics
import com.seshat.writer.analytics.LocationAnalytics
import com.seshat.writer.analytics.CastSceneMatrix
import com.seshat.writer.titlepage.ScreenplayDocumentStore
import com.seshat.writer.titlepage.TitlePageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiConfig
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.ai.StudioCoverageReport
import com.seshat.writer.ai.StudioCoveragePdfExporter
import com.seshat.writer.ai.TableReadManager
import com.seshat.writer.dictation.ScreenplayDictationManager
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.bible.StoryBibleRepository
import com.seshat.writer.corkboard.CorkboardAct
import com.seshat.writer.corkboard.CorkboardCard
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.corkboard.CorkboardRepository
import com.seshat.writer.corkboard.CorkboardTemplate
import com.seshat.writer.corkboard.SceneCard
import com.seshat.writer.breakdown.ProductionBreakdownEngine
import com.seshat.writer.breakdown.ProductionBreakdownPdfExporter
import com.seshat.writer.breakdown.ScriptBreakdownSummary
import com.seshat.writer.export.ScriptFileManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.seshat.writer.library.InitialWorkspaceView
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.library.ScriptLibraryManager
import com.seshat.writer.library.ScriptTemplate
import com.seshat.writer.importer.ScriptImporter
import com.seshat.writer.novel.NovelChapter
import com.seshat.writer.novel.NovelManuscript
import com.seshat.writer.novel.NovelParagraph
import com.seshat.writer.novel.NovelRepository
import com.seshat.writer.tv.TvEpisode
import com.seshat.writer.tv.TvSeriesProject
import com.seshat.writer.tv.TvSeriesRepository
import com.seshat.writer.ui.theme.ThemeMode
import com.seshat.writer.ui.theme.ThemePreferences
import com.seshat.writer.templates.ProjectTemplate
import com.seshat.writer.templates.TemplateRegistry
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.bundle.SeshatBundleManager

enum class CursorTargetType {
    DEFAULT,
    START,
    END,
    TOP_LINE_COLUMN,
    BOTTOM_LINE_COLUMN
}

data class CursorTarget(
    val type: CursorTargetType = CursorTargetType.DEFAULT,
    val column: Int = 0
)

data class SearchMatch(
    val blockId: String,
    val startIndex: Int,
    val endIndex: Int
)

enum class FindMode {
    FIND_REPLACE,
    RENAME_CHARACTER
}

enum class ScreenplayViewMode {
    PAGE_VIEW,
    INLINE_VIEW
}

data class ScreenplayUiState(
    val title: String = "",
    val author: String = "",
    val blocks: List<ScreenplayBlock> = listOf(
        ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
    ),
    val focusedBlockId: String = "",
    val cursorTarget: CursorTarget = CursorTarget(),
    val highlightedBlockIds: Set<String> = emptySet(),
    val selectionAnchorBlockId: String? = null,
    val viewMode: ScreenplayViewMode = ScreenplayViewMode.PAGE_VIEW,
    val zoomLevel: Float = 1.0f,
    val isZenMode: Boolean = false,
    val isSidebarExpanded: Boolean = true,
    val pageCount: Int = 1,
    val estimatedMinutes: Int = 1,
    val totalLines: Int = 1,
    val pageBreakBeforeBlockIds: Set<String> = emptySet(),
    val blockPageNumbers: Map<String, Int> = emptyMap(),
    val sceneCount: Int = 1,
    val wordCount: Int = 0,
    val selectedSceneIndex: Int = 0,
    val scenes: List<SceneItem> = emptyList(),
    val characters: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val showSceneNumbers: Boolean = false,
    val blockSceneNumbers: Map<String, String> = emptyMap(),
    val isFindReplaceVisible: Boolean = false,
    val findMode: FindMode = FindMode.FIND_REPLACE,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val matchCase: Boolean = false,
    val matchWholeWord: Boolean = false,
    val searchMatches: List<SearchMatch> = emptyList(),
    val currentMatchIndex: Int = -1,
    val renameOldName: String = "",
    val renameNewName: String = "",
    val renameCuesOnly: Boolean = false,
    val showWriterNotes: Boolean = true,
    val notesCount: Int = 0,
    val isReorderMode: Boolean = false,
    val currentRevisionColor: RevisionColor = RevisionColor.WHITE,
    val isRevisionTrackingEnabled: Boolean = false,
    val revisionDate: String = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date()),
    val revisedBlocksCount: Int = 0,
    val showColoredDraftTint: Boolean = false,
    val isReportsDialogOpen: Boolean = false,
    val isRevisionDialogOpen: Boolean = false,
    val isSceneNumberingLocked: Boolean = false,
    val lockedSceneNumbers: Map<String, String> = emptyMap(),
    val reportsSelectedTab: Int = 0,
    val isSettingsDialogOpen: Boolean = false,
    val isScriptDoctorDialogOpen: Boolean = false,
    val isTableReadActive: Boolean = false,
    val isInlineAiPolishOpen: Boolean = false,
    val inlineAiTargetBlockId: String? = null,
    val studioCoverageReport: StudioCoverageReport? = null,
    val isStudioCoverageLoading: Boolean = false,
    val isDraftDiffOpen: Boolean = false,
    val isLibraryOpen: Boolean = false,
    val isDictating: Boolean = false,
    val dictationLiveText: String = "",
    val dictationAudioRms: Float = 0f,
    val isDictationFormatting: Boolean = false,
    val dictationError: String? = null,
    val isStoryBibleOpen: Boolean = false,
    val storyBible: StoryBible = StoryBible(),
    val isCorkboardOpen: Boolean = false,
    val isCorkboardSideBySide: Boolean = true,
    val corkboardDoc: CorkboardDocument = CorkboardDocument(),
    val sceneCards: List<SceneCard> = emptyList(),
    val isStartMenuOpen: Boolean = true,
    val projectFormat: ProjectFormat = ProjectFormat.SCREENPLAY,
    val screenplayType: ScreenplayType = ScreenplayType.FEATURE,
    val novelManuscript: NovelManuscript = NovelManuscript(title = "UNTITLED NOVEL"),
    val activeChapterIndex: Int = 0,
    val isCompilerDialogOpen: Boolean = false,
    val tvSeriesProject: TvSeriesProject? = null,
    val isWritersRoomOpen: Boolean = false,
    val isStudioDrawerOpen: Boolean = false,
    val isCoverCreatorOpen: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DARK
)

class ScreenplayViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenplayUiState())
    val uiState: StateFlow<ScreenplayUiState> = _uiState.asStateFlow()

    init {
        val initialBlock = ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
        val currentTitleData = ScreenplayDocumentStore.titlePage.value
        _uiState.update {
            buildUpdatedState(
                it.copy(
                    title = currentTitleData.title,
                    author = currentTitleData.author,
                    blocks = listOf(initialBlock),
                    focusedBlockId = initialBlock.id
                ),
                listOf(initialBlock),
                focusedBlockId = initialBlock.id
            )
        }
        viewModelScope.launch {
            ScreenplayDocumentStore.titlePage.collect { titleData ->
                _uiState.update { current ->
                    if (current.title != titleData.title || current.author != titleData.author) {
                        current.copy(
                            title = titleData.title,
                            author = titleData.author,
                            storyBible = if (current.storyBible.scriptTitle.isNotBlank()) {
                                current.storyBible.copy(scriptTitle = titleData.title)
                            } else current.storyBible,
                            corkboardDoc = if (current.corkboardDoc.scriptTitle.isNotBlank()) {
                                current.corkboardDoc.copy(scriptTitle = titleData.title)
                            } else current.corkboardDoc
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    companion object {
        private val SCENE_HEADING_PREFIXES = listOf(
            "INT.",
            "EXT.",
            "I/E.",
            "INT/EXT.",
            "INT./EXT.",
            "I./E.",
            "EST.",
            "INT ",
            "EXT ",
            "I/E ",
            "INT/EXT ",
            "EST ",
            "OVER BLACK",
            "FADE IN:",
            "FADE IN"
        )

        fun isSceneHeadingPrefix(text: String): Boolean {
            val trimmed = text.trimStart().uppercase(Locale.US)
            return SCENE_HEADING_PREFIXES.any { trimmed.startsWith(it) }
        }

        fun isFadeIn(text: String): Boolean {
            val trimmed = text.trim().uppercase(Locale.US)
            return trimmed == "FADE IN:" || trimmed == "FADE IN" || trimmed.startsWith("FADE IN:") || trimmed.startsWith("FADE IN ")
        }

        fun isCountableSceneHeading(block: ScreenplayBlock): Boolean {
            if (block.type != BlockType.SCENE_HEADING) return false
            if (isFadeIn(block.text)) return false
            return true
        }

        fun formatSlugline(upperText: String): String {
            val trimmed = upperText.trim()
            return when {
                trimmed == "INT." || trimmed == "INT" -> "INT. "
                trimmed == "EXT." || trimmed == "EXT" -> "EXT. "
                trimmed == "I/E." || trimmed == "I/E" -> "I/E. "
                trimmed == "INT/EXT." || trimmed == "INT/EXT" || trimmed == "INT./EXT." -> "INT/EXT. "
                trimmed == "I./E." -> "I/E. "
                trimmed == "EST." || trimmed == "EST" -> "EST. "
                trimmed == "OVER BLACK" -> "OVER BLACK"
                isFadeIn(trimmed) -> "FADE IN:"
                else -> upperText
            }
        }

        fun cleanCharacterName(text: String): String {
            var result = text.trim()
            while (result.endsWith(")") && result.contains("(")) {
                val lastOpen = result.lastIndexOf('(')
                if (lastOpen > 0) {
                    result = result.substring(0, lastOpen).trim()
                } else {
                    break
                }
            }
            return result
        }

        private val VO_REGEX = Regex("^(.*?)(?:\\s+\\(?|\\()V\\.?O\\.?\\)?$")
        private val OS_REGEX = Regex("^(.*?)(?:\\s+\\(?|\\()O\\.?S\\.?\\)?$")
        private val OC_REGEX = Regex("^(.*?)(?:\\s+\\(?|\\()O\\.?C\\.?\\)?$")
        private val CONTD_REGEX = Regex("^(.*?)(?:\\s+\\(?|\\()CONT['’]?D\\)?$")

        fun formatCharacterText(upperText: String): String {
            return when {
                VO_REGEX.matches(upperText) -> {
                    val name = VO_REGEX.find(upperText)!!.groupValues[1].trim()
                    if (name.isNotEmpty()) "$name (V.O.)" else upperText
                }
                OS_REGEX.matches(upperText) -> {
                    val name = OS_REGEX.find(upperText)!!.groupValues[1].trim()
                    if (name.isNotEmpty()) "$name (O.S.)" else upperText
                }
                OC_REGEX.matches(upperText) -> {
                    val name = OC_REGEX.find(upperText)!!.groupValues[1].trim()
                    if (name.isNotEmpty()) "$name (O.C.)" else upperText
                }
                CONTD_REGEX.matches(upperText) -> {
                    val name = CONTD_REGEX.find(upperText)!!.groupValues[1].trim()
                    if (name.isNotEmpty()) "$name (CONT'D)" else upperText
                }
                else -> upperText
            }
        }

        fun shouldAppendContd(blocks: List<ScreenplayBlock>, currentIndex: Int, characterName: String): Boolean {
            val cleanCurrent = cleanCharacterName(characterName).uppercase(Locale.US).trim()
            if (cleanCurrent.isEmpty()) return false

            var lastSpeakerInScene: String? = null
            for (i in (currentIndex - 1) downTo 0) {
                val b = blocks[i]
                if (b.type == BlockType.SCENE_HEADING) {
                    break
                }
                if (b.type == BlockType.CHARACTER) {
                    val speakerName = cleanCharacterName(b.text).uppercase(Locale.US).trim()
                    if (speakerName.isNotEmpty()) {
                        lastSpeakerInScene = speakerName
                        break
                    }
                }
            }
            return lastSpeakerInScene != null && lastSpeakerInScene == cleanCurrent
        }

        fun capitalizeFirstLetter(text: String): String {
            if (text.isEmpty()) return text
            val firstLetterIndex = text.indexOfFirst { it.isLetter() }
            if (firstLetterIndex == -1) return text
            val char = text[firstLetterIndex]
            if (char.isLowerCase()) {
                return text.substring(0, firstLetterIndex) +
                       char.titlecase(Locale.US) +
                       text.substring(firstLetterIndex + 1)
            }
            return text
        }

        private val EXPLICIT_TRANSITION_TRIGGERS = listOf(
            "CUT TO:",
            "DISSOLVE TO:",
            "SMASH CUT TO:",
            "MATCH CUT TO:",
            "JUMP CUT TO:",
            "FADE OUT.",
            "FADE TO BLACK.",
            "FADE IN:",
            "BACK TO:",
            "FLASH CUT TO:",
            "TIME CUT:",
            "INTERCUT:",
            "SLOW FADE TO:"
        )

        fun isTransitionTrigger(text: String): Boolean {
            val trimmed = text.trim().uppercase(Locale.US)
            if (trimmed.isEmpty()) return false
            return EXPLICIT_TRANSITION_TRIGGERS.any { trimmed == it || trimmed.startsWith(it) }
        }

        fun formatTransitionText(text: String): String {
            val upper = text.trim().uppercase(Locale.US)
            return when {
                upper == "CUT TO" -> "CUT TO:"
                upper == "DISSOLVE TO" -> "DISSOLVE TO:"
                upper == "SMASH CUT TO" -> "SMASH CUT TO:"
                upper == "MATCH CUT TO" -> "MATCH CUT TO:"
                upper == "FADE OUT" -> "FADE OUT."
                upper == "FADE TO BLACK" -> "FADE TO BLACK."
                upper == "FADE IN" -> "FADE IN:"
                upper == "BACK TO" -> "BACK TO:"
                else -> upper
            }
        }

        fun getNextPanelText(blocks: List<ScreenplayBlock>, currentIndex: Int): String {
            val panelRegex = Regex("PANEL\\s+(\\d+)", RegexOption.IGNORE_CASE)
            for (i in currentIndex downTo 0) {
                val b = blocks[i]
                if (b.type == BlockType.COMIC_PANEL) {
                    val match = panelRegex.find(b.text)
                    if (match != null) {
                        val num = match.groupValues[1].toIntOrNull() ?: 1
                        return "PANEL ${num + 1}"
                    }
                }
            }
            return "PANEL 1"
        }

        data class SceneHeadingParts(
            val prefix: String,
            val location: String,
            val timeOfDay: String?,
            val hasHyphen: Boolean
        )

        val STANDARD_PREFIXES = listOf("INT.", "EXT.", "I/E.", "INT/EXT.", "EST.", "OVER BLACK")

        val STANDARD_TIME_TAGS = listOf(
            "DAY", "NIGHT", "CONTINUOUS", "LATER", "SAME", "MOMENTS LATER", "DAWN", "DUSK"
        )

        val DEFAULT_LOCATIONS = listOf(
            "LIVING ROOM", "KITCHEN", "BEDROOM", "HALLWAY", "OFFICE", "STREET", "CAR", "RESTAURANT"
        )

        fun parseSceneHeading(text: String): SceneHeadingParts {
            val upper = text.trim().uppercase(Locale.US)
            if (upper.isEmpty()) {
                return SceneHeadingParts(prefix = "", location = "", timeOfDay = null, hasHyphen = false)
            }

            val matchedPrefix = SCENE_HEADING_PREFIXES
                .filter { it.endsWith(".") || it.endsWith(" ") || it == "OVER BLACK" || it.startsWith("FADE IN") }
                .firstOrNull { upper.startsWith(it.uppercase(Locale.US)) }
                ?: ""

            val remainder = if (matchedPrefix.isNotEmpty()) {
                upper.removePrefix(matchedPrefix.uppercase(Locale.US)).trim()
            } else {
                upper
            }

            val lastHyphenIndex = remainder.lastIndexOf('-')
            return if (lastHyphenIndex != -1) {
                val loc = remainder.substring(0, lastHyphenIndex).trim()
                val time = remainder.substring(lastHyphenIndex + 1).trim()
                SceneHeadingParts(
                    prefix = matchedPrefix.trim(),
                    location = loc,
                    timeOfDay = time,
                    hasHyphen = true
                )
            } else {
                SceneHeadingParts(
                    prefix = matchedPrefix.trim(),
                    location = remainder,
                    timeOfDay = null,
                    hasHyphen = false
                )
            }
        }

        fun extractCharacters(blocks: List<ScreenplayBlock>): List<String> {
            val counts = mutableMapOf<String, Int>()
            blocks.forEach { block ->
                if (block.type == BlockType.CHARACTER) {
                    val clean = cleanCharacterName(block.text).uppercase(Locale.US).trim()
                    if (clean.isNotEmpty()) {
                        counts[clean] = (counts[clean] ?: 0) + 1
                    }
                }
            }
            return counts.entries.sortedByDescending { it.value }.map { it.key }
        }

        fun extractLocations(blocks: List<ScreenplayBlock>): List<String> {
            val counts = mutableMapOf<String, Int>()
            blocks.forEach { block ->
                if (isCountableSceneHeading(block)) {
                    val parts = parseSceneHeading(block.text)
                    val loc = parts.location.trim().uppercase(Locale.US)
                    if (loc.isNotEmpty() && loc != "BLACK") {
                        counts[loc] = (counts[loc] ?: 0) + 1
                    }
                }
            }
            return counts.entries.sortedByDescending { it.value }.map { it.key }
        }
        fun computeSearchMatches(
            query: String,
            blocks: List<ScreenplayBlock>,
            matchCase: Boolean,
            matchWholeWord: Boolean
        ): List<SearchMatch> {
            if (query.isEmpty()) return emptyList()
            val matches = mutableListOf<SearchMatch>()
            val regexOptions = if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE)
            val escaped = Regex.escape(query)
            val regex = try {
                if (matchWholeWord) Regex("\\b$escaped\\b", regexOptions) else Regex(escaped, regexOptions)
            } catch (_: Exception) {
                return emptyList()
            }
            for (block in blocks) {
                for (match in regex.findAll(block.text)) {
                    matches.add(SearchMatch(block.id, match.range.first, match.range.last + 1))
                }
            }
            return matches
        }

        fun partitionIntoScenes(blocks: List<ScreenplayBlock>): Pair<List<ScreenplayBlock>, List<List<ScreenplayBlock>>> {
            val leadBlocks = mutableListOf<ScreenplayBlock>()
            val scenes = mutableListOf<MutableList<ScreenplayBlock>>()
            var currentScene: MutableList<ScreenplayBlock>? = null

            for (block in blocks) {
                if (isCountableSceneHeading(block)) {
                    val newScene = mutableListOf<ScreenplayBlock>()
                    newScene.add(block)
                    scenes.add(newScene)
                    currentScene = newScene
                } else if (currentScene != null) {
                    currentScene.add(block)
                } else {
                    leadBlocks.add(block)
                }
            }
            return Pair(leadBlocks, scenes)
        }
    }

    private fun buildUpdatedState(
        current: ScreenplayUiState,
        updatedBlocks: List<ScreenplayBlock>,
        focusedBlockId: String = current.focusedBlockId
    ): ScreenplayUiState {
        // Self-healing: validate dual dialogue groups
        val dualGroups = updatedBlocks.groupBy { it.dualDialogueId }.filterKeys { it != null }
        val invalidDualIds = dualGroups.filter { (_, groupBlocks) ->
            val hasLeft = groupBlocks.any { it.dualSide == DualDialogueSide.LEFT }
            val hasRight = groupBlocks.any { it.dualSide == DualDialogueSide.RIGHT }
            !hasLeft || !hasRight
        }.keys

        val sanitizedBlocks = if (invalidDualIds.isNotEmpty()) {
            updatedBlocks.map { b ->
                if (b.dualDialogueId in invalidDualIds) b.copy(dualDialogueId = null, dualSide = null) else b
            }
        } else {
            updatedBlocks
        }

        val totalWords = sanitizedBlocks.sumOf { block ->
            block.text.split(Regex("\\s+")).count { it.any { c -> c.isLetterOrDigit() } }
        }
        val countableScenes = sanitizedBlocks.filter { isCountableSceneHeading(it) }
        val scenesCount = countableScenes.size

        val sceneNumberMap = mutableMapOf<String, String>()
        if (current.isSceneNumberingLocked) {
            var lastLockedBase: String? = null
            var subIndex = 0
            var preIndex = 0
            for (block in sanitizedBlocks) {
                if (block.type == BlockType.SCENE_HEADING && isCountableSceneHeading(block)) {
                    val existing = current.lockedSceneNumbers[block.id]
                    if (existing != null) {
                        sceneNumberMap[block.id] = existing
                        lastLockedBase = existing
                        subIndex = 0
                    } else {
                        val sNum = if (lastLockedBase == null) {
                            var n = preIndex++
                            val sb = StringBuilder()
                            while (n >= 0) {
                                sb.insert(0, ('A'.code + (n % 26)).toChar())
                                n = (n / 26) - 1
                            }
                            "${sb}1"
                        } else {
                            var n = subIndex++
                            val sb = StringBuilder()
                            while (n >= 0) {
                                sb.insert(0, ('A'.code + (n % 26)).toChar())
                                n = (n / 26) - 1
                            }
                            "$lastLockedBase$sb"
                        }
                        sceneNumberMap[block.id] = sNum
                    }
                }
            }
        } else {
            var sceneIdx = 1
            for (block in sanitizedBlocks) {
                if (block.type == BlockType.SCENE_HEADING && isCountableSceneHeading(block)) {
                    val sNum = block.sceneNumber ?: sceneIdx.toString()
                    sceneIdx++
                    sceneNumberMap[block.id] = sNum
                }
            }
        }

        val blocksWithSceneNumbers = sanitizedBlocks.map { b ->
            if (b.type == BlockType.SCENE_HEADING && isCountableSceneHeading(b)) {
                if (current.isSceneNumberingLocked) {
                    val sNum = sceneNumberMap[b.id]
                    if (sNum != null && b.sceneNumber != sNum) b.copy(sceneNumber = sNum) else b
                } else {
                    b
                }
            } else b
        }

        val sceneItems = countableScenes.mapIndexed { idx, block ->
            val sNum = sceneNumberMap[block.id] ?: (idx + 1).toString()
            SceneItem(
                index = idx + 1,
                heading = if (block.isOmitted) "OMITTED" else block.text.ifBlank { "UNTITLED SCENE" },
                lineNumber = idx,
                sceneNumber = sNum,
                blockId = block.id,
                isOmitted = block.isOmitted
            )
        }
        val extractedCharacters = extractCharacters(blocksWithSceneNumbers)
        val extractedLocations = extractLocations(blocksWithSceneNumbers)
        val pagination = ScreenplayPagination.calculatePagination(blocksWithSceneNumbers)

        val searchMatches = if (current.searchQuery.isNotEmpty()) {
            computeSearchMatches(current.searchQuery, blocksWithSceneNumbers, current.matchCase, current.matchWholeWord)
        } else emptyList()
        val matchIndex = if (searchMatches.isEmpty()) -1 else current.currentMatchIndex.coerceIn(0, searchMatches.size - 1)
        val notesCount = blocksWithSceneNumbers.count { !it.note.isNullOrBlank() }
        val revisedCount = blocksWithSceneNumbers.count { it.isRevised }

        val (_, partitionedScenes) = partitionIntoScenes(blocksWithSceneNumbers)
        val corkboard = current.corkboardDoc
        val numActs = maxOf(1, corkboard.actCount)
        val totalScenesCount = partitionedScenes.size
        val generatedCards = if (corkboard.cards.isNotEmpty()) {
            corkboard.cards.map { it.toSceneCard() }
        } else {
            partitionedScenes.mapIndexed { idx, sceneBlocks ->
                val headingBlock = sceneBlocks.firstOrNull { isCountableSceneHeading(it) } ?: sceneBlocks.first()
                val savedMeta = corkboard.cardMetadata[headingBlock.id]
                val defaultAct = if (totalScenesCount > 0) {
                    (idx * numActs / totalScenesCount).coerceIn(0, numActs - 1)
                } else 0
                val actIdx = savedMeta?.actIndex?.coerceIn(0, numActs - 1) ?: defaultAct
                val summary = savedMeta?.summary ?: ""
                val color = savedMeta?.colorTag ?: "#F59E0B"
                val charsInScene = sceneBlocks.filter { it.type == BlockType.CHARACTER }
                    .map { cleanCharacterName(it.text).uppercase(Locale.US).trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                val wordCount = sceneBlocks.sumOf { b -> b.text.split(Regex("\\s+")).count { it.isNotBlank() } }
                val estMin = maxOf(0.5f, String.format(Locale.US, "%.1f", wordCount / 140f).toFloat())

                SceneCard(
                    sceneHeadingBlockId = headingBlock.id,
                    sceneNumber = idx + 1,
                    headingText = if (headingBlock.isOmitted) "OMITTED" else headingBlock.text.ifBlank { "UNTITLED SCENE" },
                    summary = summary,
                    actIndex = actIdx,
                    colorTag = color,
                    charactersInScene = charsInScene,
                    blockCount = sceneBlocks.size,
                    estimatedMinutes = estMin,
                    isAiSummarizing = current.sceneCards.find { it.sceneHeadingBlockId == headingBlock.id }?.isAiSummarizing ?: false
                )
            }
        }

        return current.copy(
            blocks = blocksWithSceneNumbers,
            focusedBlockId = focusedBlockId,
            notesCount = notesCount,
            revisedBlocksCount = revisedCount,
            wordCount = totalWords,
            sceneCount = scenesCount,
            scenes = sceneItems,
            characters = extractedCharacters,
            locations = extractedLocations,
            pageCount = pagination.totalPages,
            estimatedMinutes = pagination.estimatedMinutes,
            totalLines = pagination.totalLines,
            pageBreakBeforeBlockIds = pagination.pageBreakBeforeBlockIds,
            blockPageNumbers = pagination.blockPageNumbers,
            blockSceneNumbers = sceneNumberMap,
            searchMatches = searchMatches,
            currentMatchIndex = matchIndex,
            sceneCards = generatedCards
        )
    }

    fun applyCharacterSuggestion(blockId: String, characterName: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val cleanName = cleanCharacterName(characterName).uppercase(Locale.US).trim()
            var finalText = cleanName
            if (shouldAppendContd(current.blocks, index, cleanName)) {
                finalText = "$cleanName (CONT'D)"
            }

            val updatedBlocks = current.blocks.toMutableList()
            updatedBlocks[index] = current.blocks[index].copy(
                type = BlockType.CHARACTER,
                text = finalText
            )
            buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
        }
    }

    fun applyScenePrefix(blockId: String, prefix: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val updatedBlocks = current.blocks.toMutableList()
            val text = if (prefix == "OVER BLACK") "OVER BLACK" else "$prefix "
            updatedBlocks[index] = current.blocks[index].copy(
                type = BlockType.SCENE_HEADING,
                text = text
            )
            buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
        }
    }

    fun applySceneLocation(blockId: String, location: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val currentBlock = current.blocks[index]
            val parts = parseSceneHeading(currentBlock.text)
            val prefix = if (parts.prefix.isNotEmpty()) parts.prefix else "INT."
            val newText = "$prefix $location - "

            val updatedBlocks = current.blocks.toMutableList()
            updatedBlocks[index] = currentBlock.copy(
                type = BlockType.SCENE_HEADING,
                text = newText
            )
            buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
        }
    }

    fun applySceneTime(blockId: String, timeTag: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val currentBlock = current.blocks[index]
            val text = currentBlock.text.trim()
            val hyphenIdx = text.lastIndexOf('-')
            val prefixAndLoc = if (hyphenIdx != -1) text.substring(0, hyphenIdx).trim() else text
            val newText = "$prefixAndLoc - $timeTag"

            val updatedBlocks = current.blocks.toMutableList()
            updatedBlocks[index] = currentBlock.copy(
                type = BlockType.SCENE_HEADING,
                text = newText
            )
            buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
        }
    }

    fun setTransitionPreset(blockId: String, preset: String) {
        _uiState.update { current ->
            val updatedBlocks = current.blocks.map { block ->
                if (block.id == blockId) {
                    block.copy(type = BlockType.TRANSITION, text = preset)
                } else {
                    block
                }
            }
            buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
        }
    }

    fun toggleCharacterExtension(blockId: String, extension: String) {
        _uiState.update { current ->
            val updatedBlocks = current.blocks.map { block ->
                if (block.id == blockId && block.type == BlockType.CHARACTER) {
                    val currentText = block.text.trim()
                    val cleanName = cleanCharacterName(currentText)
                    if (cleanName.isEmpty()) return@map block

                    val newText = when (extension) {
                        "(CONT'D)" -> {
                            if (currentText.contains("(CONT'D)")) {
                                currentText.replace("(CONT'D)", "").replace(Regex("\\s+"), " ").trim()
                            } else {
                                "$currentText (CONT'D)".trim()
                            }
                        }
                        else -> {
                            val hasThisExt = currentText.contains(extension)
                            val hasContd = currentText.contains("(CONT'D)")
                            val baseWithoutExts = currentText
                                .replace("(V.O.)", "")
                                .replace("(O.S.)", "")
                                .replace("(O.C.)", "")
                                .replace("(CONT'D)", "")
                                .replace(Regex("\\s+"), " ")
                                .trim()
                            if (hasThisExt) {
                                if (hasContd) "$baseWithoutExts (CONT'D)".trim() else baseWithoutExts
                            } else {
                                if (hasContd) "$baseWithoutExts $extension (CONT'D)".trim() else "$baseWithoutExts $extension".trim()
                            }
                        }
                    }
                    block.copy(text = newText)
                } else {
                    block
                }
            }
            buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
        }
    }

    fun updateBlockText(id: String, newText: String) {
        _uiState.update { current ->
            val updatedBlocks = current.blocks.map { block ->
                if (block.id == id) {
                    var targetType = block.type
                    val isNewPromotion = targetType != BlockType.SCENE_HEADING && isSceneHeadingPrefix(newText)
                    val isNewParenPromotion = targetType != BlockType.SCENE_HEADING &&
                        targetType != BlockType.CHARACTER &&
                        targetType != BlockType.PARENTHETICAL &&
                        newText.trimStart().startsWith("(")
                    val isNewTransitionPromotion = targetType == BlockType.ACTION &&
                        isTransitionTrigger(newText)

                    if (isNewPromotion) {
                        targetType = BlockType.SCENE_HEADING
                    } else if (isNewParenPromotion) {
                        targetType = BlockType.PARENTHETICAL
                    } else if (isNewTransitionPromotion) {
                        targetType = BlockType.TRANSITION
                    }

                    val isTypingForward = newText.length > block.text.length
                    val formatted = when (targetType) {
                        BlockType.SCENE_HEADING -> {
                            val upper = newText.uppercase(Locale.US)
                            if (isTypingForward && isSceneHeadingPrefix(newText)) {
                                formatSlugline(upper)
                            } else {
                                upper
                            }
                        }
                        BlockType.CHARACTER -> {
                            val upper = newText.uppercase(Locale.US)
                            formatCharacterText(upper)
                        }
                        BlockType.STAGE_ACT_SCENE, BlockType.STAGE_CURTAIN, BlockType.COMIC_PAGE, BlockType.COMIC_PANEL, BlockType.COMIC_SFX -> newText.uppercase(Locale.US)
                        BlockType.TRANSITION -> newText.uppercase(Locale.US)
                        BlockType.ACTION, BlockType.DIALOGUE, BlockType.STAGE_DIRECTION, BlockType.COMIC_ART_NOTE, BlockType.COMIC_BALLOON, BlockType.COMIC_CAPTION -> capitalizeFirstLetter(newText)
                        else -> newText
                    }
                    val wasModified = block.text != formatted || block.type != targetType
                    val isNowRevised = if (current.isRevisionTrackingEnabled && wasModified) true else block.isRevised
                    val revColor = if (current.isRevisionTrackingEnabled && wasModified) current.currentRevisionColor.name else block.revisionColor
                    block.copy(type = targetType, text = formatted, isRevised = isNowRevised, revisionColor = revColor)
                } else {
                    block
                }
            }

            buildUpdatedState(current, updatedBlocks)
        }
    }

    fun changeBlockType(id: String, newType: BlockType) {
        _uiState.update { current ->
            val updatedBlocks = current.blocks.map { block ->
                if (block.id == id) {
                    val formatted = when (newType) {
                        BlockType.SCENE_HEADING -> {
                            val upper = block.text.uppercase(Locale.US)
                            if (isSceneHeadingPrefix(block.text)) {
                                formatSlugline(upper)
                            } else {
                                upper
                            }
                        }
                        BlockType.CHARACTER -> {
                            val upper = block.text.uppercase(Locale.US)
                            formatCharacterText(upper)
                        }
                        BlockType.STAGE_ACT_SCENE, BlockType.STAGE_CURTAIN, BlockType.COMIC_PAGE, BlockType.COMIC_PANEL, BlockType.COMIC_SFX -> block.text.uppercase(Locale.US)
                        BlockType.PARENTHETICAL -> {
                            val trimmed = block.text.trim()
                            if (trimmed.isNotEmpty() && !trimmed.startsWith("(") && !trimmed.endsWith(")")) {
                                "($trimmed)"
                            } else {
                                block.text
                            }
                        }
                        BlockType.TRANSITION -> {
                            val upper = block.text.uppercase(Locale.US)
                            formatTransitionText(upper)
                        }
                        BlockType.ACTION, BlockType.DIALOGUE, BlockType.STAGE_DIRECTION, BlockType.COMIC_ART_NOTE, BlockType.COMIC_BALLOON, BlockType.COMIC_CAPTION -> capitalizeFirstLetter(block.text)
                        else -> block.text
                    }
                    block.copy(type = newType, text = formatted)
                } else {
                    block
                }
            }
            buildUpdatedState(current, updatedBlocks)
        }
    }

    fun insertNewBlock(afterId: String? = null, type: BlockType = BlockType.SCENE_HEADING) {
        _uiState.update { current ->
            val targetId = afterId ?: current.focusedBlockId
            val index = current.blocks.indexOfFirst { it.id == targetId }
            val insertIndex = if (index != -1) index + 1 else current.blocks.size

            val newBlock = ScreenplayBlock(
                type = type,
                text = "",
                isRevised = current.isRevisionTrackingEnabled,
                revisionColor = if (current.isRevisionTrackingEnabled) current.currentRevisionColor.name else null
            )
            val mutableBlocks = current.blocks.toMutableList()
            mutableBlocks.add(insertIndex, newBlock)

            buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
        }
    }

    fun setProjectFormatForTest(format: ProjectFormat) {
        _uiState.update { it.copy(projectFormat = format) }
    }

    fun onEnterPressed(blockId: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val currentBlock = current.blocks[index]
            val isCurrentEmpty = currentBlock.text.trim().isEmpty()
            var formattedCurrentText = currentBlock.text

            if (currentBlock.type == BlockType.ACTION || currentBlock.type == BlockType.DIALOGUE || currentBlock.type == BlockType.STAGE_DIRECTION || currentBlock.type == BlockType.COMIC_ART_NOTE || currentBlock.type == BlockType.COMIC_BALLOON || currentBlock.type == BlockType.COMIC_CAPTION) {
                formattedCurrentText = capitalizeFirstLetter(currentBlock.text)
            } else if (currentBlock.type == BlockType.CHARACTER) {
                val upper = currentBlock.text.uppercase(Locale.US)
                var formattedChar = formatCharacterText(upper)
                if (!formattedChar.contains("(CONT'D)") && shouldAppendContd(current.blocks, index, formattedChar)) {
                    formattedChar = "$formattedChar (CONT'D)"
                }
                formattedCurrentText = formattedChar
            } else if (currentBlock.type == BlockType.PARENTHETICAL) {
                val trimmed = currentBlock.text.trim()
                if (trimmed.isNotEmpty()) {
                    val withOpen = if (trimmed.startsWith("(")) trimmed else "($trimmed"
                    formattedCurrentText = if (withOpen.endsWith(")")) withOpen else "$withOpen)"
                }
            } else if (currentBlock.type == BlockType.TRANSITION || currentBlock.type == BlockType.STAGE_CURTAIN || currentBlock.type == BlockType.COMIC_SFX) {
                val upper = currentBlock.text.uppercase(Locale.US)
                formattedCurrentText = formatTransitionText(upper)
            } else if (currentBlock.type == BlockType.STAGE_ACT_SCENE || currentBlock.type == BlockType.COMIC_PAGE || currentBlock.type == BlockType.COMIC_PANEL) {
                formattedCurrentText = currentBlock.text.uppercase(Locale.US)
            }

            // ----------------------------------------------------
            // 🎭 THEATRICAL STAGE PLAY FORMAT STATE MACHINE
            // ----------------------------------------------------
            if (current.projectFormat == ProjectFormat.STAGE_PLAY) {
                if (currentBlock.type == BlockType.DIALOGUE && isCurrentEmpty) {
                    // Empty dialogue line promotes to Stage Direction
                    val mutableBlocks = current.blocks.toMutableList()
                    mutableBlocks[index] = currentBlock.copy(type = BlockType.STAGE_DIRECTION, text = "")
                    return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = currentBlock.id)
                }

                val nextType = when (currentBlock.type) {
                    BlockType.STAGE_ACT_SCENE -> BlockType.STAGE_DIRECTION
                    BlockType.STAGE_DIRECTION -> BlockType.STAGE_DIRECTION
                    BlockType.CHARACTER -> BlockType.DIALOGUE
                    BlockType.PARENTHETICAL -> BlockType.DIALOGUE
                    BlockType.DIALOGUE -> BlockType.CHARACTER // Rapid alternating dialogue exchanges
                    BlockType.STAGE_CURTAIN -> BlockType.STAGE_ACT_SCENE
                    BlockType.SCENE_HEADING -> BlockType.STAGE_DIRECTION
                    BlockType.ACTION -> BlockType.STAGE_DIRECTION
                    BlockType.TRANSITION -> BlockType.STAGE_ACT_SCENE
                    else -> BlockType.STAGE_DIRECTION
                }

                val newBlock = ScreenplayBlock(
                    type = nextType,
                    text = "",
                    isRevised = current.isRevisionTrackingEnabled,
                    revisionColor = if (current.isRevisionTrackingEnabled) current.currentRevisionColor.name else null
                )
                val mutableBlocks = current.blocks.toMutableList()
                mutableBlocks[index] = currentBlock.copy(text = formattedCurrentText)
                mutableBlocks.add(index + 1, newBlock)
                return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
            }

            // ----------------------------------------------------
            // 💥 COMIC BOOK SCRIPT FORMAT STATE MACHINE
            // ----------------------------------------------------
            if (current.projectFormat == ProjectFormat.COMIC_BOOK) {
                if (currentBlock.type == BlockType.COMIC_ART_NOTE && isCurrentEmpty) {
                    // Empty art note promotes to next Panel
                    val nextPanelText = getNextPanelText(current.blocks, index - 1)
                    val mutableBlocks = current.blocks.toMutableList()
                    mutableBlocks[index] = currentBlock.copy(type = BlockType.COMIC_PANEL, text = nextPanelText)
                    return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = currentBlock.id)
                }
                if ((currentBlock.type == BlockType.COMIC_BALLOON || currentBlock.type == BlockType.DIALOGUE) && isCurrentEmpty) {
                    // Empty speech balloon advances to next Panel
                    val nextPanelText = getNextPanelText(current.blocks, index - 1)
                    val mutableBlocks = current.blocks.toMutableList()
                    mutableBlocks[index] = currentBlock.copy(type = BlockType.COMIC_PANEL, text = nextPanelText)
                    return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = currentBlock.id)
                }

                val (nextType, initialText) = when (currentBlock.type) {
                    BlockType.COMIC_PAGE -> BlockType.COMIC_PANEL to "PANEL 1"
                    BlockType.COMIC_PANEL -> BlockType.COMIC_ART_NOTE to ""
                    BlockType.COMIC_ART_NOTE -> BlockType.COMIC_ART_NOTE to ""
                    BlockType.CHARACTER -> BlockType.COMIC_BALLOON to ""
                    BlockType.COMIC_BALLOON, BlockType.DIALOGUE -> BlockType.CHARACTER to ""
                    BlockType.COMIC_CAPTION -> BlockType.COMIC_BALLOON to ""
                    BlockType.COMIC_SFX -> BlockType.COMIC_PANEL to getNextPanelText(current.blocks, index)
                    BlockType.SCENE_HEADING -> BlockType.COMIC_PANEL to "PANEL 1"
                    BlockType.ACTION -> BlockType.COMIC_ART_NOTE to ""
                    else -> BlockType.COMIC_ART_NOTE to ""
                }

                val newBlock = ScreenplayBlock(
                    type = nextType,
                    text = initialText,
                    isRevised = current.isRevisionTrackingEnabled,
                    revisionColor = if (current.isRevisionTrackingEnabled) current.currentRevisionColor.name else null
                )
                val mutableBlocks = current.blocks.toMutableList()
                mutableBlocks[index] = currentBlock.copy(text = formattedCurrentText)
                mutableBlocks.add(index + 1, newBlock)
                return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
            }

            // ----------------------------------------------------
            // 🎬 STANDARD SCREENPLAY / TV SERIES FORMAT STATE MACHINE
            // ----------------------------------------------------
            val nextType = when (currentBlock.type) {
                BlockType.SCENE_HEADING -> {
                    if (isFadeIn(currentBlock.text)) BlockType.SCENE_HEADING else BlockType.ACTION
                }
                BlockType.ACTION -> BlockType.ACTION
                BlockType.CHARACTER -> BlockType.DIALOGUE
                BlockType.PARENTHETICAL -> BlockType.DIALOGUE
                BlockType.DIALOGUE -> BlockType.ACTION
                BlockType.TRANSITION -> BlockType.SCENE_HEADING
                else -> BlockType.ACTION
            }

            // In dual dialogue, left dialogue jump focuses right character if present
            if (currentBlock.dualDialogueId != null && currentBlock.type == BlockType.DIALOGUE && currentBlock.dualSide == DualDialogueSide.LEFT) {
                val nextBlock = current.blocks.getOrNull(index + 1)
                if (nextBlock != null && nextBlock.dualDialogueId == currentBlock.dualDialogueId && nextBlock.dualSide == DualDialogueSide.RIGHT) {
                    val mutableBlocks = current.blocks.toMutableList()
                    mutableBlocks[index] = currentBlock.copy(text = formattedCurrentText)
                    return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = nextBlock.id)
                }
            }

            val dualId = if (currentBlock.dualDialogueId != null && nextType == BlockType.DIALOGUE) currentBlock.dualDialogueId else null
            val dualSide = if (dualId != null) currentBlock.dualSide else null

            val newBlock = ScreenplayBlock(
                type = nextType,
                text = "",
                dualDialogueId = dualId,
                dualSide = dualSide,
                isRevised = current.isRevisionTrackingEnabled,
                revisionColor = if (current.isRevisionTrackingEnabled) current.currentRevisionColor.name else null
            )
            val mutableBlocks = current.blocks.toMutableList()
            mutableBlocks[index] = currentBlock.copy(text = formattedCurrentText)
            mutableBlocks.add(index + 1, newBlock)

            buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
        }
    }

    fun onTabPressed(blockId: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index == -1) return@update current

            val currentBlock = current.blocks[index]

            // ----------------------------------------------------
            // 🎭 THEATRICAL STAGE PLAY TAB HANDLING
            // ----------------------------------------------------
            if (current.projectFormat == ProjectFormat.STAGE_PLAY) {
                when (currentBlock.type) {
                    BlockType.STAGE_DIRECTION, BlockType.STAGE_ACT_SCENE, BlockType.ACTION, BlockType.SCENE_HEADING -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) {
                                block.copy(type = BlockType.CHARACTER, text = block.text.uppercase(Locale.US))
                            } else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    BlockType.CHARACTER -> {
                        val newBlock = ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "")
                        val mutableBlocks = current.blocks.toMutableList()
                        mutableBlocks.add(index + 1, newBlock)
                        return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
                    }
                    BlockType.DIALOGUE -> {
                        val newBlock = ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "")
                        val mutableBlocks = current.blocks.toMutableList()
                        mutableBlocks.add(index + 1, newBlock)
                        return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
                    }
                    BlockType.PARENTHETICAL -> {
                        val newBlock = ScreenplayBlock(type = BlockType.DIALOGUE, text = "")
                        val mutableBlocks = current.blocks.toMutableList()
                        mutableBlocks.add(index + 1, newBlock)
                        return@update buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
                    }
                    BlockType.STAGE_CURTAIN -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.STAGE_ACT_SCENE) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    else -> {}
                }
            }

            // ----------------------------------------------------
            // 💥 COMIC BOOK SCRIPT TAB HANDLING (Page -> Panel -> Art -> Char -> Caption -> SFX)
            // ----------------------------------------------------
            if (current.projectFormat == ProjectFormat.COMIC_BOOK) {
                when (currentBlock.type) {
                    BlockType.COMIC_PAGE -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.COMIC_PANEL) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    BlockType.COMIC_PANEL -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.COMIC_PAGE) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    BlockType.COMIC_ART_NOTE, BlockType.ACTION -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.CHARACTER, text = block.text.uppercase(Locale.US)) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    BlockType.CHARACTER -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.COMIC_CAPTION) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    BlockType.COMIC_CAPTION -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.COMIC_SFX, text = block.text.uppercase(Locale.US)) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    BlockType.COMIC_SFX -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.COMIC_ART_NOTE) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    BlockType.COMIC_BALLOON, BlockType.DIALOGUE -> {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.COMIC_CAPTION) else block
                        }
                        return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    }
                    else -> {}
                }
            }

            // ----------------------------------------------------
            // 🎬 STANDARD SCREENPLAY / TV SERIES TAB HANDLING
            // ----------------------------------------------------
            when (currentBlock.type) {
                BlockType.ACTION -> {
                    // Tab on Action promotes to Character (uppercase)
                    val updatedBlocks = current.blocks.map { block ->
                        if (block.id == blockId) {
                            block.copy(
                                type = BlockType.CHARACTER,
                                text = block.text.uppercase(Locale.US)
                            )
                        } else {
                            block
                        }
                    }
                    buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                }
                BlockType.CHARACTER -> {
                    // Tab on Character inserts a Parenthetical block immediately below it
                    val newBlock = ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "")
                    val mutableBlocks = current.blocks.toMutableList()
                    mutableBlocks.add(index + 1, newBlock)
                    buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
                }
                BlockType.DIALOGUE -> {
                    if (currentBlock.text.isEmpty()) {
                        // Empty dialogue -> Parenthetical
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) {
                                block.copy(type = BlockType.PARENTHETICAL, text = "")
                            } else {
                                block
                            }
                        }
                        buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    } else {
                        // Tab after dialogue creates Parenthetical block
                        val newBlock = ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "")
                        val mutableBlocks = current.blocks.toMutableList()
                        mutableBlocks.add(index + 1, newBlock)
                        buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
                    }
                }
                BlockType.PARENTHETICAL -> {
                    if (currentBlock.text.isEmpty()) {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) {
                                block.copy(type = BlockType.DIALOGUE)
                            } else {
                                block
                            }
                        }
                        buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    } else {
                        val newBlock = ScreenplayBlock(type = BlockType.DIALOGUE, text = "")
                        val mutableBlocks = current.blocks.toMutableList()
                        mutableBlocks.add(index + 1, newBlock)
                        buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
                    }
                }
                BlockType.TRANSITION -> {
                    val updatedBlocks = current.blocks.map { block ->
                        if (block.id == blockId) {
                            block.copy(type = BlockType.SCENE_HEADING)
                        } else {
                            block
                        }
                    }
                    buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                }
                BlockType.SCENE_HEADING -> {
                    if (currentBlock.text.isEmpty()) {
                        val updatedBlocks = current.blocks.map { block ->
                            if (block.id == blockId) block.copy(type = BlockType.ACTION) else block
                        }
                        buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
                    } else {
                        val newBlock = ScreenplayBlock(type = BlockType.ACTION, text = "")
                        val mutableBlocks = current.blocks.toMutableList()
                        mutableBlocks.add(index + 1, newBlock)
                        buildUpdatedState(current, mutableBlocks, focusedBlockId = newBlock.id)
                    }
                }
                else -> buildUpdatedState(current, current.blocks, focusedBlockId = blockId)
            }
        }
    }

    fun onBackspaceOnEmptyBlock(blockId: String) {
        _uiState.update { current ->
            if (current.blocks.size <= 1) return@update current

            val index = current.blocks.indexOfFirst { it.id == blockId }
            if (index <= 0) return@update current

            val targetBlock = current.blocks[index]
            if (targetBlock.text.isNotEmpty()) return@update current

            // If empty CHARACTER or TRANSITION block, pressing backspace demotes it back to ACTION
            if (targetBlock.type == BlockType.CHARACTER || targetBlock.type == BlockType.TRANSITION) {
                val updatedBlocks = current.blocks.map { block ->
                    if (block.id == blockId) {
                        block.copy(type = BlockType.ACTION)
                    } else {
                        block
                    }
                }
                return@update buildUpdatedState(current, updatedBlocks, focusedBlockId = blockId)
            }

            val previousBlock = current.blocks[index - 1]
            val mutableBlocks = current.blocks.toMutableList()
            mutableBlocks.removeAt(index)

            buildUpdatedState(current, mutableBlocks, focusedBlockId = previousBlock.id)
        }
    }

    fun setFocusedBlock(blockId: String) {
        _uiState.update { it.copy(focusedBlockId = blockId, cursorTarget = CursorTarget(CursorTargetType.DEFAULT)) }
    }

    fun navigateUp(fromBlockId: String, currentColumn: Int) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == fromBlockId }
            if (index > 0) {
                val targetBlock = current.blocks[index - 1]
                current.copy(
                    focusedBlockId = targetBlock.id,
                    cursorTarget = CursorTarget(CursorTargetType.BOTTOM_LINE_COLUMN, currentColumn)
                )
            } else {
                current
            }
        }
    }

    fun navigateDown(fromBlockId: String, currentColumn: Int) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == fromBlockId }
            if (index >= 0 && index < current.blocks.size - 1) {
                val targetBlock = current.blocks[index + 1]
                current.copy(
                    focusedBlockId = targetBlock.id,
                    cursorTarget = CursorTarget(CursorTargetType.TOP_LINE_COLUMN, currentColumn)
                )
            } else {
                current
            }
        }
    }

    fun navigateLeft(fromBlockId: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == fromBlockId }
            if (index > 0) {
                val targetBlock = current.blocks[index - 1]
                current.copy(
                    focusedBlockId = targetBlock.id,
                    cursorTarget = CursorTarget(CursorTargetType.END)
                )
            } else {
                current
            }
        }
    }

    fun navigateRight(fromBlockId: String) {
        _uiState.update { current ->
            val index = current.blocks.indexOfFirst { it.id == fromBlockId }
            if (index >= 0 && index < current.blocks.size - 1) {
                val targetBlock = current.blocks[index + 1]
                current.copy(
                    focusedBlockId = targetBlock.id,
                    cursorTarget = CursorTarget(CursorTargetType.START)
                )
            } else {
                current
            }
        }
    }

    fun toggleZenMode() {
        _uiState.update { it.copy(isZenMode = !it.isZenMode) }
    }

    fun setViewMode(mode: ScreenplayViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun toggleViewMode() {
        _uiState.update { current ->
            val nextMode = if (current.viewMode == ScreenplayViewMode.PAGE_VIEW) {
                ScreenplayViewMode.INLINE_VIEW
            } else {
                ScreenplayViewMode.PAGE_VIEW
            }
            current.copy(viewMode = nextMode)
        }
    }

    fun zoomIn() {
        _uiState.update { current ->
            val nextZoom = (current.zoomLevel + 0.25f).coerceAtMost(2.0f)
            current.copy(zoomLevel = (Math.round(nextZoom * 100f) / 100f))
        }
    }

    fun zoomOut() {
        _uiState.update { current ->
            val nextZoom = (current.zoomLevel - 0.25f).coerceAtLeast(0.75f)
            current.copy(zoomLevel = (Math.round(nextZoom * 100f) / 100f))
        }
    }

    fun resetZoom() {
        _uiState.update { it.copy(zoomLevel = 1.0f) }
    }

    fun setZoom(zoom: Float) {
        _uiState.update { it.copy(zoomLevel = zoom.coerceIn(0.75f, 2.0f)) }
    }

    fun toggleSidebar() {
        _uiState.update { it.copy(isSidebarExpanded = !it.isSidebarExpanded) }
    }

    fun toggleWriterNotes() {
        _uiState.update { it.copy(showWriterNotes = !it.showWriterNotes) }
    }

    fun setBlockNote(blockId: String, note: String?) {
        _uiState.update { current ->
            val updated = current.blocks.map { b ->
                if (b.id == blockId) b.copy(note = note?.takeIf { it.isNotBlank() }) else b
            }
            buildUpdatedState(current, updated)
        }
    }

    fun deleteBlockNote(blockId: String) {
        setBlockNote(blockId, null)
    }

    fun toggleBlockHighlight(blockId: String, isShiftPressed: Boolean = false) {
        _uiState.update { current ->
            val anchor = current.selectionAnchorBlockId
            if (isShiftPressed && anchor != null && anchor != blockId) {
                val idx1 = current.blocks.indexOfFirst { it.id == anchor }
                val idx2 = current.blocks.indexOfFirst { it.id == blockId }
                if (idx1 != -1 && idx2 != -1) {
                    val start = minOf(idx1, idx2)
                    val end = maxOf(idx1, idx2)
                    val rangeIds = current.blocks.subList(start, end + 1).map { it.id }.toSet()
                    return@update current.copy(highlightedBlockIds = rangeIds)
                }
            }
            val currentSet = current.highlightedBlockIds
            val newSet = if (currentSet.contains(blockId)) {
                currentSet - blockId
            } else {
                currentSet + blockId
            }
            current.copy(
                highlightedBlockIds = newSet,
                selectionAnchorBlockId = if (newSet.isNotEmpty()) blockId else null
            )
        }
    }

    fun highlightBlockRange(fromBlockId: String, toBlockId: String) {
        _uiState.update { current ->
            val idx1 = current.blocks.indexOfFirst { it.id == fromBlockId }
            val idx2 = current.blocks.indexOfFirst { it.id == toBlockId }
            if (idx1 == -1 || idx2 == -1) return@update current
            val start = minOf(idx1, idx2)
            val end = maxOf(idx1, idx2)
            val rangeIds = current.blocks.subList(start, end + 1).map { it.id }.toSet()
            current.copy(
                highlightedBlockIds = rangeIds,
                selectionAnchorBlockId = fromBlockId
            )
        }
    }

    fun setHighlightedBlocks(blockIds: Set<String>) {
        _uiState.update { current ->
            current.copy(
                highlightedBlockIds = blockIds,
                selectionAnchorBlockId = blockIds.firstOrNull() ?: current.selectionAnchorBlockId
            )
        }
    }

    fun clearHighlight() {
        _uiState.update { it.copy(highlightedBlockIds = emptySet(), selectionAnchorBlockId = null) }
    }

    fun deleteHighlightedBlocks() {
        _uiState.update { current ->
            val toDelete = current.highlightedBlockIds
            if (toDelete.isEmpty()) return@update current
            val remaining = current.blocks.filter { !toDelete.contains(it.id) }
            val finalBlocks = if (remaining.isEmpty()) listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING)) else remaining
            val nextFocus = finalBlocks.first().id
            buildUpdatedState(current.copy(highlightedBlockIds = emptySet(), selectionAnchorBlockId = null), finalBlocks, focusedBlockId = nextFocus)
        }
    }

    fun expandHighlightDown(fromBlockId: String) {
        _uiState.update { current ->
            val anchor = current.selectionAnchorBlockId ?: fromBlockId
            val anchorIdx = current.blocks.indexOfFirst { it.id == anchor }
            val currentIdx = current.blocks.indexOfFirst { it.id == fromBlockId }
            if (anchorIdx == -1 || currentIdx == -1) return@update current
            val nextIdx = (currentIdx + 1).coerceAtMost(current.blocks.size - 1)
            val start = minOf(anchorIdx, nextIdx)
            val end = maxOf(anchorIdx, nextIdx)
            val rangeIds = current.blocks.subList(start, end + 1).map { it.id }.toSet()
            current.copy(
                highlightedBlockIds = rangeIds,
                selectionAnchorBlockId = anchor,
                focusedBlockId = current.blocks[nextIdx].id
            )
        }
    }

    fun expandHighlightUp(fromBlockId: String) {
        _uiState.update { current ->
            val anchor = current.selectionAnchorBlockId ?: fromBlockId
            val anchorIdx = current.blocks.indexOfFirst { it.id == anchor }
            val currentIdx = current.blocks.indexOfFirst { it.id == fromBlockId }
            if (anchorIdx == -1 || currentIdx == -1) return@update current
            val prevIdx = (currentIdx - 1).coerceAtLeast(0)
            val start = minOf(anchorIdx, prevIdx)
            val end = maxOf(anchorIdx, prevIdx)
            val rangeIds = current.blocks.subList(start, end + 1).map { it.id }.toSet()
            current.copy(
                highlightedBlockIds = rangeIds,
                selectionAnchorBlockId = anchor,
                focusedBlockId = current.blocks[prevIdx].id
            )
        }
    }

    fun getDualDialogueRange(
        allBlocks: List<ScreenplayBlock>,
        selectedIds: Set<String>
    ): List<ScreenplayBlock>? {
        val selected = allBlocks.filter { it.id in selectedIds }
        val charBlocks = selected.filter { it.type == BlockType.CHARACTER }
        if (charBlocks.size != 2) return null

        val firstChar = charBlocks[0]
        val secondChar = charBlocks[1]

        val firstIdx = allBlocks.indexOfFirst { it.id == firstChar.id }
        val secondIdx = allBlocks.indexOfFirst { it.id == secondChar.id }
        if (firstIdx == -1 || secondIdx == -1 || firstIdx >= secondIdx) return null

        // Check blocks between firstIdx and secondIdx
        val between = allBlocks.subList(firstIdx + 1, secondIdx)
        if (between.isEmpty()) return null
        if (between.none { it.type == BlockType.DIALOGUE }) return null
        if (between.any { it.type != BlockType.DIALOGUE && it.type != BlockType.PARENTHETICAL }) return null

        // Find the end of second character's dialogue
        var endIdx = secondIdx + 1
        while (endIdx < allBlocks.size) {
            val b = allBlocks[endIdx]
            if (b.type == BlockType.DIALOGUE || b.type == BlockType.PARENTHETICAL) {
                endIdx++
            } else {
                break
            }
        }
        val secondDialogueBlocks = allBlocks.subList(secondIdx + 1, endIdx)
        if (secondDialogueBlocks.none { it.type == BlockType.DIALOGUE }) return null

        return allBlocks.subList(firstIdx, endIdx)
    }

    fun isDualDialogueCandidate(selectedIds: Set<String> = _uiState.value.highlightedBlockIds): Boolean {
        val range = getDualDialogueRange(_uiState.value.blocks, selectedIds) ?: return false
        val rangeIds = range.map { it.id }.toSet()
        val charIds = range.filter { it.type == BlockType.CHARACTER }.map { it.id }.toSet()
        return charIds.all { it in selectedIds } && selectedIds.intersect(rangeIds).size >= 2
    }

    fun isAlreadyDualDialogue(selectedIds: Set<String> = _uiState.value.highlightedBlockIds): Boolean {
        if (selectedIds.isEmpty()) return false
        val selected = _uiState.value.blocks.filter { it.id in selectedIds }
        val dualIds = selected.mapNotNull { it.dualDialogueId }.toSet()
        return dualIds.size == 1 && selected.all { it.dualDialogueId != null }
    }

    fun makeDualDialogue(blockIds: Set<String> = _uiState.value.highlightedBlockIds) {
        _uiState.update { current ->
            val candidateRange = getDualDialogueRange(current.blocks, blockIds) ?: return@update current
            val dualId = UUID.randomUUID().toString()
            val secondCharIndex = candidateRange.indexOfLast { it.type == BlockType.CHARACTER }

            val leftIds = candidateRange.subList(0, secondCharIndex).map { it.id }.toSet()
            val rightIds = candidateRange.subList(secondCharIndex, candidateRange.size).map { it.id }.toSet()

            val updatedBlocks = current.blocks.map { block ->
                when {
                    block.id in leftIds -> block.copy(dualDialogueId = dualId, dualSide = DualDialogueSide.LEFT)
                    block.id in rightIds -> block.copy(dualDialogueId = dualId, dualSide = DualDialogueSide.RIGHT)
                    else -> block
                }
            }
            buildUpdatedState(current.copy(highlightedBlockIds = leftIds + rightIds), updatedBlocks)
        }
    }

    fun separateDualDialogue(blockIds: Set<String> = _uiState.value.highlightedBlockIds) {
        _uiState.update { current ->
            val selectedBlocks = current.blocks.filter { it.id in blockIds }
            val targetDualIds = selectedBlocks.mapNotNull { it.dualDialogueId }.toSet()
            if (targetDualIds.isEmpty()) return@update current

            val updatedBlocks = current.blocks.map { block ->
                if (block.dualDialogueId in targetDualIds) {
                    block.copy(dualDialogueId = null, dualSide = null)
                } else {
                    block
                }
            }
            buildUpdatedState(current, updatedBlocks)
        }
    }

    fun toggleReorderMode() {
        _uiState.update { it.copy(isReorderMode = !it.isReorderMode) }
    }

    fun setReorderMode(enabled: Boolean) {
        _uiState.update { it.copy(isReorderMode = enabled) }
    }

    fun moveScene(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            val (leadBlocks, scenes) = partitionIntoScenes(current.blocks)
            if (fromIndex !in scenes.indices || toIndex !in scenes.indices || fromIndex == toIndex) {
                return@update current
            }
            val mutableScenes = scenes.toMutableList()
            val movedScene = mutableScenes.removeAt(fromIndex)
            mutableScenes.add(toIndex, movedScene)

            val reorderedBlocks = mutableListOf<ScreenplayBlock>()
            reorderedBlocks.addAll(leadBlocks)
            for (sc in mutableScenes) {
                reorderedBlocks.addAll(sc)
            }

            val movedHeading = movedScene.firstOrNull { isCountableSceneHeading(it) } ?: movedScene.firstOrNull()
            val targetFocusId = movedHeading?.id ?: current.focusedBlockId

            buildUpdatedState(
                current.copy(selectedSceneIndex = toIndex + 1),
                reorderedBlocks,
                focusedBlockId = targetFocusId
            )
        }
    }

    fun moveSceneUp(sceneIndex: Int) {
        val from0 = sceneIndex - 1
        if (from0 > 0) {
            moveScene(from0, from0 - 1)
        }
    }

    fun moveSceneDown(sceneIndex: Int) {
        val from0 = sceneIndex - 1
        val numScenes = _uiState.value.scenes.size
        if (from0 in 0 until (numScenes - 1)) {
            moveScene(from0, from0 + 1)
        }
    }

    fun selectScene(scene: SceneItem) {
        _uiState.update { current ->
            val targetBlock = if (scene.blockId.isNotEmpty()) {
                current.blocks.find { it.id == scene.blockId }
            } else {
                val sceneHeadings = current.blocks.filter { isCountableSceneHeading(it) }
                sceneHeadings.getOrNull(scene.index - 1)
            }
            current.copy(
                selectedSceneIndex = scene.index,
                focusedBlockId = targetBlock?.id ?: current.focusedBlockId
            )
        }
    }


    fun updateTitle(newTitle: String) {
        ScreenplayDocumentStore.updateTitle(newTitle)
        _uiState.update { current ->
            current.copy(
                title = newTitle,
                storyBible = if (current.storyBible.scriptTitle.isNotBlank()) current.storyBible.copy(scriptTitle = newTitle) else current.storyBible,
                corkboardDoc = if (current.corkboardDoc.scriptTitle.isNotBlank()) current.corkboardDoc.copy(scriptTitle = newTitle) else current.corkboardDoc
            )
        }
    }

    fun updateAuthor(newAuthor: String) {
        ScreenplayDocumentStore.updateAuthor(newAuthor)
        _uiState.update { it.copy(author = newAuthor) }
    }

    fun syncTitlePage() {
        val data = ScreenplayDocumentStore.titlePage.value
        _uiState.update { current ->
            if (current.title != data.title || current.author != data.author) {
                current.copy(
                    title = data.title,
                    author = data.author,
                    storyBible = if (current.storyBible.scriptTitle.isNotBlank()) {
                        current.storyBible.copy(scriptTitle = data.title)
                    } else current.storyBible,
                    corkboardDoc = if (current.corkboardDoc.scriptTitle.isNotBlank()) {
                        current.corkboardDoc.copy(scriptTitle = data.title)
                    } else current.corkboardDoc
                )
            } else {
                current
            }
        }
    }

    fun loadDocument(blocks: List<ScreenplayBlock>, titlePageData: TitlePageData = TitlePageData()) {
        ScreenplayDocumentStore.setTitlePage(titlePageData)
        val initialFocus = blocks.firstOrNull()?.id ?: ""
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(
                    title = titlePageData.title,
                    author = titlePageData.author,
                    blocks = blocks,
                    focusedBlockId = initialFocus
                ),
                blocks,
                focusedBlockId = initialFocus
            )
        }
    }

    fun newDocument() {
        val blankTitlePage = TitlePageData(
            title = "UNTITLED SCREENPLAY",
            writtenByLabel = "written by",
            author = "",
            sourceMaterial = "",
            contactInfo = "",
            draftInfo = ""
        )
        ScreenplayDocumentStore.setTitlePage(blankTitlePage)
        val initialBlock = ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(
                    title = blankTitlePage.title,
                    author = blankTitlePage.author,
                    blocks = listOf(initialBlock),
                    focusedBlockId = initialBlock.id
                ),
                listOf(initialBlock),
                focusedBlockId = initialBlock.id
            )
        }
    }

    fun toggleSceneNumbers() {
        _uiState.update { it.copy(showSceneNumbers = !it.showSceneNumbers) }
    }

    fun setSceneNumbersVisible(visible: Boolean) {
        _uiState.update { it.copy(showSceneNumbers = visible) }
    }

    fun setSceneNumber(blockId: String, sceneNum: String?) {
        _uiState.update { current ->
            val updated = current.blocks.map {
                if (it.id == blockId) it.copy(sceneNumber = sceneNum?.trim()?.ifBlank { null }) else it
            }
            buildUpdatedState(current, updated)
        }
    }

    fun openFind(replaceMode: Boolean = false) {
        _uiState.update { it.copy(isFindReplaceVisible = true, findMode = FindMode.FIND_REPLACE) }
    }

    fun openCharacterRename(characterName: String? = null) {
        _uiState.update { current ->
            val defaultOld = characterName ?: current.characters.firstOrNull() ?: ""
            current.copy(
                isFindReplaceVisible = true,
                findMode = FindMode.RENAME_CHARACTER,
                renameOldName = defaultOld,
                renameNewName = ""
            )
        }
    }

    fun setFindMode(mode: FindMode) {
        _uiState.update { it.copy(findMode = mode) }
    }

    fun closeFind() {
        _uiState.update { it.copy(isFindReplaceVisible = false, searchQuery = "", searchMatches = emptyList(), currentMatchIndex = -1) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { current ->
            val matches = computeSearchMatches(query, current.blocks, current.matchCase, current.matchWholeWord)
            val newIdx = if (matches.isNotEmpty()) 0 else -1
            val focusedId = if (newIdx != -1) matches[0].blockId else current.focusedBlockId
            val cursor = if (newIdx != -1) CursorTarget(CursorTargetType.START, matches[0].startIndex) else current.cursorTarget
            current.copy(
                searchQuery = query,
                searchMatches = matches,
                currentMatchIndex = newIdx,
                focusedBlockId = focusedId,
                cursorTarget = cursor
            )
        }
    }

    fun updateReplaceQuery(replaceText: String) {
        _uiState.update { it.copy(replaceQuery = replaceText) }
    }

    fun toggleMatchCase() {
        _uiState.update { current ->
            val newCase = !current.matchCase
            val matches = computeSearchMatches(current.searchQuery, current.blocks, newCase, current.matchWholeWord)
            val newIdx = if (matches.isNotEmpty()) 0 else -1
            current.copy(matchCase = newCase, searchMatches = matches, currentMatchIndex = newIdx)
        }
    }

    fun toggleMatchWholeWord() {
        _uiState.update { current ->
            val newWord = !current.matchWholeWord
            val matches = computeSearchMatches(current.searchQuery, current.blocks, current.matchCase, newWord)
            val newIdx = if (matches.isNotEmpty()) 0 else -1
            current.copy(matchWholeWord = newWord, searchMatches = matches, currentMatchIndex = newIdx)
        }
    }

    fun findNext() {
        _uiState.update { current ->
            if (current.searchMatches.isEmpty()) return@update current
            val nextIdx = (current.currentMatchIndex + 1) % current.searchMatches.size
            val match = current.searchMatches[nextIdx]
            current.copy(
                currentMatchIndex = nextIdx,
                focusedBlockId = match.blockId,
                cursorTarget = CursorTarget(CursorTargetType.START, match.startIndex)
            )
        }
    }

    fun findPrevious() {
        _uiState.update { current ->
            if (current.searchMatches.isEmpty()) return@update current
            val prevIdx = if (current.currentMatchIndex <= 0) current.searchMatches.size - 1 else current.currentMatchIndex - 1
            val match = current.searchMatches[prevIdx]
            current.copy(
                currentMatchIndex = prevIdx,
                focusedBlockId = match.blockId,
                cursorTarget = CursorTarget(CursorTargetType.START, match.startIndex)
            )
        }
    }

    fun replaceCurrent() {
        _uiState.update { current ->
            if (current.searchMatches.isEmpty() || current.currentMatchIndex !in current.searchMatches.indices) return@update current
            val match = current.searchMatches[current.currentMatchIndex]
            val blockIdx = current.blocks.indexOfFirst { it.id == match.blockId }
            if (blockIdx == -1) return@update current
            val block = current.blocks[blockIdx]
            val text = block.text
            if (match.startIndex > text.length || match.endIndex > text.length) return@update current
            val newText = text.substring(0, match.startIndex) + current.replaceQuery + text.substring(match.endIndex)
            val mutable = current.blocks.toMutableList()
            mutable[blockIdx] = block.copy(text = newText)
            val nextState = buildUpdatedState(current, mutable, focusedBlockId = block.id)
            val newMatches = computeSearchMatches(current.searchQuery, nextState.blocks, current.matchCase, current.matchWholeWord)
            val newIdx = if (newMatches.isEmpty()) -1 else current.currentMatchIndex.coerceAtMost(newMatches.size - 1)
            nextState.copy(searchMatches = newMatches, currentMatchIndex = newIdx)
        }
    }

    fun replaceAll(): Int {
        var count = 0
        _uiState.update { current ->
            if (current.searchQuery.isEmpty()) return@update current
            val regexOptions = if (current.matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE)
            val escaped = Regex.escape(current.searchQuery)
            val regex = if (current.matchWholeWord) Regex("\\b$escaped\\b", regexOptions) else Regex(escaped, regexOptions)
            val updated = current.blocks.map { b ->
                val matchesInBlock = regex.findAll(b.text).count()
                count += matchesInBlock
                if (matchesInBlock > 0) b.copy(text = regex.replace(b.text, current.replaceQuery)) else b
            }
            val nextState = buildUpdatedState(current, updated)
            nextState.copy(searchMatches = emptyList(), currentMatchIndex = -1)
        }
        return count
    }

    fun setRenameOldName(name: String) {
        _uiState.update { it.copy(renameOldName = name) }
    }

    fun setRenameNewName(name: String) {
        _uiState.update { it.copy(renameNewName = name) }
    }

    fun setRenameCuesOnly(cuesOnly: Boolean) {
        _uiState.update { it.copy(renameCuesOnly = cuesOnly) }
    }

    fun renameCharacter(
        oldName: String = _uiState.value.renameOldName,
        newName: String = _uiState.value.renameNewName,
        cuesOnly: Boolean = _uiState.value.renameCuesOnly
    ): Int {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim().uppercase(Locale.US)
        if (trimmedOld.isBlank() || trimmedNew.isBlank() || trimmedOld.equals(trimmedNew, ignoreCase = true)) return 0

        var renamedCount = 0
        _uiState.update { current ->
            val updated = current.blocks.map { block ->
                when (block.type) {
                    BlockType.CHARACTER -> {
                        val clean = cleanCharacterName(block.text)
                        if (clean.equals(trimmedOld, ignoreCase = true)) {
                            renamedCount++
                            val extensions = if (block.text.contains("(")) {
                                block.text.substring(block.text.indexOf("(")).trim()
                            } else ""
                            val formatted = if (extensions.isNotEmpty()) "$trimmedNew $extensions" else trimmedNew
                            block.copy(text = formatted)
                        } else {
                            block
                        }
                    }
                    BlockType.ACTION, BlockType.DIALOGUE -> {
                        if (!cuesOnly) {
                            val regex = Regex("\\b${Regex.escape(trimmedOld)}\\b", RegexOption.IGNORE_CASE)
                            val matchCount = regex.findAll(block.text).count()
                            if (matchCount > 0) {
                                renamedCount += matchCount
                                block.copy(text = regex.replace(block.text, trimmedNew))
                            } else {
                                block
                            }
                        } else {
                            block
                        }
                    }
                    else -> block
                }
            }
            val nextState = buildUpdatedState(current, updated)
            nextState.copy(renameOldName = trimmedNew, renameNewName = "")
        }
        return renamedCount
    }

    fun setRevisionTrackingEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isRevisionTrackingEnabled = enabled) }
    }

    fun setCurrentRevisionColor(color: RevisionColor) {
        _uiState.update { it.copy(currentRevisionColor = color) }
    }

    fun setRevisionDate(date: String) {
        _uiState.update { it.copy(revisionDate = date) }
    }

    fun toggleBlockRevised(blockId: String) {
        _uiState.update { current ->
            val updated = current.blocks.map { block ->
                if (block.id == blockId) {
                    val newRevised = !block.isRevised
                    block.copy(
                        isRevised = newRevised,
                        revisionColor = if (newRevised) current.currentRevisionColor.name else null
                    )
                } else {
                    block
                }
            }
            buildUpdatedState(current, updated)
        }
    }

    fun markAllBlocksRevised(isRevised: Boolean, color: RevisionColor = _uiState.value.currentRevisionColor) {
        _uiState.update { current ->
            val updated = current.blocks.map { block ->
                block.copy(
                    isRevised = isRevised,
                    revisionColor = if (isRevised) color.name else null
                )
            }
            buildUpdatedState(current, updated)
        }
    }

    fun toggleColoredDraftTint() {
        _uiState.update { it.copy(showColoredDraftTint = !it.showColoredDraftTint) }
    }

    fun setColoredDraftTint(enabled: Boolean) {
        _uiState.update { it.copy(showColoredDraftTint = enabled) }
    }

    fun openReportsDialog() {
        _uiState.update { it.copy(isReportsDialogOpen = true) }
    }

    fun closeReportsDialog() {
        _uiState.update { it.copy(isReportsDialogOpen = false) }
    }

    fun openRevisionDialog() {
        _uiState.update { it.copy(isRevisionDialogOpen = true) }
    }

    fun closeRevisionDialog() {
        _uiState.update { it.copy(isRevisionDialogOpen = false) }
    }

    fun getScriptAnalytics(): ScriptAnalytics {
        val state = _uiState.value
        return ScreenplayAnalyticsEngine.analyzeScreenplay(state.blocks, state.blockPageNumbers)
    }

    fun lockSceneNumbers() {
        _uiState.update { current ->
            val lockedMap = if (current.blockSceneNumbers.isNotEmpty()) {
                current.blockSceneNumbers.toMap()
            } else {
                var idx = 1
                current.blocks
                    .filter { it.type == BlockType.SCENE_HEADING && isCountableSceneHeading(it) }
                    .associate { it.id to (idx++).toString() }
            }
            val updated = current.blocks.map { block ->
                if (block.type == BlockType.SCENE_HEADING && lockedMap.containsKey(block.id)) {
                    block.copy(sceneNumber = lockedMap[block.id])
                } else block
            }
            buildUpdatedState(
                current.copy(
                    isSceneNumberingLocked = true,
                    lockedSceneNumbers = lockedMap,
                    showSceneNumbers = true
                ),
                updated
            )
        }
    }

    fun unlockSceneNumbers() {
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(
                    isSceneNumberingLocked = false,
                    lockedSceneNumbers = emptyMap()
                ),
                current.blocks
            )
        }
    }

    fun renumberAllScenes() {
        _uiState.update { current ->
            var idx = 1
            val updated = current.blocks.map { block ->
                if (block.type == BlockType.SCENE_HEADING && isCountableSceneHeading(block)) {
                    block.copy(sceneNumber = (idx++).toString())
                } else block
            }
            val lockedMap = if (current.isSceneNumberingLocked) {
                updated.filter { it.type == BlockType.SCENE_HEADING && it.sceneNumber != null }
                    .associate { it.id to it.sceneNumber!! }
            } else emptyMap()
            buildUpdatedState(
                current.copy(lockedSceneNumbers = lockedMap),
                updated
            )
        }
    }

    fun toggleSceneOmitted(sceneBlockId: String) {
        _uiState.update { current ->
            val targetIdx = current.blocks.indexOfFirst { it.id == sceneBlockId }
            if (targetIdx == -1) return@update current
            val newOmittedState = !current.blocks[targetIdx].isOmitted

            val targetSceneBlocks = mutableSetOf<String>()
            targetSceneBlocks.add(sceneBlockId)
            for (i in (targetIdx + 1) until current.blocks.size) {
                val b = current.blocks[i]
                if (isCountableSceneHeading(b)) break
                targetSceneBlocks.add(b.id)
            }

            val updated = current.blocks.map { block ->
                if (block.id in targetSceneBlocks) {
                    block.copy(isOmitted = newOmittedState)
                } else block
            }
            buildUpdatedState(current, updated, focusedBlockId = sceneBlockId)
        }
    }

    fun omitScene(sceneBlockId: String) {
        _uiState.update { current ->
            val targetIdx = current.blocks.indexOfFirst { it.id == sceneBlockId }
            if (targetIdx == -1) return@update current

            val targetSceneBlocks = mutableSetOf<String>()
            targetSceneBlocks.add(sceneBlockId)
            for (i in (targetIdx + 1) until current.blocks.size) {
                val b = current.blocks[i]
                if (isCountableSceneHeading(b)) break
                targetSceneBlocks.add(b.id)
            }

            val updated = current.blocks.map { block ->
                if (block.id in targetSceneBlocks) {
                    block.copy(isOmitted = true)
                } else block
            }
            buildUpdatedState(current, updated, focusedBlockId = sceneBlockId)
        }
    }

    fun restoreScene(sceneBlockId: String) {
        _uiState.update { current ->
            val targetIdx = current.blocks.indexOfFirst { it.id == sceneBlockId }
            if (targetIdx == -1) return@update current

            val targetSceneBlocks = mutableSetOf<String>()
            targetSceneBlocks.add(sceneBlockId)
            for (i in (targetIdx + 1) until current.blocks.size) {
                val b = current.blocks[i]
                if (isCountableSceneHeading(b)) break
                targetSceneBlocks.add(b.id)
            }

            val updated = current.blocks.map { block ->
                if (block.id in targetSceneBlocks) {
                    block.copy(isOmitted = false)
                } else block
            }
            buildUpdatedState(current, updated, focusedBlockId = sceneBlockId)
        }
    }

    fun setReportsSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(reportsSelectedTab = tabIndex) }
    }

    fun getLocationAnalytics(): LocationAnalytics {
        val state = _uiState.value
        return ScreenplayAnalyticsEngine.analyzeLocations(state.blocks, state.blockPageNumbers)
    }

    fun getCastSceneMatrix(): CastSceneMatrix {
        val state = _uiState.value
        return ScreenplayAnalyticsEngine.buildCastSceneMatrix(state.blocks, state.blockPageNumbers)
    }

    private var tableReadManagerInstance: TableReadManager? = null

    fun getTableReadManager(context: Context): TableReadManager {
        if (tableReadManagerInstance == null) {
            tableReadManagerInstance = TableReadManager(context, viewModelScope)
        }
        return tableReadManagerInstance!!
    }

    fun openSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = true) }
    }

    fun closeSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = false) }
    }

    fun initTheme(context: Context) {
        val savedMode = ThemePreferences.getThemeMode(context)
        if (_uiState.value.themeMode != savedMode) {
            _uiState.update { it.copy(themeMode = savedMode) }
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        ThemePreferences.setThemeMode(context, mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleThemeMode(context: Context) {
        val newMode = if (_uiState.value.themeMode == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
        setThemeMode(context, newMode)
    }

    fun openScriptDoctorDialog() {
        _uiState.update { it.copy(isScriptDoctorDialogOpen = true) }
    }

    fun closeScriptDoctorDialog() {
        _uiState.update { it.copy(isScriptDoctorDialogOpen = false) }
    }

    fun openTableRead(context: Context) {
        val manager = getTableReadManager(context)
        val state = _uiState.value
        val startIndex = state.blocks.indexOfFirst { it.id == state.focusedBlockId }.coerceAtLeast(0)
        manager.startReading(blocks = state.blocks, startIndex = startIndex)
        _uiState.update { it.copy(isTableReadActive = true) }
    }

    fun closeTableRead() {
        tableReadManagerInstance?.stop()
        _uiState.update { it.copy(isTableReadActive = false) }
    }

    fun openInlineAiPolish(blockId: String? = null) {
        val targetId = blockId ?: _uiState.value.focusedBlockId
        _uiState.update { it.copy(isInlineAiPolishOpen = true, inlineAiTargetBlockId = targetId) }
    }

    fun closeInlineAiPolish() {
        _uiState.update { it.copy(isInlineAiPolishOpen = false, inlineAiTargetBlockId = null) }
    }

    fun applyAiReplacement(blockId: String, newText: String) {
        updateBlockText(blockId, newText)
        closeInlineAiPolish()
    }

    fun generateStudioCoverage(context: Context) {
        val config = AiPreferences.load(context)
        if (!config.isConfigured) return
        _uiState.update { it.copy(isStudioCoverageLoading = true) }
        viewModelScope.launch {
            val state = _uiState.value
            val scriptExcerpt = state.blocks.take(80).joinToString("\n") { b ->
                if (b.type == BlockType.CHARACTER) b.text.uppercase() else b.text
            }
            val result = AiClient.generateStudioCoverage(
                config = config,
                scriptText = scriptExcerpt,
                title = state.title,
                author = state.author,
                storyBibleContext = getStoryBibleAiSnippet()
            )
            _uiState.update {
                it.copy(
                    isStudioCoverageLoading = false,
                    studioCoverageReport = result.getOrNull()
                )
            }
        }
    }

    fun exportStudioCoveragePdf(context: Context, report: StudioCoverageReport): File? {
        return try {
            val docsDir = context.getExternalFilesDir(null) ?: context.filesDir
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val safeTitle = report.scriptTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetFile = File(docsDir, "${safeTitle}_Studio_Coverage_$timestamp.pdf")
            StudioCoveragePdfExporter.exportCoverageReportToFile(report, targetFile)
            targetFile
        } catch (_: Exception) {
            null
        }
    }

    fun openDraftDiff() {
        _uiState.update { it.copy(isDraftDiffOpen = true) }
    }

    fun closeDraftDiff() {
        _uiState.update { it.copy(isDraftDiffOpen = false) }
    }

    fun openScriptLibrary() {
        _uiState.update { it.copy(isLibraryOpen = true) }
    }

    fun closeScriptLibrary() {
        _uiState.update { it.copy(isLibraryOpen = false) }
    }

    fun getProductionBreakdown(): ScriptBreakdownSummary {
        val state = _uiState.value
        return ProductionBreakdownEngine.generateBreakdown(state.blocks, state.blockPageNumbers)
    }

    fun exportProductionBreakdownPdf(context: Context): File {
        val summary = getProductionBreakdown()
        val exportsDir = ScriptFileManager.getExportsDirectory(context)
        val safeTitle = _uiState.value.title.ifBlank { "Screenplay" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val targetFile = File(exportsDir, "${safeTitle}_AD_Breakdown_$timestamp.pdf")
        ProductionBreakdownPdfExporter.exportBreakdownToFile(summary, _uiState.value.title.ifBlank { "Screenplay" }, targetFile, context)
        return targetFile
    }

    // ==========================================
    // 🎙️ GEMINI VOICE DICTATION SYSTEM
    // ==========================================

    private var dictationManagerInstance: ScreenplayDictationManager? = null

    fun getOrCreateDictationManager(context: Context): ScreenplayDictationManager {
        if (dictationManagerInstance == null) {
            dictationManagerInstance = ScreenplayDictationManager(context.applicationContext).apply {
                onSpeechFinalized = { finalSpeech ->
                    _uiState.update { it.copy(dictationLiveText = finalSpeech) }
                }
            }
        }
        return dictationManagerInstance!!
    }

    fun startDictation(context: Context) {
        val manager = getOrCreateDictationManager(context)
        _uiState.update {
            it.copy(
                isDictating = true,
                dictationLiveText = "",
                dictationError = null,
                isDictationFormatting = false
            )
        }
        manager.startListening()

        viewModelScope.launch {
            manager.livePartialText.collect { partial ->
                _uiState.update { it.copy(dictationLiveText = partial) }
            }
        }
        viewModelScope.launch {
            manager.audioRmsLevel.collect { rms ->
                _uiState.update { it.copy(dictationAudioRms = rms) }
            }
        }
        viewModelScope.launch {
            manager.dictationError.collect { err ->
                if (err != null) {
                    _uiState.update { it.copy(dictationError = err) }
                }
            }
        }
    }

    fun stopDictation() {
        dictationManagerInstance?.stopListening()
        _uiState.update { it.copy(isDictating = false, dictationAudioRms = 0f) }
    }

    fun cancelDictation() {
        dictationManagerInstance?.cancelListening()
        _uiState.update {
            it.copy(
                isDictating = false,
                dictationLiveText = "",
                dictationAudioRms = 0f,
                isDictationFormatting = false,
                dictationError = null
            )
        }
    }

    fun toggleDictation(context: Context) {
        if (_uiState.value.isDictating) {
            stopDictation()
        } else {
            startDictation(context)
        }
    }

    fun setSimulatedDictationText(text: String) {
        dictationManagerInstance?.setSimulatedText(text)
        _uiState.update { it.copy(dictationLiveText = text) }
    }

    fun insertDictatedSpeech(context: Context, customText: String? = null) {
        val speechText = customText ?: _uiState.value.dictationLiveText
        if (speechText.isBlank()) {
            cancelDictation()
            return
        }

        dictationManagerInstance?.stopListening()
        _uiState.update { it.copy(isDictationFormatting = true) }

        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.projectFormat == ProjectFormat.NOVEL) {
                val ms = currentState.novelManuscript
                val chIndex = currentState.activeChapterIndex.coerceIn(0, (ms.chapters.size - 1).coerceAtLeast(0))
                val ch = ms.chapters.getOrNull(chIndex)
                if (ch != null) {
                    val trimmed = speechText.trim()
                    val isQuote = trimmed.startsWith("“") || trimmed.startsWith("\"")
                    val clean = if (isQuote) {
                        val inner = trimmed.trim('“', '”', '"')
                        "“${inner.replaceFirstChar { c -> c.uppercase() }}”"
                    } else {
                        trimmed.replaceFirstChar { c -> c.uppercase() }
                    }
                    val type = if (isQuote) com.seshat.writer.novel.NovelParagraphType.DIALOGUE else com.seshat.writer.novel.NovelParagraphType.NARRATIVE
                    val newP = com.seshat.writer.novel.NovelParagraph(type = type, text = clean)
                    val updatedCh = if (ch.paragraphs.size == 1 && ch.paragraphs[0].text.isBlank()) {
                        ch.copy(paragraphs = listOf(newP))
                    } else {
                        ch.copy(paragraphs = ch.paragraphs + newP)
                    }
                    val updatedChapters = ms.chapters.mapIndexed { i, c -> if (i == chIndex) updatedCh else c }
                    val updatedMs = ms.copy(chapters = updatedChapters)
                    updateNovelManuscript(context, updatedMs)
                }
                _uiState.update {
                    it.copy(
                        isDictating = false,
                        isDictationFormatting = false,
                        dictationLiveText = "",
                        dictationAudioRms = 0f,
                        dictationError = null
                    )
                }
                return@launch
            }

            val config = AiPreferences.load(context)

            // Extract scene context: up to 8 blocks before focused block
            val focusIdx = currentState.blocks.indexOfFirst { it.id == currentState.focusedBlockId }
            val startIdx = if (focusIdx > 8) focusIdx - 8 else 0
            val endIdx = if (focusIdx != -1) focusIdx else currentState.blocks.size
            val contextBlocks = currentState.blocks.subList(startIdx, endIdx.coerceAtMost(currentState.blocks.size))
            val sceneContext = contextBlocks.joinToString("\n") { "${it.type.name}: ${it.text}" }

            val result = AiClient.formatDictatedSpeech(
                config = config,
                spokenText = speechText,
                sceneContext = sceneContext,
                activeCharacters = currentState.characters
            )

            val blocksToInsert = result.getOrElse {
                AiClient.formatDictationOfflineFallback(speechText, currentState.characters)
            }

            if (blocksToInsert.isNotEmpty()) {
                insertFormattedBlocks(currentState.focusedBlockId, blocksToInsert)
            }

            _uiState.update {
                it.copy(
                    isDictating = false,
                    isDictationFormatting = false,
                    dictationLiveText = "",
                    dictationAudioRms = 0f,
                    dictationError = null
                )
            }
        }
    }

    fun insertDictatedSpeechAsType(blockType: BlockType, customText: String? = null) {
        val speechText = customText ?: _uiState.value.dictationLiveText
        if (speechText.isBlank()) {
            cancelDictation()
            return
        }

        dictationManagerInstance?.stopListening()

        val newBlock = ScreenplayBlock(
            type = blockType,
            text = speechText.trim(),
            isRevised = _uiState.value.isRevisionTrackingEnabled,
            revisionColor = if (_uiState.value.isRevisionTrackingEnabled) _uiState.value.currentRevisionColor.name else null
        )

        insertFormattedBlocks(_uiState.value.focusedBlockId, listOf(newBlock))

        _uiState.update {
            it.copy(
                isDictating = false,
                isDictationFormatting = false,
                dictationLiveText = "",
                dictationAudioRms = 0f,
                dictationError = null
            )
        }
    }

    fun insertFormattedBlocks(afterId: String?, newBlocks: List<ScreenplayBlock>) {
        if (newBlocks.isEmpty()) return
        _uiState.update { current ->
            val targetId = afterId ?: current.focusedBlockId
            val index = current.blocks.indexOfFirst { it.id == targetId }

            val mutableBlocks = current.blocks.toMutableList()
            val targetBlock = if (index != -1) current.blocks[index] else null

            // If current block was empty, replace it with the new block(s)
            val insertIndex = if (index != -1) {
                if (targetBlock != null && targetBlock.text.isBlank()) {
                    mutableBlocks.removeAt(index)
                    index
                } else {
                    index + 1
                }
            } else {
                current.blocks.size
            }

            mutableBlocks.addAll(insertIndex, newBlocks)
            val nextFocusId = newBlocks.last().id
            buildUpdatedState(current, mutableBlocks, focusedBlockId = nextFocusId)
        }
    }

    // ==========================================
    // 📖 INTERACTIVE STORY BIBLE & LORE SYSTEM
    // ==========================================

    fun openStoryBible(context: Context) {
        val title = _uiState.value.title.ifBlank { "UNTITLED SCREENPLAY" }
        val loaded = StoryBibleRepository.loadBible(context, title)
        _uiState.update { it.copy(isStoryBibleOpen = true, storyBible = loaded) }
    }

    fun closeStoryBible() {
        _uiState.update { it.copy(isStoryBibleOpen = false) }
    }

    fun toggleStoryBible(context: Context) {
        if (_uiState.value.isStoryBibleOpen) {
            closeStoryBible()
        } else {
            openStoryBible(context)
        }
    }

    fun saveStoryBible(context: Context, updatedBible: StoryBible) {
        _uiState.update { current ->
            val updatedTv = current.tvSeriesProject?.copy(
                seriesBible = updatedBible,
                lastModified = System.currentTimeMillis()
            )
            current.copy(
                storyBible = updatedBible,
                tvSeriesProject = updatedTv ?: current.tvSeriesProject
            )
        }
        StoryBibleRepository.saveBible(context, updatedBible)
        _uiState.value.tvSeriesProject?.let { TvSeriesRepository.saveProject(context, it) }
    }

    fun loadStoryBibleIfPresent(context: Context) {
        val title = _uiState.value.title.ifBlank { "UNTITLED SCREENPLAY" }
        val loaded = StoryBibleRepository.loadBible(context, title)
        _uiState.update { it.copy(storyBible = loaded) }
    }

    fun getStoryBibleAiSnippet(): String {
        return _uiState.value.storyBible.toAiContextSnippet()
    }

    fun saveCurrentProject(context: Context): Boolean {
        return try {
            val format = _uiState.value.projectFormat
            val title = _uiState.value.title.ifBlank { "UNTITLED SCREENPLAY" }
            if (format == ProjectFormat.NOVEL) {
                NovelRepository.saveManuscript(context, _uiState.value.novelManuscript)
            } else if (format == ProjectFormat.TV_SERIES && _uiState.value.tvSeriesProject != null) {
                val currentProject = _uiState.value.tvSeriesProject!!
                val activeIdx = currentProject.activeEpisodeIndex.coerceIn(0, (currentProject.episodes.size - 1).coerceAtLeast(0))
                val updatedEpisodes = currentProject.episodes.toMutableList()
                if (activeIdx in updatedEpisodes.indices) {
                    val currentEp = updatedEpisodes[activeIdx]
                    updatedEpisodes[activeIdx] = currentEp.copy(
                        blocks = _uiState.value.blocks,
                        corkboard = _uiState.value.corkboardDoc,
                        sceneCount = _uiState.value.sceneCount,
                        pageCount = _uiState.value.pageCount,
                        revisionColor = _uiState.value.currentRevisionColor,
                        lastModified = System.currentTimeMillis()
                    )
                }
                val updatedProject = currentProject.copy(
                    seriesBible = _uiState.value.storyBible,
                    episodes = updatedEpisodes,
                    lastModified = System.currentTimeMillis()
                )
                TvSeriesRepository.saveProject(context, updatedProject)
                _uiState.update { it.copy(tvSeriesProject = updatedProject) }
            } else {
                val storeTitle = ScreenplayDocumentStore.titlePage.value
                val currentTitleData = if (storeTitle.title.isNotBlank()) {
                    storeTitle.copy(
                        title = title.ifBlank { storeTitle.title },
                        author = _uiState.value.author.ifBlank { storeTitle.author }
                    )
                } else {
                    TitlePageData(
                        title = title,
                        author = _uiState.value.author,
                        writtenByLabel = "written by",
                        draftInfo = "First Draft"
                    )
                }
                ScriptFileManager.saveScript(context, _uiState.value.blocks, currentTitleData, "fountain")
            }
            CorkboardRepository.saveCorkboard(context, _uiState.value.corkboardDoc)
            StoryBibleRepository.saveBible(context, _uiState.value.storyBible)
            true
        } catch (_: Exception) {
            false
        }
    }

    // ==========================================
    // 📌 GROUND-LEVEL OUTLINE & CORKBOARD SYSTEM
    // ==========================================

    fun loadCorkboardIfPresent(context: Context) {
        val title = _uiState.value.title.ifBlank { "UNTITLED SCREENPLAY" }
        val loaded = CorkboardRepository.loadCorkboard(context, title)
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = loaded),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
    }

    fun openCorkboard(context: Context) {
        loadCorkboardIfPresent(context)
        _uiState.update { it.copy(isCorkboardOpen = true) }
    }

    fun closeCorkboard() {
        _uiState.update { it.copy(isCorkboardOpen = false) }
    }

    fun toggleCorkboard(context: Context) {
        if (_uiState.value.isCorkboardOpen) {
            closeCorkboard()
        } else {
            openCorkboard(context)
        }
    }

    fun toggleCorkboardSideBySide() {
        _uiState.update { it.copy(isCorkboardSideBySide = !it.isCorkboardSideBySide) }
    }

    fun setCorkboardActCount(context: Context, newCount: Int) {
        val clamped = newCount.coerceIn(1, 9)
        val currentDoc = _uiState.value.corkboardDoc
        val newActs = CorkboardDocument.defaultActs(clamped)
        val updatedDoc = currentDoc.copy(actCount = clamped, acts = newActs)
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun updateCorkboardActTitle(context: Context, actIndex: Int, newTitle: String, newSubtitle: String = "") {
        val currentDoc = _uiState.value.corkboardDoc
        val updatedActs = currentDoc.acts.map {
            if (it.actIndex == actIndex) it.copy(title = newTitle, subtitle = newSubtitle) else it
        }
        val updatedDoc = currentDoc.copy(acts = updatedActs)
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun updateSceneCardSummary(context: Context, blockId: String, newSummary: String) {
        val currentDoc = _uiState.value.corkboardDoc
        val updatedCards = currentDoc.cards.map {
            if (it.id == blockId || it.sceneHeadingBlockId == blockId) it.copy(summary = newSummary) else it
        }
        val currentMeta = currentDoc.cardMetadata[blockId] ?: CorkboardDocument.CardMetadata()
        val updatedMetaMap = currentDoc.cardMetadata.toMutableMap()
        updatedMetaMap[blockId] = currentMeta.copy(summary = newSummary)
        val updatedDoc = currentDoc.copy(cardMetadata = updatedMetaMap, cards = updatedCards)

        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun updateSceneCardTitle(context: Context, blockId: String, newTitle: String) {
        val currentDoc = _uiState.value.corkboardDoc
        val updatedCards = currentDoc.cards.map {
            if (it.id == blockId || it.sceneHeadingBlockId == blockId) it.copy(title = newTitle) else it
        }
        val updatedDoc = currentDoc.copy(cards = updatedCards)
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun updateSceneCardColor(context: Context, blockId: String, newColorTag: String) {
        val currentDoc = _uiState.value.corkboardDoc
        val updatedCards = currentDoc.cards.map {
            if (it.id == blockId || it.sceneHeadingBlockId == blockId) it.copy(colorTag = newColorTag) else it
        }
        val currentMeta = currentDoc.cardMetadata[blockId] ?: CorkboardDocument.CardMetadata()
        val updatedMetaMap = currentDoc.cardMetadata.toMutableMap()
        updatedMetaMap[blockId] = currentMeta.copy(colorTag = newColorTag)
        val updatedDoc = currentDoc.copy(cardMetadata = updatedMetaMap, cards = updatedCards)

        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun moveSceneCardToAct(context: Context, blockId: String, targetActIndex: Int) {
        val currentCards = _uiState.value.sceneCards
        val currentCardIndex = currentCards.indexOfFirst { it.sceneHeadingBlockId == blockId }
        if (currentCardIndex == -1) return

        val clampedTargetAct = targetActIndex.coerceIn(0, _uiState.value.corkboardDoc.actCount - 1)
        if (currentCards[currentCardIndex].actIndex == clampedTargetAct) return

        val currentDoc = _uiState.value.corkboardDoc
        val updatedCards = currentDoc.cards.map {
            if (it.id == blockId || it.sceneHeadingBlockId == blockId) it.copy(actIndex = clampedTargetAct) else it
        }
        val currentMeta = currentDoc.cardMetadata[blockId] ?: CorkboardDocument.CardMetadata()
        val updatedMetaMap = currentDoc.cardMetadata.toMutableMap()
        updatedMetaMap[blockId] = currentMeta.copy(actIndex = clampedTargetAct)
        val updatedDoc = currentDoc.copy(cardMetadata = updatedMetaMap, cards = updatedCards)

        if (currentDoc.cards.isEmpty()) {
            var targetIndex = -1
            if (clampedTargetAct > currentCards[currentCardIndex].actIndex) {
                for (i in currentCards.indices) {
                    if (currentCards[i].sceneHeadingBlockId != blockId && currentCards[i].actIndex <= clampedTargetAct) {
                        targetIndex = i
                    }
                }
            } else {
                for (i in currentCards.indices) {
                    if (currentCards[i].sceneHeadingBlockId != blockId && currentCards[i].actIndex >= clampedTargetAct) {
                        targetIndex = i
                        break
                    }
                }
            }

            if (targetIndex != -1 && targetIndex != currentCardIndex) {
                moveScene(currentCardIndex, targetIndex)
            }
        }

        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun applyCorkboardTemplate(context: Context, template: CorkboardTemplate) {
        val newCards = template.beats.mapIndexed { idx, beat ->
            CorkboardCard(
                id = UUID.randomUUID().toString(),
                cardNumber = idx + 1,
                title = beat.title,
                summary = "",
                actIndex = beat.actIndex.coerceIn(0, template.actCount - 1),
                colorTag = beat.colorTag,
                prompt = beat.prompt
            )
        }
        val currentDoc = _uiState.value.corkboardDoc
        val updatedDoc = currentDoc.copy(
            actCount = template.actCount,
            acts = template.acts,
            cards = newCards,
            activeTemplateId = template.id
        )
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun addCorkboardCard(context: Context, actIndex: Int, title: String = "", summary: String = "", colorTag: String = "#F59E0B") {
        val currentDoc = _uiState.value.corkboardDoc
        val clampedAct = actIndex.coerceIn(0, currentDoc.actCount - 1)
        val nextNum = currentDoc.cards.size + 1
        val finalTitle = title.ifBlank { "BEAT $nextNum" }
        val newCard = CorkboardCard(
            id = UUID.randomUUID().toString(),
            cardNumber = nextNum,
            title = finalTitle,
            summary = summary,
            actIndex = clampedAct,
            colorTag = colorTag
        )
        val updatedCards = currentDoc.cards + newCard
        val updatedDoc = currentDoc.copy(cards = updatedCards)
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun deleteCorkboardCard(context: Context, cardId: String) {
        val currentDoc = _uiState.value.corkboardDoc
        val filtered = currentDoc.cards.filterNot { it.id == cardId || it.sceneHeadingBlockId == cardId }
        val renumbered = filtered.mapIndexed { idx, c -> c.copy(cardNumber = idx + 1) }
        val updatedDoc = currentDoc.copy(cards = renumbered)
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun clearCorkboardCards(context: Context) {
        val currentDoc = _uiState.value.corkboardDoc
        val updatedDoc = currentDoc.copy(cards = emptyList(), activeTemplateId = null)
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(corkboardDoc = updatedDoc),
                current.blocks,
                focusedBlockId = current.focusedBlockId
            )
        }
        CorkboardRepository.saveCorkboard(context, updatedDoc)
    }

    fun reorderCorkboardCard(context: Context, fromIndex: Int, toIndex: Int) {
        val currentDoc = _uiState.value.corkboardDoc
        if (currentDoc.cards.isNotEmpty()) {
            if (fromIndex in currentDoc.cards.indices && toIndex in currentDoc.cards.indices) {
                val list = currentDoc.cards.toMutableList()
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                val renumbered = list.mapIndexed { idx, c -> c.copy(cardNumber = idx + 1) }
                val updatedDoc = currentDoc.copy(cards = renumbered)
                _uiState.update { current ->
                    buildUpdatedState(
                        current.copy(corkboardDoc = updatedDoc),
                        current.blocks,
                        focusedBlockId = current.focusedBlockId
                    )
                }
                CorkboardRepository.saveCorkboard(context, updatedDoc)
            }
        } else {
            moveScene(fromIndex, toIndex)
        }
    }

    fun reorderCorkboardScene(fromIndex: Int, toIndex: Int) {
        moveScene(fromIndex, toIndex)
    }

    fun summarizeSceneCardWithAi(context: Context, blockId: String) {
        val config = AiPreferences.load(context)
        val (_, scenes) = partitionIntoScenes(_uiState.value.blocks)
        val targetScene = scenes.find { sc ->
            sc.any { it.id == blockId }
        } ?: return

        val heading = targetScene.firstOrNull { isCountableSceneHeading(it) }?.text ?: "SCENE"
        val sceneText = targetScene.joinToString("\n") { b ->
            if (b.type == BlockType.CHARACTER) b.text.uppercase(Locale.US) else b.text
        }
        val bibleSnippet = getStoryBibleAiSnippet()

        _uiState.update { current ->
            current.copy(
                sceneCards = current.sceneCards.map {
                    if (it.sceneHeadingBlockId == blockId) it.copy(isAiSummarizing = true) else it
                }
            )
        }

        viewModelScope.launch {
            val result = AiClient.summarizeScene(config, heading, sceneText, bibleSnippet)
            val summary = result.getOrElse { "Scene: $heading." }
            updateSceneCardSummary(context, blockId, summary)
        }
    }

    fun summarizeAllSceneCardsWithAi(context: Context) {
        val config = AiPreferences.load(context)
        val (_, scenes) = partitionIntoScenes(_uiState.value.blocks)
        val bibleSnippet = getStoryBibleAiSnippet()

        _uiState.update { current ->
            current.copy(
                sceneCards = current.sceneCards.map { it.copy(isAiSummarizing = true) }
            )
        }

        viewModelScope.launch {
            for (sc in scenes) {
                val headingBlock = sc.firstOrNull { isCountableSceneHeading(it) } ?: continue
                val heading = headingBlock.text
                val sceneText = sc.joinToString("\n") { b ->
                    if (b.type == BlockType.CHARACTER) b.text.uppercase(Locale.US) else b.text
                }
                val result = AiClient.summarizeScene(config, heading, sceneText, bibleSnippet)
                val summary = result.getOrElse { "Scene: $heading." }
                updateSceneCardSummary(context, headingBlock.id, summary)
            }
        }
    }

    fun openStartMenu() {
        _uiState.update { it.copy(isStartMenuOpen = true) }
    }

    fun closeStartMenu() {
        _uiState.update { it.copy(isStartMenuOpen = false) }
    }

    fun startNewProject(
        context: Context,
        format: ProjectFormat,
        screenplayType: ScreenplayType?,
        initialWorkspaceView: InitialWorkspaceView,
        customTitle: String? = null,
        template: ProjectTemplate? = null
    ) {
        val resolvedTemplate = template ?: TemplateRegistry.getBlankTemplate(format, screenplayType)
        if (format == ProjectFormat.TV_SERIES) {
            val defaultTitle = when (screenplayType) {
                ScreenplayType.TV_HALF_HOUR_COMEDY -> customTitle?.ifBlank { null } ?: "Untitled TV Comedy"
                else -> customTitle?.ifBlank { null } ?: "Untitled TV Drama"
            }
            startNewTvSeriesProject(
                context = context,
                seriesTitle = defaultTitle,
                showrunner = "",
                format = screenplayType ?: ScreenplayType.TV_HOUR_DRAMA,
                initialWorkspaceView = initialWorkspaceView,
                template = resolvedTemplate
            )
            return
        }

        val defaultTitle = when (format) {
            ProjectFormat.NOVEL -> customTitle?.ifBlank { null } ?: "Untitled Novel"
            ProjectFormat.STAGE_PLAY -> customTitle?.ifBlank { null } ?: "Untitled Stage Play"
            ProjectFormat.COMIC_BOOK -> customTitle?.ifBlank { null } ?: "Untitled Comic Book"
            ProjectFormat.SCREENPLAY -> {
                when (screenplayType) {
                    ScreenplayType.TV_HOUR_DRAMA -> customTitle?.ifBlank { null } ?: "Untitled TV Drama"
                    ScreenplayType.TV_HALF_HOUR_COMEDY -> customTitle?.ifBlank { null } ?: "Untitled TV Comedy"
                    else -> customTitle?.ifBlank { null } ?: "Untitled Feature"
                }
            }
            ProjectFormat.TV_SERIES -> customTitle?.ifBlank { null } ?: "Untitled TV Series"
        }

        val initialBlocks = when (format) {
            ProjectFormat.STAGE_PLAY -> listOf(
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "")
            )
            ProjectFormat.COMIC_BOOK -> listOf(
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "")
            )
            else -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
            )
        }

        val acts = if (resolvedTemplate.acts.isNotEmpty()) {
            resolvedTemplate.acts
        } else when (format) {
            ProjectFormat.NOVEL -> CorkboardDocument.actsForNovel()
            ProjectFormat.STAGE_PLAY -> CorkboardDocument.actsForStagePlay()
            ProjectFormat.COMIC_BOOK -> CorkboardDocument.actsForComicBook()
            ProjectFormat.SCREENPLAY, ProjectFormat.TV_SERIES -> when (screenplayType) {
                ScreenplayType.TV_HOUR_DRAMA -> CorkboardDocument.actsForTvHourlong()
                ScreenplayType.TV_HALF_HOUR_COMEDY -> CorkboardDocument.actsForTvHalfHour()
                else -> CorkboardDocument.actsForFeature()
            }
        }

        // Save customized corkboard document
        val initialCorkboard = CorkboardDocument(
            scriptTitle = defaultTitle,
            actCount = acts.size,
            acts = acts
        )
        CorkboardRepository.saveCorkboard(context, initialCorkboard)

        // Save starter story bible
        val initialBible = StoryBible(
            scriptTitle = defaultTitle,
            beats = resolvedTemplate.storyBibleBeats
        )
        StoryBibleRepository.saveBible(context, initialBible)

        val starterManuscript = if (format == ProjectFormat.NOVEL) {
            val chaps = if (resolvedTemplate.novelChapters.isNotEmpty()) resolvedTemplate.novelChapters else NovelManuscript.defaultChapters()
            val ms = NovelManuscript(
                title = defaultTitle,
                author = "",
                acts = acts,
                chapters = chaps
            )
            NovelRepository.saveManuscript(context, ms)
            ms
        } else {
            NovelManuscript(title = defaultTitle)
        }

        val initialChapterCards = if (format == ProjectFormat.NOVEL) {
            starterManuscript.chapters.map { ch ->
                SceneCard(
                    sceneHeadingBlockId = ch.id,
                    sceneNumber = ch.chapterNumber,
                    headingText = ch.title.uppercase(Locale.US),
                    summary = ch.synopsis,
                    actIndex = ch.actIndex,
                    colorTag = ch.colorTag,
                    blockCount = ch.paragraphs.size,
                    estimatedMinutes = (ch.wordCount() / 225.0f).coerceAtLeast(1.0f)
                )
            }
        } else emptyList()

        loadDocument(initialBlocks, TitlePageData(title = defaultTitle))

        // Save to local library
        try {
            val safeName = defaultTitle.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim('_').ifBlank { "Screenplay" }
            val titlePage = TitlePageData(title = defaultTitle, author = "", writtenByLabel = "written by", draftInfo = "First Draft")
            val content = FountainSerializer.exportToFountain(initialBlocks, titlePage)
            val file = File(ScriptLibraryManager.getScriptsDirectory(context), "$safeName.fountain")
            file.writeText(content)
        } catch (_: Exception) {}

        _uiState.update { current ->
            current.copy(
                isStartMenuOpen = false,
                projectFormat = format,
                screenplayType = screenplayType ?: ScreenplayType.FEATURE,
                corkboardDoc = initialCorkboard,
                storyBible = initialBible,
                novelManuscript = starterManuscript,
                sceneCards = if (initialChapterCards.isNotEmpty()) initialChapterCards else current.sceneCards,
                activeChapterIndex = 0,
                isStoryBibleOpen = initialWorkspaceView == InitialWorkspaceView.LORE_DOC,
                isCorkboardOpen = initialWorkspaceView == InitialWorkspaceView.OUTLINE,
                isCorkboardSideBySide = false
            )
        }
    }

    fun selectNovelChapter(index: Int) {
        _uiState.update {
            it.copy(activeChapterIndex = index.coerceIn(0, (it.novelManuscript.chapters.size - 1).coerceAtLeast(0)))
        }
    }

    fun updateNovelManuscript(context: Context, manuscript: NovelManuscript) {
        _uiState.update { current ->
            val chapterCards = NovelRepository.chaptersToSceneCards(manuscript)
            current.copy(
                novelManuscript = manuscript,
                sceneCards = chapterCards
            )
        }
        NovelRepository.saveManuscript(context, manuscript)
    }

    fun openNovelCompiler() {
        _uiState.update { it.copy(isCompilerDialogOpen = true) }
    }

    fun closeNovelCompiler() {
        _uiState.update { it.copy(isCompilerDialogOpen = false) }
    }

    fun loadNovelIfPresent(context: Context) {
        if (_uiState.value.projectFormat == ProjectFormat.NOVEL) {
            val ms = NovelRepository.loadManuscript(context, _uiState.value.title)
            val chapterCards = NovelRepository.chaptersToSceneCards(ms)
            _uiState.update {
                it.copy(
                    novelManuscript = ms,
                    sceneCards = chapterCards
                )
            }
        }
    }

    fun startNewTvSeriesProject(
        context: Context,
        seriesTitle: String,
        showrunner: String = "",
        format: ScreenplayType = ScreenplayType.TV_HOUR_DRAMA,
        initialWorkspaceView: InitialWorkspaceView = InitialWorkspaceView.EDITOR,
        template: ProjectTemplate? = null
    ) {
        var series = TvSeriesProject.createDefault(
            title = seriesTitle.ifBlank { "Untitled TV Series" },
            showrunner = showrunner,
            format = format,
            pilotTitle = "Pilot"
        )
        if (template != null) {
            val pilotBlocks = listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = ""))
            val pilotActs = if (template.acts.isNotEmpty()) template.acts else series.episodes.first().corkboard.acts
            val updatedPilot = series.episodes.first().copy(
                blocks = pilotBlocks,
                corkboard = CorkboardDocument(
                    scriptTitle = "${series.title} - Ep 101: Pilot",
                    actCount = pilotActs.size,
                    acts = pilotActs
                )
            )
            series = series.copy(episodes = listOf(updatedPilot))
        }
        TvSeriesRepository.saveProject(context, series)

        val activeEp = series.episodes.first()
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(
                    isStartMenuOpen = false,
                    projectFormat = ProjectFormat.TV_SERIES,
                    screenplayType = format,
                    tvSeriesProject = series,
                    title = "${series.title} - Ep ${activeEp.episodeNumber}: ${activeEp.title}",
                    author = showrunner,
                    blocks = activeEp.blocks,
                    focusedBlockId = activeEp.blocks.firstOrNull()?.id ?: "",
                    corkboardDoc = activeEp.corkboard,
                    storyBible = series.seriesBible,
                    isStoryBibleOpen = initialWorkspaceView == InitialWorkspaceView.LORE_DOC,
                    isCorkboardOpen = initialWorkspaceView == InitialWorkspaceView.OUTLINE,
                    isCorkboardSideBySide = false
                ),
                activeEp.blocks,
                focusedBlockId = activeEp.blocks.firstOrNull()?.id ?: ""
            )
        }
    }

    fun selectTvEpisode(context: Context, index: Int) {
        val currentSeries = _uiState.value.tvSeriesProject ?: return
        if (index !in currentSeries.episodes.indices) return

        // 1. Sync current episode state
        val oldIdx = currentSeries.activeEpisodeIndex.coerceIn(0, currentSeries.episodes.size - 1)
        val updatedEpisodes = currentSeries.episodes.toMutableList()
        if (oldIdx in updatedEpisodes.indices) {
            val oldEp = updatedEpisodes[oldIdx]
            updatedEpisodes[oldIdx] = oldEp.copy(
                blocks = _uiState.value.blocks,
                corkboard = _uiState.value.corkboardDoc,
                sceneCount = _uiState.value.sceneCount,
                pageCount = _uiState.value.pageCount,
                revisionColor = _uiState.value.currentRevisionColor,
                lastModified = System.currentTimeMillis()
            )
        }

        // 2. Select new episode
        val targetEp = updatedEpisodes[index]
        val updatedSeries = currentSeries.copy(
            seriesBible = _uiState.value.storyBible,
            episodes = updatedEpisodes,
            activeEpisodeIndex = index,
            lastModified = System.currentTimeMillis()
        )
        TvSeriesRepository.saveProject(context, updatedSeries)

        // 3. Update UI state: target episode's blocks, its corkboard outline, and persistent lore bible
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(
                    tvSeriesProject = updatedSeries,
                    title = "${updatedSeries.title} - Ep ${targetEp.episodeNumber}: ${targetEp.title}",
                    blocks = targetEp.blocks,
                    focusedBlockId = targetEp.blocks.firstOrNull()?.id ?: "",
                    corkboardDoc = targetEp.corkboard,
                    currentRevisionColor = targetEp.revisionColor
                ),
                targetEp.blocks,
                focusedBlockId = targetEp.blocks.firstOrNull()?.id ?: ""
            )
        }
    }

    fun addTvEpisode(
        context: Context,
        episodeNumber: String,
        episodeTitle: String,
        writerCredit: String = ""
    ) {
        val currentSeries = _uiState.value.tvSeriesProject ?: return
        val acts = if (currentSeries.format == ScreenplayType.TV_HALF_HOUR_COMEDY) {
            CorkboardDocument.actsForTvHalfHour()
        } else {
            CorkboardDocument.actsForTvHourlong()
        }
        val newCorkboard = CorkboardDocument(
            scriptTitle = "${currentSeries.title} - Ep $episodeNumber: $episodeTitle",
            actCount = acts.size,
            acts = acts
        )
        val initialBlock = ScreenplayBlock(
            type = BlockType.SCENE_HEADING,
            text = ""
        )
        val newEp = TvEpisode(
            episodeNumber = episodeNumber,
            title = episodeTitle,
            writerCredit = writerCredit.ifBlank { currentSeries.showrunner },
            blocks = listOf(initialBlock),
            corkboard = newCorkboard,
            revisionColor = RevisionColor.WHITE,
            draftTag = "White Draft"
        )

        val updatedEpisodes = currentSeries.episodes + newEp
        val updatedSeries = currentSeries.copy(
            episodes = updatedEpisodes,
            lastModified = System.currentTimeMillis()
        )
        _uiState.update { it.copy(tvSeriesProject = updatedSeries) }
        TvSeriesRepository.saveProject(context, updatedSeries)

        // Automatically switch to the newly created episode
        selectTvEpisode(context, updatedEpisodes.size - 1)
    }

    fun reorderTvEpisodes(context: Context, fromIndex: Int, toIndex: Int) {
        val currentSeries = _uiState.value.tvSeriesProject ?: return
        if (fromIndex !in currentSeries.episodes.indices || toIndex !in currentSeries.episodes.indices) return
        val list = currentSeries.episodes.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        val updatedSeries = currentSeries.copy(
            episodes = list,
            activeEpisodeIndex = if (currentSeries.activeEpisodeIndex == fromIndex) toIndex else currentSeries.activeEpisodeIndex,
            lastModified = System.currentTimeMillis()
        )
        _uiState.update { it.copy(tvSeriesProject = updatedSeries) }
        TvSeriesRepository.saveProject(context, updatedSeries)
    }

    fun deleteTvEpisode(context: Context, index: Int) {
        val currentSeries = _uiState.value.tvSeriesProject ?: return
        if (currentSeries.episodes.size <= 1) return
        if (index !in currentSeries.episodes.indices) return

        val list = currentSeries.episodes.toMutableList()
        list.removeAt(index)
        val newActiveIndex = if (index == currentSeries.activeEpisodeIndex) {
            0.coerceAtMost(list.size - 1)
        } else if (currentSeries.activeEpisodeIndex > index) {
            currentSeries.activeEpisodeIndex - 1
        } else {
            currentSeries.activeEpisodeIndex
        }

        val updatedSeries = currentSeries.copy(
            episodes = list,
            activeEpisodeIndex = newActiveIndex,
            lastModified = System.currentTimeMillis()
        )
        _uiState.update { it.copy(tvSeriesProject = updatedSeries) }
        TvSeriesRepository.saveProject(context, updatedSeries)

        val targetEp = list[newActiveIndex]
        _uiState.update { current ->
            buildUpdatedState(
                current.copy(
                    title = "${updatedSeries.title} - Ep ${targetEp.episodeNumber}: ${targetEp.title}",
                    blocks = targetEp.blocks,
                    focusedBlockId = targetEp.blocks.firstOrNull()?.id ?: "",
                    corkboardDoc = targetEp.corkboard,
                    currentRevisionColor = targetEp.revisionColor
                ),
                targetEp.blocks,
                focusedBlockId = targetEp.blocks.firstOrNull()?.id ?: ""
            )
        }
    }

    fun loadTvSeriesIfPresent(context: Context) {
        if (_uiState.value.projectFormat == ProjectFormat.TV_SERIES) {
            val title = _uiState.value.tvSeriesProject?.title ?: _uiState.value.title
            val loaded = TvSeriesRepository.loadProject(context, title)
            if (loaded != null) {
                val activeEp = loaded.getActiveEpisode() ?: loaded.episodes.firstOrNull()
                if (activeEp != null) {
                    _uiState.update { current ->
                        buildUpdatedState(
                            current.copy(
                                tvSeriesProject = loaded,
                                title = "${loaded.title} - Ep ${activeEp.episodeNumber}: ${activeEp.title}",
                                blocks = activeEp.blocks,
                                focusedBlockId = activeEp.blocks.firstOrNull()?.id ?: "",
                                corkboardDoc = activeEp.corkboard,
                                storyBible = loaded.seriesBible,
                                currentRevisionColor = activeEp.revisionColor
                            ),
                            activeEp.blocks,
                            focusedBlockId = activeEp.blocks.firstOrNull()?.id ?: ""
                        )
                    }
                }
            }
        }
    }

    fun openWritersRoom() {
        _uiState.update { it.copy(isWritersRoomOpen = true) }
    }

    fun closeWritersRoom() {
        _uiState.update { it.copy(isWritersRoomOpen = false) }
    }

    fun toggleWritersRoom() {
        _uiState.update { it.copy(isWritersRoomOpen = !it.isWritersRoomOpen) }
    }

    fun openStudioDrawer() {
        _uiState.update { it.copy(isStudioDrawerOpen = true) }
    }

    fun closeStudioDrawer() {
        _uiState.update { it.copy(isStudioDrawerOpen = false) }
    }

    fun toggleStudioDrawer() {
        _uiState.update { it.copy(isStudioDrawerOpen = !it.isStudioDrawerOpen) }
    }

    fun openCoverCreator() {
        _uiState.update { it.copy(isCoverCreatorOpen = true) }
    }

    fun closeCoverCreator() {
        _uiState.update { it.copy(isCoverCreatorOpen = false) }
    }

    override fun onCleared() {
        super.onCleared()
        tableReadManagerInstance?.release()
        dictationManagerInstance?.destroy()
    }
}
