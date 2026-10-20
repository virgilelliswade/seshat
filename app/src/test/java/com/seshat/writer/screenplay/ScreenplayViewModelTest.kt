package com.seshat.writer.screenplay

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.DualDialogueSide
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScreenplayViewModelTest {

    private lateinit var viewModel: ScreenplayViewModel

    @Before
    fun setUp() {
        viewModel = ScreenplayViewModel()
    }

    @Test
    fun initialStateContainsSingleEmptySceneHeadingBlock() {
        val state = viewModel.uiState.value
        assertEquals(1, state.blocks.size)
        assertEquals(BlockType.SCENE_HEADING, state.blocks[0].type)
        assertEquals("", state.blocks[0].text)
        assertEquals(0, state.wordCount)
        assertEquals(1, state.sceneCount)
    }

    @Test
    fun updateBlockTextForcesUppercaseForSceneHeading() {
        val blockId = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(blockId, "ext. desert oasis - sunset")

        val state = viewModel.uiState.value
        assertEquals("EXT. DESERT OASIS - SUNSET", state.blocks[0].text)
        assertEquals(4, state.wordCount)
        assertEquals(1, state.scenes.size)
        assertEquals("EXT. DESERT OASIS - SUNSET", state.scenes[0].heading)
    }

    @Test
    fun enterFromSceneHeadingCreatesActionBlock() {
        val headingId = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(headingId, "INT. ARCHIVE - DAY")
        viewModel.onEnterPressed(headingId)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.SCENE_HEADING, state.blocks[0].type)
        assertEquals(BlockType.ACTION, state.blocks[1].type)
        assertEquals(state.blocks[1].id, state.focusedBlockId)
    }

    @Test
    fun backspaceOnEmptyActionBlockRemovesItAndFocusesSceneHeading() {
        val headingId = viewModel.uiState.value.blocks[0].id
        viewModel.onEnterPressed(headingId)

        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.onBackspaceOnEmptyBlock(actionId)

        val state = viewModel.uiState.value
        assertEquals(1, state.blocks.size)
        assertEquals(headingId, state.blocks[0].id)
        assertEquals(headingId, state.focusedBlockId)
    }

    @Test
    fun toggleZenModeChangesState() {
        assertFalse(viewModel.uiState.value.isZenMode)
        viewModel.toggleZenMode()
        assertTrue(viewModel.uiState.value.isZenMode)
        viewModel.toggleZenMode()
        assertFalse(viewModel.uiState.value.isZenMode)
    }

    @Test
    fun toggleSidebarChangesState() {
        assertTrue(viewModel.uiState.value.isSidebarExpanded)
        viewModel.toggleSidebar()
        assertFalse(viewModel.uiState.value.isSidebarExpanded)
    }

    @Test
    fun defaultViewModeIsPageViewAndCanToggle() {
        assertEquals(ScreenplayViewMode.PAGE_VIEW, viewModel.uiState.value.viewMode)
        viewModel.toggleViewMode()
        assertEquals(ScreenplayViewMode.INLINE_VIEW, viewModel.uiState.value.viewMode)
        viewModel.toggleViewMode()
        assertEquals(ScreenplayViewMode.PAGE_VIEW, viewModel.uiState.value.viewMode)
        viewModel.setViewMode(ScreenplayViewMode.INLINE_VIEW)
        assertEquals(ScreenplayViewMode.INLINE_VIEW, viewModel.uiState.value.viewMode)
    }

    @Test
    fun zoomControlsChangeLevelWithinBounds() {
        assertEquals(1.0f, viewModel.uiState.value.zoomLevel, 0.001f)

        viewModel.zoomIn()
        assertEquals(1.25f, viewModel.uiState.value.zoomLevel, 0.001f)

        viewModel.zoomIn()
        viewModel.zoomIn()
        viewModel.zoomIn()
        viewModel.zoomIn() // At or exceeds 2.0f
        assertEquals(2.0f, viewModel.uiState.value.zoomLevel, 0.001f)

        viewModel.resetZoom()
        assertEquals(1.0f, viewModel.uiState.value.zoomLevel, 0.001f)

        viewModel.zoomOut()
        assertEquals(0.75f, viewModel.uiState.value.zoomLevel, 0.001f)

        viewModel.zoomOut() // Should clamp at 0.75f
        assertEquals(0.75f, viewModel.uiState.value.zoomLevel, 0.001f)

        viewModel.setZoom(1.5f)
        assertEquals(1.5f, viewModel.uiState.value.zoomLevel, 0.001f)
    }

    @Test
    fun arrowNavigationNavigatesBetweenBlocksAcrossPage() {
        val headingId = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(headingId, "INT. CONTROL ROOM - NIGHT")
        viewModel.onEnterPressed(headingId)
        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(actionId, "The sonar screen pulses.")
        viewModel.onEnterPressed(actionId)
        val charId = viewModel.uiState.value.blocks[2].id
        viewModel.changeBlockType(charId, BlockType.CHARACTER)
        viewModel.updateBlockText(charId, "CAPTAIN")

        // Navigate Down from Heading at column 10 -> Action
        viewModel.navigateDown(headingId, currentColumn = 10)
        var state = viewModel.uiState.value
        assertEquals(actionId, state.focusedBlockId)
        assertEquals(CursorTargetType.TOP_LINE_COLUMN, state.cursorTarget.type)
        assertEquals(10, state.cursorTarget.column)

        // Navigate Down from Action at column 5 -> Character
        viewModel.navigateDown(actionId, currentColumn = 5)
        state = viewModel.uiState.value
        assertEquals(charId, state.focusedBlockId)
        assertEquals(CursorTargetType.TOP_LINE_COLUMN, state.cursorTarget.type)
        assertEquals(5, state.cursorTarget.column)

        // Boundary: Navigate Down on last block stays on last block
        viewModel.navigateDown(charId, currentColumn = 3)
        assertEquals(charId, viewModel.uiState.value.focusedBlockId)

        // Navigate Up from Character at column 4 -> Action
        viewModel.navigateUp(charId, currentColumn = 4)
        state = viewModel.uiState.value
        assertEquals(actionId, state.focusedBlockId)
        assertEquals(CursorTargetType.BOTTOM_LINE_COLUMN, state.cursorTarget.type)
        assertEquals(4, state.cursorTarget.column)

        // Navigate Up from Action at column 8 -> Heading
        viewModel.navigateUp(actionId, currentColumn = 8)
        state = viewModel.uiState.value
        assertEquals(headingId, state.focusedBlockId)
        assertEquals(CursorTargetType.BOTTOM_LINE_COLUMN, state.cursorTarget.type)
        assertEquals(8, state.cursorTarget.column)

        // Boundary: Navigate Up on first block stays on first block
        viewModel.navigateUp(headingId, currentColumn = 2)
        assertEquals(headingId, viewModel.uiState.value.focusedBlockId)

        // Navigate Right from Heading at end -> Action at START
        viewModel.navigateRight(headingId)
        state = viewModel.uiState.value
        assertEquals(actionId, state.focusedBlockId)
        assertEquals(CursorTargetType.START, state.cursorTarget.type)

        // Navigate Left from Action at start -> Heading at END
        viewModel.navigateLeft(actionId)
        state = viewModel.uiState.value
        assertEquals(headingId, state.focusedBlockId)
        assertEquals(CursorTargetType.END, state.cursorTarget.type)
    }

    @Test
    fun blockHighlightTogglesAndRangeSelection() {
        val headingId = viewModel.uiState.value.blocks[0].id
        viewModel.onEnterPressed(headingId)
        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.onEnterPressed(actionId)
        val charId = viewModel.uiState.value.blocks[2].id

        // 1. Single toggle adds block
        viewModel.toggleBlockHighlight(headingId)
        assertTrue(viewModel.uiState.value.highlightedBlockIds.contains(headingId))
        assertEquals(headingId, viewModel.uiState.value.selectionAnchorBlockId)

        // 2. Second toggle removes block
        viewModel.toggleBlockHighlight(headingId)
        assertFalse(viewModel.uiState.value.highlightedBlockIds.contains(headingId))

        // 3. Highlight range
        viewModel.highlightBlockRange(headingId, charId)
        val highlighted = viewModel.uiState.value.highlightedBlockIds
        assertEquals(3, highlighted.size)
        assertTrue(highlighted.contains(headingId))
        assertTrue(highlighted.contains(actionId))
        assertTrue(highlighted.contains(charId))

        // 4. Clear highlight
        viewModel.clearHighlight()
        assertTrue(viewModel.uiState.value.highlightedBlockIds.isEmpty())
        assertNull(viewModel.uiState.value.selectionAnchorBlockId)
    }

    @Test
    fun expandHighlightDownAndUp() {
        val headingId = viewModel.uiState.value.blocks[0].id
        viewModel.onEnterPressed(headingId)
        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.onEnterPressed(actionId)
        val charId = viewModel.uiState.value.blocks[2].id

        viewModel.setFocusedBlock(headingId)
        viewModel.expandHighlightDown(headingId)
        var state = viewModel.uiState.value
        assertEquals(setOf(headingId, actionId), state.highlightedBlockIds)
        assertEquals(actionId, state.focusedBlockId)

        viewModel.expandHighlightDown(actionId)
        state = viewModel.uiState.value
        assertEquals(setOf(headingId, actionId, charId), state.highlightedBlockIds)
        assertEquals(charId, state.focusedBlockId)

        viewModel.expandHighlightUp(charId)
        state = viewModel.uiState.value
        assertEquals(setOf(headingId, actionId), state.highlightedBlockIds)
        assertEquals(actionId, state.focusedBlockId)
    }

    @Test
    fun dualDialogueCandidateDetection() {
        val blocks = listOf(
            ScreenplayBlock(id = "h1", type = BlockType.SCENE_HEADING, text = "INT. LAB - DAY"),
            ScreenplayBlock(id = "c1", type = BlockType.CHARACTER, text = "ALICE"),
            ScreenplayBlock(id = "d1", type = BlockType.DIALOGUE, text = "Look at this reading!"),
            ScreenplayBlock(id = "c2", type = BlockType.CHARACTER, text = "BOB"),
            ScreenplayBlock(id = "d2", type = BlockType.DIALOGUE, text = "It's impossible!"),
            ScreenplayBlock(id = "a1", type = BlockType.ACTION, text = "The machine hums loudly.")
        )
        viewModel.loadDocument(blocks, TitlePageData())

        // Alice + Alice dialogue + Bob + Bob dialogue -> Candidate
        val validPair = setOf("c1", "d1", "c2", "d2")
        assertTrue(viewModel.isDualDialogueCandidate(validPair))

        // Just character names -> Candidate (range expands to cover the dialogue)
        assertTrue(viewModel.isDualDialogueCandidate(setOf("c1", "c2")))

        // Only one character -> Not a candidate
        assertFalse(viewModel.isDualDialogueCandidate(setOf("c1", "d1")))

        // Interrupted by an action block between the two characters
        val blocksWithAction = listOf(
            ScreenplayBlock(id = "c1", type = BlockType.CHARACTER, text = "ALICE"),
            ScreenplayBlock(id = "d1", type = BlockType.DIALOGUE, text = "Look at this reading!"),
            ScreenplayBlock(id = "a_mid", type = BlockType.ACTION, text = "She turns the knob."),
            ScreenplayBlock(id = "c2", type = BlockType.CHARACTER, text = "BOB"),
            ScreenplayBlock(id = "d2", type = BlockType.DIALOGUE, text = "It's impossible!")
        )
        viewModel.loadDocument(blocksWithAction, TitlePageData())
        assertFalse(viewModel.isDualDialogueCandidate(setOf("c1", "c2")))
    }

    @Test
    fun makeDualDialogueAndSeparateDualDialogue() {
        val blocks = listOf(
            ScreenplayBlock(id = "c1", type = BlockType.CHARACTER, text = "ROMEO"),
            ScreenplayBlock(id = "p1", type = BlockType.PARENTHETICAL, text = "(whispering)"),
            ScreenplayBlock(id = "d1", type = BlockType.DIALOGUE, text = "Lady, by yonder blessed moon I vow--"),
            ScreenplayBlock(id = "c2", type = BlockType.CHARACTER, text = "JULIET"),
            ScreenplayBlock(id = "d2", type = BlockType.DIALOGUE, text = "O, swear not by the moon, the inconstant moon!")
        )
        viewModel.loadDocument(blocks, TitlePageData())
        val selectedIds = setOf("c1", "p1", "d1", "c2", "d2")

        assertFalse(viewModel.isAlreadyDualDialogue(selectedIds))

        // Make Dual Dialogue
        viewModel.makeDualDialogue(selectedIds)
        val stateAfterDual = viewModel.uiState.value
        val updatedBlocks = stateAfterDual.blocks

        val romeo = updatedBlocks.first { it.id == "c1" }
        val romeoParen = updatedBlocks.first { it.id == "p1" }
        val romeoDiag = updatedBlocks.first { it.id == "d1" }
        val juliet = updatedBlocks.first { it.id == "c2" }
        val julietDiag = updatedBlocks.first { it.id == "d2" }

        assertNotNull(romeo.dualDialogueId)
        assertEquals(romeo.dualDialogueId, juliet.dualDialogueId)
        assertEquals(DualDialogueSide.LEFT, romeo.dualSide)
        assertEquals(DualDialogueSide.LEFT, romeoParen.dualSide)
        assertEquals(DualDialogueSide.LEFT, romeoDiag.dualSide)
        assertEquals(DualDialogueSide.RIGHT, juliet.dualSide)
        assertEquals(DualDialogueSide.RIGHT, julietDiag.dualSide)

        assertTrue(viewModel.isAlreadyDualDialogue(selectedIds))

        // Separate Dual Dialogue
        viewModel.separateDualDialogue(selectedIds)
        val stateAfterSeparate = viewModel.uiState.value
        stateAfterSeparate.blocks.forEach { block ->
            assertNull(block.dualDialogueId)
            assertNull(block.dualSide)
        }
        assertFalse(viewModel.isAlreadyDualDialogue(selectedIds))
    }

    @Test
    fun dualDialogueEnterKeyNavigation() {
        val dualId = "test-dual-id"
        val blocks = listOf(
            ScreenplayBlock(id = "c1", type = BlockType.CHARACTER, text = "JACK", dualDialogueId = dualId, dualSide = DualDialogueSide.LEFT),
            ScreenplayBlock(id = "d1", type = BlockType.DIALOGUE, text = "Hold on!", dualDialogueId = dualId, dualSide = DualDialogueSide.LEFT),
            ScreenplayBlock(id = "c2", type = BlockType.CHARACTER, text = "JILL", dualDialogueId = dualId, dualSide = DualDialogueSide.RIGHT),
            ScreenplayBlock(id = "d2", type = BlockType.DIALOGUE, text = "I'm slipping!", dualDialogueId = dualId, dualSide = DualDialogueSide.RIGHT)
        )
        viewModel.loadDocument(blocks, TitlePageData())

        // Press Enter on Left Dialogue (d1) -> Jumps to Right Character (c2)
        viewModel.onEnterPressed("d1")
        assertEquals("c2", viewModel.uiState.value.focusedBlockId)

        // Press Enter on Right Dialogue (d2) -> Creates normal ACTION block exiting dual dialogue
        viewModel.onEnterPressed("d2")
        val state = viewModel.uiState.value
        assertEquals(5, state.blocks.size)
        val exitBlock = state.blocks[4]
        assertEquals(BlockType.ACTION, exitBlock.type)
        assertNull(exitBlock.dualDialogueId)
        assertNull(exitBlock.dualSide)
        assertEquals(exitBlock.id, state.focusedBlockId)
    }

    @Test
    fun sceneNumberingToggleAndCalculation() {
        val blocks = listOf(
            ScreenplayBlock(id = "b0", type = BlockType.SCENE_HEADING, text = "FADE IN:"),
            ScreenplayBlock(id = "b1", type = BlockType.SCENE_HEADING, text = "INT. BANK - DAY"),
            ScreenplayBlock(id = "b2", type = BlockType.ACTION, text = "Gunshots ring out."),
            ScreenplayBlock(id = "b3", type = BlockType.SCENE_HEADING, text = "EXT. STREET - CONTINUOUS"),
            ScreenplayBlock(id = "b4", type = BlockType.ACTION, text = "The getaway car screeches away.")
        )
        viewModel.loadDocument(blocks, TitlePageData())

        // Default: scene numbers OFF (Spec script)
        assertFalse(viewModel.uiState.value.showSceneNumbers)

        // Block scene numbers should be auto-calculated for countable scene headings
        val sceneNums = viewModel.uiState.value.blockSceneNumbers
        assertNull(sceneNums["b0"]) // FADE IN: must not have a scene number
        assertEquals("1", sceneNums["b1"])
        assertEquals("2", sceneNums["b3"])

        // Toggle to Shooting Script mode
        viewModel.toggleSceneNumbers()
        assertTrue(viewModel.uiState.value.showSceneNumbers)

        // Set custom scene number
        viewModel.setSceneNumber("b3", "2A")
        val customSceneNums = viewModel.uiState.value.blockSceneNumbers
        assertEquals("2A", customSceneNums["b3"])

        // Toggle back to Spec script mode
        viewModel.toggleSceneNumbers()
        assertFalse(viewModel.uiState.value.showSceneNumbers)
    }

    @Test
    fun findAndReplaceFunctionality() {
        val blocks = listOf(
            ScreenplayBlock(id = "b1", type = BlockType.SCENE_HEADING, text = "INT. BANK VAULT - NIGHT"),
            ScreenplayBlock(id = "b2", type = BlockType.ACTION, text = "SARAH cuts through the steel vault door. Sarah smiles."),
            ScreenplayBlock(id = "b3", type = BlockType.CHARACTER, text = "SARAH"),
            ScreenplayBlock(id = "b4", type = BlockType.DIALOGUE, text = "The vault is open.")
        )
        viewModel.loadDocument(blocks, TitlePageData())

        viewModel.openFind(replaceMode = false)
        assertTrue(viewModel.uiState.value.isFindReplaceVisible)
        assertEquals(FindMode.FIND_REPLACE, viewModel.uiState.value.findMode)

        // Search for "vault" (case insensitive by default)
        viewModel.updateSearchQuery("vault")
        val matches = viewModel.uiState.value.searchMatches
        assertEquals(3, matches.size)
        assertEquals(0, viewModel.uiState.value.currentMatchIndex)

        // Navigate next and previous
        viewModel.findNext()
        assertEquals(1, viewModel.uiState.value.currentMatchIndex)
        viewModel.findNext()
        assertEquals(2, viewModel.uiState.value.currentMatchIndex)
        viewModel.findNext()
        assertEquals(0, viewModel.uiState.value.currentMatchIndex) // wraps around
        viewModel.findPrevious()
        assertEquals(2, viewModel.uiState.value.currentMatchIndex)

        // Test Match Case
        viewModel.toggleMatchCase()
        assertTrue(viewModel.uiState.value.matchCase)
        val caseMatches = viewModel.uiState.value.searchMatches
        // "VAULT" in b1, "vault" in b2, "vault" in b4 -> case-sensitive "vault" matches 2 times
        assertEquals(2, caseMatches.size)

        // Test Replace Current
        viewModel.updateReplaceQuery("chamber")
        viewModel.replaceCurrent()
        val afterReplace = viewModel.uiState.value
        assertTrue(afterReplace.blocks.any { it.text.contains("chamber") })

        // Test Replace All
        viewModel.updateSearchQuery("Sarah")
        viewModel.toggleMatchCase() // case-insensitive now
        assertFalse(viewModel.uiState.value.matchCase)
        viewModel.updateReplaceQuery("ELENA")
        val replaceCount = viewModel.replaceAll()
        assertTrue(replaceCount >= 2)
        assertFalse(viewModel.uiState.value.blocks.any { it.text.contains("SARAH") || it.text.contains("Sarah") })

        // Close Find
        viewModel.closeFind()
        assertFalse(viewModel.uiState.value.isFindReplaceVisible)
    }

    @Test
    fun renameCharacterPreservingExtensions() {
        val blocks = listOf(
            ScreenplayBlock(id = "b1", type = BlockType.SCENE_HEADING, text = "INT. BRIDGE - DAY"),
            ScreenplayBlock(id = "b2", type = BlockType.CHARACTER, text = "ADMIRAL (V.O.)"),
            ScreenplayBlock(id = "b3", type = BlockType.DIALOGUE, text = "We have incoming fighters."),
            ScreenplayBlock(id = "b4", type = BlockType.ACTION, text = "The crew looks at Admiral in disbelief."),
            ScreenplayBlock(id = "b5", type = BlockType.CHARACTER, text = "ADMIRAL (CONT'D)"),
            ScreenplayBlock(id = "b6", type = BlockType.DIALOGUE, text = "Raise the shields!")
        )
        viewModel.loadDocument(blocks, TitlePageData())

        // 1. Rename Character Cues Only
        val countCues = viewModel.renameCharacter(
            oldName = "ADMIRAL",
            newName = "COMMANDER",
            cuesOnly = true
        )
        assertEquals(2, countCues)
        val stateAfterCues = viewModel.uiState.value
        assertEquals("COMMANDER (V.O.)", stateAfterCues.blocks[1].text)
        assertEquals("COMMANDER (CONT'D)", stateAfterCues.blocks[4].text)
        // Action line still contains "Admiral"
        assertTrue(stateAfterCues.blocks[3].text.contains("Admiral"))

        // 2. Rename Everywhere (cues, dialogue & action)
        val countEverywhere = viewModel.renameCharacter(
            oldName = "COMMANDER",
            newName = "GENERAL",
            cuesOnly = false
        )
        assertTrue(countEverywhere >= 2)
        val stateAfterEverywhere = viewModel.uiState.value
        assertEquals("GENERAL (V.O.)", stateAfterEverywhere.blocks[1].text)
        assertEquals("GENERAL (CONT'D)", stateAfterEverywhere.blocks[4].text)
    }

    @Test
    fun writersNotesLifecycleAndCount() {
        val blocks = listOf(
            ScreenplayBlock(id = "b1", type = BlockType.SCENE_HEADING, text = "INT. WRITER'S ROOM - NIGHT"),
            ScreenplayBlock(id = "b2", type = BlockType.ACTION, text = "Crumpled script pages litter the floor.")
        )
        viewModel.loadDocument(blocks, TitlePageData())

        assertEquals(0, viewModel.uiState.value.notesCount)
        assertTrue(viewModel.uiState.value.showWriterNotes)

        // 1. Add note to block 1
        viewModel.setBlockNote("b1", "Remember to add thunder FX here")
        val stateWithOneNote = viewModel.uiState.value
        assertEquals(1, stateWithOneNote.notesCount)
        assertEquals("Remember to add thunder FX here", stateWithOneNote.blocks[0].note)

        // 2. Add note to block 2
        viewModel.setBlockNote("b2", "Needs more sensory details")
        val stateWithTwoNotes = viewModel.uiState.value
        assertEquals(2, stateWithTwoNotes.notesCount)
        assertEquals("Needs more sensory details", stateWithTwoNotes.blocks[1].note)

        // 3. Toggle writer notes visibility
        viewModel.toggleWriterNotes()
        assertFalse(viewModel.uiState.value.showWriterNotes)
        viewModel.toggleWriterNotes()
        assertTrue(viewModel.uiState.value.showWriterNotes)

        // 4. Delete note from block 1
        viewModel.deleteBlockNote("b1")
        val stateAfterDelete = viewModel.uiState.value
        assertEquals(1, stateAfterDelete.notesCount)
        assertNull(stateAfterDelete.blocks[0].note)
        assertEquals("Needs more sensory details", stateAfterDelete.blocks[1].note)
    }

    @Test
    fun reorderScenesMovesAllChildBlocksAndPreservesLeadBlocks() {
        val blocks = listOf(
            ScreenplayBlock(id = "lead1", type = BlockType.TRANSITION, text = "FADE IN:"),
            // Scene 1
            ScreenplayBlock(id = "h1", type = BlockType.SCENE_HEADING, text = "INT. COFFEE SHOP - DAY"),
            ScreenplayBlock(id = "a1", type = BlockType.ACTION, text = "Alice sits at the counter."),
            ScreenplayBlock(id = "c1", type = BlockType.CHARACTER, text = "ALICE"),
            ScreenplayBlock(id = "d1", type = BlockType.DIALOGUE, text = "One coffee please.", note = "Delivered softly"),
            // Scene 2
            ScreenplayBlock(id = "h2", type = BlockType.SCENE_HEADING, text = "EXT. RAINY STREET - NIGHT"),
            ScreenplayBlock(id = "a2", type = BlockType.ACTION, text = "Rain falls steadily."),
            // Scene 3
            ScreenplayBlock(id = "h3", type = BlockType.SCENE_HEADING, text = "INT. APARTMENT - NIGHT"),
            ScreenplayBlock(id = "a3", type = BlockType.ACTION, text = "Bob enters with grocery bags.")
        )
        viewModel.loadDocument(blocks, TitlePageData())

        val initial = viewModel.uiState.value
        assertEquals(3, initial.scenes.size)
        assertEquals("INT. COFFEE SHOP - DAY", initial.scenes[0].heading)
        assertEquals("EXT. RAINY STREET - NIGHT", initial.scenes[1].heading)
        assertEquals("INT. APARTMENT - NIGHT", initial.scenes[2].heading)
        assertEquals("h1", initial.scenes[0].blockId)
        assertEquals("h2", initial.scenes[1].blockId)
        assertEquals("h3", initial.scenes[2].blockId)

        // 1. Reorder mode toggling
        assertFalse(viewModel.uiState.value.isReorderMode)
        viewModel.toggleReorderMode()
        assertTrue(viewModel.uiState.value.isReorderMode)
        viewModel.setReorderMode(false)
        assertFalse(viewModel.uiState.value.isReorderMode)

        // 2. Move Scene 2 up (EXT. RAINY STREET - NIGHT becomes first scene)
        viewModel.moveSceneUp(2)
        val afterUp = viewModel.uiState.value
        assertEquals(3, afterUp.scenes.size)
        assertEquals("EXT. RAINY STREET - NIGHT", afterUp.scenes[0].heading)
        assertEquals("INT. COFFEE SHOP - DAY", afterUp.scenes[1].heading)
        assertEquals("INT. APARTMENT - NIGHT", afterUp.scenes[2].heading)
        assertEquals("h2", afterUp.focusedBlockId)
        assertEquals(1, afterUp.selectedSceneIndex)

        // Verify lead block FADE IN: is still block 0
        assertEquals("lead1", afterUp.blocks[0].id)
        assertEquals("FADE IN:", afterUp.blocks[0].text)

        // Verify Scene 2 blocks are at index 1 and 2
        assertEquals("h2", afterUp.blocks[1].id)
        assertEquals("a2", afterUp.blocks[2].id)

        // Verify Scene 1 and its child blocks are at indices 3..6 with note intact
        assertEquals("h1", afterUp.blocks[3].id)
        assertEquals("a1", afterUp.blocks[4].id)
        assertEquals("c1", afterUp.blocks[5].id)
        assertEquals("d1", afterUp.blocks[6].id)
        assertEquals("Delivered softly", afterUp.blocks[6].note)

        // Verify Scene 3 blocks at indices 7..8
        assertEquals("h3", afterUp.blocks[7].id)
        assertEquals("a3", afterUp.blocks[8].id)

        // 3. Move Scene 1 down (EXT. RAINY STREET - NIGHT moves back to position 2)
        viewModel.moveSceneDown(1)
        val afterDown = viewModel.uiState.value
        assertEquals("INT. COFFEE SHOP - DAY", afterDown.scenes[0].heading)
        assertEquals("EXT. RAINY STREET - NIGHT", afterDown.scenes[1].heading)
        assertEquals("INT. APARTMENT - NIGHT", afterDown.scenes[2].heading)
        assertEquals("h2", afterDown.focusedBlockId)
        assertEquals(2, afterDown.selectedSceneIndex)

        // 4. Boundary checks: cannot move Scene 1 up or last scene down
        viewModel.moveSceneUp(1)
        assertEquals("INT. COFFEE SHOP - DAY", viewModel.uiState.value.scenes[0].heading)
        viewModel.moveSceneDown(3)
        assertEquals("INT. APARTMENT - NIGHT", viewModel.uiState.value.scenes[2].heading)

        // 5. Select Scene uses blockId
        viewModel.selectScene(afterDown.scenes[0])
        assertEquals("h1", viewModel.uiState.value.focusedBlockId)
        assertEquals(1, viewModel.uiState.value.selectedSceneIndex)

        // 6. Direct moveScene
        viewModel.moveScene(0, 2)
        val afterDirect = viewModel.uiState.value
        assertEquals("EXT. RAINY STREET - NIGHT", afterDirect.scenes[0].heading)
        assertEquals("INT. APARTMENT - NIGHT", afterDirect.scenes[1].heading)
        assertEquals("INT. COFFEE SHOP - DAY", afterDirect.scenes[2].heading)
        assertEquals("h1", afterDirect.focusedBlockId)
        assertEquals(3, afterDirect.selectedSceneIndex)
    }

    @Test
    fun draftDiffAndScriptLibraryDialogStatesToggleCorrectly() {
        assertFalse(viewModel.uiState.value.isDraftDiffOpen)
        assertFalse(viewModel.uiState.value.isLibraryOpen)

        viewModel.openDraftDiff()
        assertTrue(viewModel.uiState.value.isDraftDiffOpen)

        viewModel.closeDraftDiff()
        assertFalse(viewModel.uiState.value.isDraftDiffOpen)

        viewModel.openScriptLibrary()
        assertTrue(viewModel.uiState.value.isLibraryOpen)

        viewModel.closeScriptLibrary()
        assertFalse(viewModel.uiState.value.isLibraryOpen)
    }

    @Test
    fun getProductionBreakdownExtractsElementsFromActiveScript() {
        val b1 = ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. BANK VAULT - NIGHT", sceneNumber = "1")
        val b2 = ScreenplayBlock(type = BlockType.ACTION, text = "ALEX wears black leather gloves and pulls a silenced pistol from his backpack.")
        val b3 = ScreenplayBlock(type = BlockType.CHARACTER, text = "ALEX")
        val b4 = ScreenplayBlock(type = BlockType.DIALOGUE, text = "Grab the diamonds and let's go. A siren wails outside.")

        viewModel.loadDocument(listOf(b1, b2, b3, b4), TitlePageData(title = "HEIST"))

        val breakdown = viewModel.getProductionBreakdown()
        assertEquals(1, breakdown.totalScenes)
        val sc = breakdown.scenes[0]
        assertEquals("INT. BANK VAULT - NIGHT", sc.heading)
        assertEquals("INT.", sc.intExt)
        assertEquals("BANK VAULT", sc.location)
        assertEquals("NIGHT", sc.timeOfDay)
        assertTrue(sc.castMembers.contains("ALEX"))
        assertTrue(sc.props.any { it.contains("pistol", ignoreCase = true) || it.contains("backpack", ignoreCase = true) })
        assertTrue(sc.wardrobe.any { it.contains("gloves", ignoreCase = true) })
        assertTrue(sc.soundNotes.any { it.contains("siren", ignoreCase = true) })
    }
}

