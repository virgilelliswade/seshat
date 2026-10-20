package com.seshat.writer.screenplay

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.RevisionColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductionRevisionTest {

    private lateinit var viewModel: ScreenplayViewModel

    @Before
    fun setUp() {
        viewModel = ScreenplayViewModel()
    }

    @Test
    fun testInitialRevisionState() {
        val state = viewModel.uiState.value
        assertFalse(state.isRevisionTrackingEnabled)
        assertEquals(RevisionColor.WHITE, state.currentRevisionColor)
        assertEquals(0, state.revisedBlocksCount)
        assertFalse(state.showColoredDraftTint)
        assertFalse(state.isRevisionDialogOpen)
        assertFalse(state.isReportsDialogOpen)
    }

    @Test
    fun testRevisionTrackingStampsEditedBlocks() {
        val blockId = viewModel.uiState.value.blocks[0].id

        // Edit with tracking disabled: block is not revised
        viewModel.updateBlockText(blockId, "INT. APARTMENT - DAY")
        assertFalse(viewModel.uiState.value.blocks[0].isRevised)
        assertNull(viewModel.uiState.value.blocks[0].revisionColor)

        // Enable tracking and set draft to BLUE (1st Revision)
        viewModel.setRevisionTrackingEnabled(true)
        viewModel.setCurrentRevisionColor(RevisionColor.BLUE)

        // Edit text: block should be stamped as revised in BLUE
        viewModel.updateBlockText(blockId, "INT. LUXURY APARTMENT - DAY")
        val updated = viewModel.uiState.value.blocks[0]
        assertTrue(updated.isRevised)
        assertEquals(RevisionColor.BLUE.name, updated.revisionColor)
        assertEquals(1, viewModel.uiState.value.revisedBlocksCount)
    }

    @Test
    fun testToggleBlockRevised() {
        val blockId = viewModel.uiState.value.blocks[0].id
        viewModel.setCurrentRevisionColor(RevisionColor.PINK)

        // Toggle on
        viewModel.toggleBlockRevised(blockId)
        assertTrue(viewModel.uiState.value.blocks[0].isRevised)
        assertEquals(RevisionColor.PINK.name, viewModel.uiState.value.blocks[0].revisionColor)
        assertEquals(1, viewModel.uiState.value.revisedBlocksCount)

        // Toggle off
        viewModel.toggleBlockRevised(blockId)
        assertFalse(viewModel.uiState.value.blocks[0].isRevised)
        assertNull(viewModel.uiState.value.blocks[0].revisionColor)
        assertEquals(0, viewModel.uiState.value.revisedBlocksCount)
    }

    @Test
    fun testMarkAllBlocksRevisedAndClear() {
        val headingId = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(headingId, "INT. OFFICE - DAY")
        viewModel.onEnterPressed(headingId)
        assertEquals(2, viewModel.uiState.value.blocks.size)

        // Mark all revised in YELLOW
        viewModel.markAllBlocksRevised(isRevised = true, color = RevisionColor.YELLOW)
        val stateYellow = viewModel.uiState.value
        assertEquals(2, stateYellow.revisedBlocksCount)
        assertTrue(stateYellow.blocks[0].isRevised)
        assertEquals(RevisionColor.YELLOW.name, stateYellow.blocks[0].revisionColor)
        assertTrue(stateYellow.blocks[1].isRevised)
        assertEquals(RevisionColor.YELLOW.name, stateYellow.blocks[1].revisionColor)

        // Clear all revised
        viewModel.markAllBlocksRevised(isRevised = false)
        val stateCleared = viewModel.uiState.value
        assertEquals(0, stateCleared.revisedBlocksCount)
        assertFalse(stateCleared.blocks[0].isRevised)
        assertNull(stateCleared.blocks[0].revisionColor)
        assertFalse(stateCleared.blocks[1].isRevised)
        assertNull(stateCleared.blocks[1].revisionColor)
    }

    @Test
    fun testRevisionColorPaletteTagsAndProperties() {
        assertEquals("White (First Draft)", RevisionColor.WHITE.displayName)
        assertEquals("WHITE DRAFT", RevisionColor.WHITE.draftTag)

        assertEquals("Blue (1st Revision)", RevisionColor.BLUE.displayName)
        assertEquals("BLUE REV.", RevisionColor.BLUE.draftTag)

        assertEquals("Pink (2nd Revision)", RevisionColor.PINK.displayName)
        assertEquals("PINK REV.", RevisionColor.PINK.draftTag)

        assertEquals("Yellow (3rd Revision)", RevisionColor.YELLOW.displayName)
        assertEquals("YELLOW REV.", RevisionColor.YELLOW.draftTag)

        assertEquals("Green (4th Revision)", RevisionColor.GREEN.displayName)
        assertEquals("GREEN REV.", RevisionColor.GREEN.draftTag)

        assertEquals("Goldenrod (5th Revision)", RevisionColor.GOLDENROD.displayName)
        assertEquals("GOLDENROD REV.", RevisionColor.GOLDENROD.draftTag)

        assertEquals("Buff (6th Revision)", RevisionColor.BUFF.displayName)
        assertEquals("BUFF REV.", RevisionColor.BUFF.draftTag)

        assertEquals("Salmon (7th Revision)", RevisionColor.SALMON.displayName)
        assertEquals("SALMON REV.", RevisionColor.SALMON.draftTag)

        assertEquals("Cherry (8th Revision)", RevisionColor.CHERRY.displayName)
        assertEquals("CHERRY REV.", RevisionColor.CHERRY.draftTag)

        assertEquals("2nd Blue (9th Revision)", RevisionColor.SECOND_BLUE.displayName)
        assertEquals("2ND BLUE REV.", RevisionColor.SECOND_BLUE.draftTag)
    }

    @Test
    fun testRevisionDateAndTintToggles() {
        viewModel.setRevisionDate("OCTOBER 10, 2026")
        assertEquals("OCTOBER 10, 2026", viewModel.uiState.value.revisionDate)

        viewModel.setColoredDraftTint(true)
        assertTrue(viewModel.uiState.value.showColoredDraftTint)

        viewModel.toggleColoredDraftTint()
        assertFalse(viewModel.uiState.value.showColoredDraftTint)
    }

    @Test
    fun testDialogStateTransitions() {
        viewModel.openRevisionDialog()
        assertTrue(viewModel.uiState.value.isRevisionDialogOpen)
        viewModel.closeRevisionDialog()
        assertFalse(viewModel.uiState.value.isRevisionDialogOpen)

        viewModel.openReportsDialog()
        assertTrue(viewModel.uiState.value.isReportsDialogOpen)
        viewModel.closeReportsDialog()
        assertFalse(viewModel.uiState.value.isReportsDialogOpen)
    }
}
