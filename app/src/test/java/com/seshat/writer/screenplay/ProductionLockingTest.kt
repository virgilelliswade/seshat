package com.seshat.writer.screenplay

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductionLockingTest {

    private lateinit var viewModel: ScreenplayViewModel

    @Before
    fun setUp() {
        viewModel = ScreenplayViewModel()
    }

    @Test
    fun testLockSceneNumbersAssignsNumbersAndEnablesDisplay() {
        val b1 = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(b1, "INT. DINER - DAY")
        viewModel.onEnterPressed(b1)
        val b2 = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(b2, "EXT. ALLEY - NIGHT")
        viewModel.changeBlockType(b2, BlockType.SCENE_HEADING)

        assertEquals(2, viewModel.uiState.value.scenes.size)
        assertFalse(viewModel.uiState.value.isSceneNumberingLocked)

        viewModel.lockSceneNumbers()

        val lockedState = viewModel.uiState.value
        assertTrue(lockedState.isSceneNumberingLocked)
        assertTrue(lockedState.showSceneNumbers)
        assertEquals(2, lockedState.lockedSceneNumbers.size)
        assertEquals("1", lockedState.scenes[0].sceneNumber)
        assertEquals("2", lockedState.scenes[1].sceneNumber)
    }

    @Test
    fun testInsertSceneWhileLockedAssignsHollywoodLettering() {
        // Create 2 scenes: 1 and 2
        val b1 = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(b1, "INT. OFFICE - DAY")
        viewModel.onEnterPressed(b1)
        val b2 = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(b2, "EXT. STREET - NIGHT")
        viewModel.changeBlockType(b2, BlockType.SCENE_HEADING)

        // Lock scene numbers -> Scene 1 and Scene 2
        viewModel.lockSceneNumbers()
        assertEquals("1", viewModel.uiState.value.scenes[0].sceneNumber)
        assertEquals("2", viewModel.uiState.value.scenes[1].sceneNumber)

        // Insert new scene between scene 1 and scene 2
        // Press Enter after scene 1 -> creates action block, then change type to SCENE_HEADING
        viewModel.onEnterPressed(b1)
        val newBlockId = viewModel.uiState.value.blocks[1].id
        viewModel.changeBlockType(newBlockId, BlockType.SCENE_HEADING)
        viewModel.updateBlockText(newBlockId, "INT. HALLWAY - CONTINUOUS")

        val stateAfterInsert = viewModel.uiState.value
        assertEquals(3, stateAfterInsert.scenes.size)
        // Scene 1 remains "1"
        assertEquals("1", stateAfterInsert.scenes[0].sceneNumber)
        // Newly inserted scene between 1 and 2 receives "1A"
        assertEquals("1A", stateAfterInsert.scenes[1].sceneNumber)
        // Original Scene 2 remains "2" (call sheets preserved!)
        assertEquals("2", stateAfterInsert.scenes[2].sceneNumber)
    }

    @Test
    fun testInsertMultipleScenesWhileLockedAssignsConsecutiveLetters() {
        val b1 = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(b1, "INT. LIVING ROOM - DAY")

        viewModel.lockSceneNumbers()
        assertEquals("1", viewModel.uiState.value.scenes[0].sceneNumber)

        // Insert first scene after scene 1 -> "1A"
        viewModel.onEnterPressed(b1)
        val b1A = viewModel.uiState.value.blocks[1].id
        viewModel.changeBlockType(b1A, BlockType.SCENE_HEADING)
        viewModel.updateBlockText(b1A, "INT. KITCHEN - DAY")

        assertEquals("1A", viewModel.uiState.value.scenes[1].sceneNumber)

        // Insert second scene after 1A -> "1B"
        viewModel.onEnterPressed(b1A)
        val b1B = viewModel.uiState.value.blocks[2].id
        viewModel.changeBlockType(b1B, BlockType.SCENE_HEADING)
        viewModel.updateBlockText(b1B, "INT. BASEMENT - NIGHT")

        val state = viewModel.uiState.value
        assertEquals(3, state.scenes.size)
        assertEquals("1", state.scenes[0].sceneNumber)
        assertEquals("1A", state.scenes[1].sceneNumber)
        assertEquals("1B", state.scenes[2].sceneNumber)
    }

    @Test
    fun testRenumberAllScenesRestoresSequentialNumbers() {
        val b1 = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(b1, "INT. SCENE ONE - DAY")
        viewModel.lockSceneNumbers()

        viewModel.onEnterPressed(b1)
        val b1A = viewModel.uiState.value.blocks[1].id
        viewModel.changeBlockType(b1A, BlockType.SCENE_HEADING)
        viewModel.updateBlockText(b1A, "INT. SCENE ONE A - DAY")

        assertEquals("1A", viewModel.uiState.value.scenes[1].sceneNumber)

        // Renumber all scenes: resets back to 1, 2
        viewModel.renumberAllScenes()

        val state = viewModel.uiState.value
        assertEquals("1", state.scenes[0].sceneNumber)
        assertEquals("2", state.scenes[1].sceneNumber)
    }

    @Test
    fun testOmitSceneMarksSceneHeadingAndChildBlocks() {
        val h1 = viewModel.uiState.value.blocks[0].id
        viewModel.updateBlockText(h1, "INT. WAREHOUSE - NIGHT")

        // Add action and dialogue to scene 1
        viewModel.onEnterPressed(h1)
        val a1 = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(a1, "Footsteps echo in the dark.")

        viewModel.onEnterPressed(a1)
        val c1 = viewModel.uiState.value.blocks[2].id
        viewModel.changeBlockType(c1, BlockType.CHARACTER)
        viewModel.updateBlockText(c1, "GUARD")

        viewModel.onEnterPressed(c1)
        val d1 = viewModel.uiState.value.blocks[3].id
        viewModel.updateBlockText(d1, "Who is there?")

        // Add a second scene
        viewModel.onEnterPressed(d1)
        val h2 = viewModel.uiState.value.blocks[4].id
        viewModel.changeBlockType(h2, BlockType.SCENE_HEADING)
        viewModel.updateBlockText(h2, "EXT. ROOF - NIGHT")

        assertEquals(2, viewModel.uiState.value.scenes.size)
        assertFalse(viewModel.uiState.value.blocks[0].isOmitted)
        assertFalse(viewModel.uiState.value.blocks[1].isOmitted)

        // Omit Scene 1
        viewModel.omitScene(h1)

        val stateOmitted = viewModel.uiState.value
        // Heading is omitted
        assertTrue(stateOmitted.blocks[0].isOmitted)
        // All child blocks of scene 1 are omitted
        assertTrue(stateOmitted.blocks[1].isOmitted)
        assertTrue(stateOmitted.blocks[2].isOmitted)
        assertTrue(stateOmitted.blocks[3].isOmitted)
        // Scene 2 is NOT omitted
        assertFalse(stateOmitted.blocks[4].isOmitted)

        // SceneItem in outline shows OMITTED heading and isOmitted = true
        val scene1Item = stateOmitted.scenes[0]
        assertTrue(scene1Item.isOmitted)
        assertEquals("OMITTED", scene1Item.heading)

        // Restore Scene 1
        viewModel.restoreScene(h1)
        val stateRestored = viewModel.uiState.value
        assertFalse(stateRestored.blocks[0].isOmitted)
        assertFalse(stateRestored.blocks[1].isOmitted)
        assertFalse(stateRestored.blocks[2].isOmitted)
        assertFalse(stateRestored.blocks[3].isOmitted)
        assertFalse(stateRestored.scenes[0].isOmitted)
        assertEquals("INT. WAREHOUSE - NIGHT", stateRestored.scenes[0].heading)
    }
}
