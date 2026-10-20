package com.seshat.writer.ui

import androidx.compose.ui.layout.LayoutCoordinates
import com.seshat.writer.model.BlockType
import com.seshat.writer.screenplay.CursorTarget

/**
 * Encapsulates display, layout, and visual state flags for rendering a screenplay block.
 */
data class BlockDisplayConfig(
    val nextBlockType: BlockType? = null,
    val isFocused: Boolean = false,
    val isHighlighted: Boolean = false,
    val isDualColumn: Boolean = false,
    val cursorTarget: CursorTarget = CursorTarget(),
    val isFirstBlock: Boolean = false,
    val zoom: Float = 1.0f,
    val showSceneNumbers: Boolean = false,
    val sceneNumber: String? = null,
    val isFindReplaceVisible: Boolean = false,
    val showWriterNotes: Boolean = true,
    val isRevisionTrackingEnabled: Boolean = false,
    val isAiPolishEnabled: Boolean = false,
    val isDictating: Boolean = false,
    val isCompactScreen: Boolean = false,
    val isKeyboardActive: Boolean = false
)

/**
 * Encapsulates event callbacks and action handlers for screenplay block interaction.
 */
data class BlockCallbacks(
    val onTextChange: (String) -> Unit = {},
    val onEnter: () -> Unit = {},
    val onTab: () -> Unit = {},
    val onBackspaceOnEmpty: () -> Unit = {},
    val onNavigateUp: (Int) -> Unit = {},
    val onNavigateDown: (Int) -> Unit = {},
    val onNavigateLeft: () -> Unit = {},
    val onNavigateRight: () -> Unit = {},
    val onZoomIn: () -> Unit = {},
    val onZoomOut: () -> Unit = {},
    val onResetZoom: () -> Unit = {},
    val onFocused: () -> Unit = {},
    val onOpenFind: (Boolean) -> Unit = {},
    val onCloseFind: () -> Unit = {},
    val onToggleHighlight: (Boolean) -> Unit = {},
    val onDragHighlightStart: () -> Unit = {},
    val onDragHighlightMove: (Float) -> Unit = {},
    val onDragHighlightEnd: () -> Unit = {},
    val onCoordinatesChanged: (LayoutCoordinates) -> Unit = {},
    val onExpandHighlightDown: () -> Unit = {},
    val onExpandHighlightUp: () -> Unit = {},
    val onClearHighlight: () -> Unit = {},
    val onUpdateNote: (String) -> Unit = {},
    val onDeleteNote: () -> Unit = {},
    val onToggleRevised: () -> Unit = {},
    val onOpenAiPolish: () -> Unit = {},
    val onToggleDictation: () -> Unit = {},
    val onCancelDictation: () -> Unit = {},
    val onOpenStoryBible: () -> Unit = {},
    val onToggleCorkboard: () -> Unit = {},
    val onNavigateDocumentStart: () -> Unit = {},
    val onNavigateDocumentEnd: () -> Unit = {},
    val onNavigatePreviousScene: () -> Unit = {},
    val onNavigateNextScene: () -> Unit = {},
    val onNavigatePageUp: () -> Unit = {},
    val onNavigatePageDown: () -> Unit = {}
)

/**
 * Encapsulates event callbacks and action handlers for dual dialogue column interactions.
 */
data class DualDialogueActions(
    val onTextChange: (String, String) -> Unit = { _, _ -> },
    val onEnter: (String) -> Unit = {},
    val onTab: (String) -> Unit = {},
    val onBackspaceOnEmpty: (String) -> Unit = {},
    val onNavigateUp: (String, Int) -> Unit = { _, _ -> },
    val onNavigateDown: (String, Int) -> Unit = { _, _ -> },
    val onNavigateLeft: (String) -> Unit = {},
    val onNavigateRight: (String) -> Unit = {},
    val onZoomIn: () -> Unit = {},
    val onZoomOut: () -> Unit = {},
    val onResetZoom: () -> Unit = {},
    val onFocused: (String) -> Unit = {},
    val onToggleHighlight: (String, Boolean) -> Unit = { _, _ -> },
    val onExpandHighlightDown: (String) -> Unit = {},
    val onExpandHighlightUp: (String) -> Unit = {},
    val onClearHighlight: () -> Unit = {},
    val onUpdateNote: (String, String?) -> Unit = { _, _ -> },
    val onDeleteNote: (String) -> Unit = {},
    val onToggleRevised: (String) -> Unit = {},
    val onToggleDictation: () -> Unit = {},
    val onCancelDictation: () -> Unit = {},
    val onOpenStoryBible: () -> Unit = {},
    val onToggleCorkboard: () -> Unit = {}
)

/**
 * Encapsulates editor workstation and canvas actions, delegating directly to ScreenplayViewModel.
 * Consolidating actions avoids Dalvik method parameter explosion and bytecode verifier register exhaustion.
 */
class EditorCanvasActions(
    val viewModel: com.seshat.writer.screenplay.ScreenplayViewModel,
    val context: android.content.Context
) {
    fun updateBlock(id: String, text: String) = viewModel.updateBlockText(id, text)
    fun enterPressed(id: String) = viewModel.onEnterPressed(id)
    fun tabPressed(id: String) = viewModel.onTabPressed(id)
    fun backspaceEmpty(id: String) = viewModel.onBackspaceOnEmptyBlock(id)
    fun focusBlock(id: String) = viewModel.setFocusedBlock(id)
    fun navigateUp(id: String, col: Int) = viewModel.navigateUp(id, col)
    fun navigateDown(id: String, col: Int) = viewModel.navigateDown(id, col)
    fun navigateLeft(id: String) = viewModel.navigateLeft(id)
    fun navigateRight(id: String) = viewModel.navigateRight(id)
    fun navigateDocumentStart() = viewModel.navigateDocumentStart()
    fun navigateDocumentEnd() = viewModel.navigateDocumentEnd()
    fun navigatePreviousScene() = viewModel.navigatePreviousScene()
    fun navigateNextScene() = viewModel.navigateNextScene()
    fun navigatePageUp(id: String) = viewModel.navigatePageUp(id)
    fun navigatePageDown(id: String) = viewModel.navigatePageDown(id)
    fun changeBlockType(id: String, type: com.seshat.writer.model.BlockType) = viewModel.changeBlockType(id, type)
    fun insertNewBlock(id: String, type: com.seshat.writer.model.BlockType) = viewModel.insertNewBlock(id, type)
    fun toggleExtension(id: String, ext: String) = viewModel.toggleCharacterExtension(id, ext)
    fun setTransition(id: String, text: String) = viewModel.setTransitionPreset(id, text)
    fun applyCharacterSuggestion(id: String, name: String) = viewModel.applyCharacterSuggestion(id, name)
    fun applyScenePrefix(id: String, prefix: String) = viewModel.applyScenePrefix(id, prefix)
    fun applySceneLocation(id: String, loc: String) = viewModel.applySceneLocation(id, loc)
    fun applySceneTime(id: String, time: String) = viewModel.applySceneTime(id, time)
    fun toggleSidebar() = viewModel.toggleSidebar()
    fun openStudioDrawer() = viewModel.openStudioDrawer()
    fun toggleZenMode() = viewModel.toggleZenMode()
    fun toggleViewMode() = viewModel.toggleViewMode()
    fun zoomIn() = viewModel.zoomIn()
    fun zoomOut() = viewModel.zoomOut()
    fun resetZoom() = viewModel.resetZoom()
    fun toggleBlockHighlight(id: String, isShift: Boolean) = viewModel.toggleBlockHighlight(id, isShift)
    fun highlightBlockRange(fromId: String, toId: String) = viewModel.highlightBlockRange(fromId, toId)
    fun clearHighlight() = viewModel.clearHighlight()
    fun expandHighlightDown(id: String) = viewModel.expandHighlightDown(id)
    fun expandHighlightUp(id: String) = viewModel.expandHighlightUp(id)
    fun deleteHighlight() = viewModel.deleteHighlightedBlocks()
    fun makeDualDialogue() = viewModel.makeDualDialogue()
    fun separateDualDialogue() = viewModel.separateDualDialogue()
    fun isDualDialogueCandidate(highlightedIds: Set<String>) = viewModel.isDualDialogueCandidate(highlightedIds)
    fun isAlreadyDualDialogue(highlightedIds: Set<String>) = viewModel.isAlreadyDualDialogue(highlightedIds)
    fun toggleSceneNumbers() = viewModel.toggleSceneNumbers()
    fun toggleWriterNotes() = viewModel.toggleWriterNotes()
    fun setBlockNote(id: String, note: String?) = viewModel.setBlockNote(id, note)
    fun deleteBlockNote(id: String) = viewModel.deleteBlockNote(id)
    fun openFind(replace: Boolean) = viewModel.openFind(replace)
    fun closeFind() = viewModel.closeFind()
    fun updateSearchQuery(query: String) = viewModel.updateSearchQuery(query)
    fun updateReplaceQuery(replace: String) = viewModel.updateReplaceQuery(replace)
    fun toggleMatchCase() = viewModel.toggleMatchCase()
    fun toggleMatchWholeWord() = viewModel.toggleMatchWholeWord()
    fun findNext() = viewModel.findNext()
    fun findPrevious() = viewModel.findPrevious()
    fun replaceCurrent() = viewModel.replaceCurrent()
    fun replaceAll() = viewModel.replaceAll()
    fun setFindMode(mode: com.seshat.writer.screenplay.FindMode) = viewModel.setFindMode(mode)
    fun setRenameOldName(name: String) = viewModel.setRenameOldName(name)
    fun setRenameNewName(name: String) = viewModel.setRenameNewName(name)
    fun setRenameCuesOnly(cuesOnly: Boolean) = viewModel.setRenameCuesOnly(cuesOnly)
    fun renameCharacter() = viewModel.renameCharacter()
    fun toggleBlockRevised(id: String) = viewModel.toggleBlockRevised(id)
    fun openAiPolish(id: String) = viewModel.openInlineAiPolish(id)
    fun toggleDictation() = viewModel.toggleDictation(context)
    fun cancelDictation() = viewModel.cancelDictation()
    fun openStoryBible() = viewModel.openStoryBible(context)
    fun toggleCorkboard() = viewModel.toggleCorkboard(context)
    fun openStartMenu() = viewModel.openStartMenu()
    fun openWritersRoom() = viewModel.openWritersRoom()
    fun openScriptDoctor() = viewModel.openScriptDoctorDialog()
    fun toggleThemeMode() = viewModel.toggleThemeMode(context)
    fun openCompiler() = viewModel.openNovelCompiler()
}
