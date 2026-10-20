package com.seshat.writer.diff

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DraftDiffEngineTest {

    private lateinit var draftABlocks: List<ScreenplayBlock>
    private lateinit var draftBBlocks: List<ScreenplayBlock>
    private lateinit var titlePageA: TitlePageData
    private lateinit var titlePageB: TitlePageData

    @Before
    fun setUp() {
        titlePageA = TitlePageData(title = "NIGHTFALL", author = "Alex Vance", draftInfo = "White Draft")
        titlePageB = TitlePageData(title = "NIGHTFALL", author = "Alex Vance", draftInfo = "Blue Revision")

        // Draft A: 2 scenes
        draftABlocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. WAREHOUSE - NIGHT", sceneNumber = "1"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Shadows stretch across the cracked concrete floor."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "MARCUS"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "We only have ten minutes before they arrive."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. ALLEYWAY - CONTINUOUS", sceneNumber = "2"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Rain pours down from rusted gutters.")
        )

        // Draft B: Scene 1 modified, Scene 2 kept, Scene 3 added
        draftBBlocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. WAREHOUSE - NIGHT", sceneNumber = "1"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Shadows stretch across the cracked concrete floor. Sirens wail in the distance."), // modified action
            ScreenplayBlock(type = BlockType.CHARACTER, text = "MARCUS"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "We only have five minutes before they arrive."), // modified dialogue
            ScreenplayBlock(type = BlockType.CHARACTER, text = "ELENA"), // added character
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Then we better move fast."), // added dialogue
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. ALLEYWAY - CONTINUOUS", sceneNumber = "2"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Rain pours down from rusted gutters."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. ROOFTOP - NIGHT", sceneNumber = "3"), // added scene
            ScreenplayBlock(type = BlockType.ACTION, text = "Lightning flashes across the city skyline.")
        )
    }

    @Test
    fun computeDiffIdenticalDraftsReturnsNoDifferences() {
        val result = DraftDiffEngine.compareDrafts(
            blocksA = draftABlocks,
            titlePageA = titlePageA,
            blocksB = draftABlocks,
            titlePageB = titlePageA
        )

        assertFalse(result.summary.hasDifferences)
        assertEquals(0, result.summary.scenesAdded)
        assertEquals(0, result.summary.scenesRemoved)
        assertEquals(0, result.summary.scenesModified)
        assertEquals(2, result.summary.scenesUnchanged)
    }

    @Test
    fun computeDiffDetectsAddedAndModifiedScenes() {
        val result = DraftDiffEngine.compareDrafts(
            blocksA = draftABlocks,
            titlePageA = titlePageA,
            blocksB = draftBBlocks,
            titlePageB = titlePageB
        )

        assertTrue(result.summary.hasDifferences)
        assertEquals(1, result.summary.scenesAdded)      // EXT. ROOFTOP added
        assertEquals(0, result.summary.scenesRemoved)
        assertEquals(1, result.summary.scenesModified)   // INT. WAREHOUSE modified
        assertEquals(1, result.summary.scenesUnchanged)  // EXT. ALLEYWAY unchanged

        // Total scenes in B is 3
        assertEquals(3, result.sceneDiffs.size)

        val warehouseDiff = result.sceneDiffs.find { it.headingB?.contains("WAREHOUSE") == true }
        assertNotNull(warehouseDiff)
        assertEquals(DiffType.MODIFIED, warehouseDiff!!.type)
        assertTrue(warehouseDiff.stats.modifiedBlocks > 0)
        assertTrue(warehouseDiff.stats.addedBlocks > 0)

        val rooftopDiff = result.sceneDiffs.find { it.headingB?.contains("ROOFTOP") == true }
        assertNotNull(rooftopDiff)
        assertEquals(DiffType.ADDED, rooftopDiff!!.type)
    }

    @Test
    fun computeDiffDetectsRemovedScenes() {
        val draftCRemoved = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. WAREHOUSE - NIGHT", sceneNumber = "1"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Shadows stretch across the cracked concrete floor.")
        )

        val result = DraftDiffEngine.compareDrafts(
            blocksA = draftABlocks,
            titlePageA = titlePageA,
            blocksB = draftCRemoved,
            titlePageB = titlePageB
        )

        assertTrue(result.summary.hasDifferences)
        assertEquals(1, result.summary.scenesRemoved) // EXT. ALLEYWAY removed
        assertEquals(1, result.summary.scenesModified) // INT. WAREHOUSE has removed dialogue
    }

    @Test
    fun generateChangeReportTextContainsSummaryAndSceneDetails() {
        val result = DraftDiffEngine.compareDrafts(
            blocksA = draftABlocks,
            titlePageA = titlePageA,
            blocksB = draftBBlocks,
            titlePageB = titlePageB
        )

        val reportText = DraftDiffEngine.generateChangeReportText(result)
        assertNotNull(reportText)
        assertTrue(reportText.contains("DRAFT REVISION COMPARISON REPORT"))
        assertTrue(reportText.contains("NIGHTFALL"))
        assertTrue(reportText.contains("ADDED SCENES"))
        assertTrue(reportText.contains("MODIFIED SCENES"))
        assertTrue(reportText.contains("EXT. ROOFTOP - NIGHT"))
        assertTrue(reportText.contains("MARCUS"))
    }
}
