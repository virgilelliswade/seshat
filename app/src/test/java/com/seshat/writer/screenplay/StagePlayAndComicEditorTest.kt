package com.seshat.writer.screenplay

import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StagePlayAndComicEditorTest {

    private lateinit var viewModel: ScreenplayViewModel

    @Before
    fun setUp() {
        viewModel = ScreenplayViewModel()
    }

    // ==========================================
    // 🎭 STAGE PLAY TESTS
    // ==========================================

    @Test
    fun stagePlayFormatHelperReturnsDesignatedElements() {
        val types = BlockType.forFormat(ProjectFormat.STAGE_PLAY)
        assertEquals(
            listOf(
                BlockType.STAGE_ACT_SCENE,
                BlockType.STAGE_DIRECTION,
                BlockType.CHARACTER,
                BlockType.PARENTHETICAL,
                BlockType.DIALOGUE,
                BlockType.STAGE_CURTAIN
            ),
            types
        )
    }

    @Test
    fun stagePlayEnterFromActSceneCreatesStageDirection() {
        viewModel.setProjectFormatForTest(ProjectFormat.STAGE_PLAY)
        val actBlock = ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1")
        viewModel.loadDocument(listOf(actBlock))

        viewModel.onEnterPressed(actBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.STAGE_ACT_SCENE, state.blocks[0].type)
        assertEquals(BlockType.STAGE_DIRECTION, state.blocks[1].type)
        assertEquals(state.blocks[1].id, state.focusedBlockId)
    }

    @Test
    fun stagePlayEnterFromCharacterCreatesDialogue() {
        viewModel.setProjectFormatForTest(ProjectFormat.STAGE_PLAY)
        val charBlock = ScreenplayBlock(type = BlockType.CHARACTER, text = "HAMLET")
        viewModel.loadDocument(listOf(charBlock))

        viewModel.onEnterPressed(charBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.CHARACTER, state.blocks[0].type)
        assertEquals(BlockType.DIALOGUE, state.blocks[1].type)
        assertEquals(state.blocks[1].id, state.focusedBlockId)
    }

    @Test
    fun stagePlayEnterFromDialogueImmediatelyCreatesNextCharacter() {
        viewModel.setProjectFormatForTest(ProjectFormat.STAGE_PLAY)
        val dialBlock = ScreenplayBlock(type = BlockType.DIALOGUE, text = "To be, or not to be, that is the question.")
        viewModel.loadDocument(listOf(dialBlock))

        viewModel.onEnterPressed(dialBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.DIALOGUE, state.blocks[0].type)
        // Stage plays automatically cue the next speaker line!
        assertEquals(BlockType.CHARACTER, state.blocks[1].type)
        assertEquals(state.blocks[1].id, state.focusedBlockId)
    }

    @Test
    fun stagePlayEnterFromEmptyDialogueConvertsToStageDirection() {
        viewModel.setProjectFormatForTest(ProjectFormat.STAGE_PLAY)
        val dialBlock = ScreenplayBlock(type = BlockType.DIALOGUE, text = "")
        viewModel.loadDocument(listOf(dialBlock))

        viewModel.onEnterPressed(dialBlock.id)

        val state = viewModel.uiState.value
        assertEquals(1, state.blocks.size)
        // Empty dialogue line promotes to Stage Direction
        assertEquals(BlockType.STAGE_DIRECTION, state.blocks[0].type)
    }

    @Test
    fun stagePlayTabFromCharacterInsertsParenthetical() {
        viewModel.setProjectFormatForTest(ProjectFormat.STAGE_PLAY)
        val charBlock = ScreenplayBlock(type = BlockType.CHARACTER, text = "OPHELIA")
        viewModel.loadDocument(listOf(charBlock))

        viewModel.onTabPressed(charBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.CHARACTER, state.blocks[0].type)
        assertEquals(BlockType.PARENTHETICAL, state.blocks[1].type)
    }

    @Test
    fun stagePlayTabFromDialogueInsertsParenthetical() {
        viewModel.setProjectFormatForTest(ProjectFormat.STAGE_PLAY)
        val dialBlock = ScreenplayBlock(type = BlockType.DIALOGUE, text = "My lord, as I was sewing in my closet,")
        viewModel.loadDocument(listOf(dialBlock))

        viewModel.onTabPressed(dialBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.PARENTHETICAL, state.blocks[1].type)
    }

    // ==========================================
    // 💥 COMIC BOOK TESTS
    // ==========================================

    @Test
    fun comicBookFormatHelperReturnsDesignatedElements() {
        val types = BlockType.forFormat(ProjectFormat.COMIC_BOOK)
        assertEquals(
            listOf(
                BlockType.COMIC_PAGE,
                BlockType.COMIC_PANEL,
                BlockType.COMIC_ART_NOTE,
                BlockType.CHARACTER,
                BlockType.COMIC_BALLOON,
                BlockType.COMIC_CAPTION,
                BlockType.COMIC_SFX
            ),
            types
        )
    }

    @Test
    fun comicBookEnterFromPageCreatesPanel1() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val pageBlock = ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1")
        viewModel.loadDocument(listOf(pageBlock))

        viewModel.onEnterPressed(pageBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.COMIC_PAGE, state.blocks[0].type)
        assertEquals(BlockType.COMIC_PANEL, state.blocks[1].type)
        assertEquals("PANEL 1", state.blocks[1].text)
        assertEquals(state.blocks[1].id, state.focusedBlockId)
    }

    @Test
    fun comicBookEnterFromPanelCreatesArtNote() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val panelBlock = ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 (WIDE)")
        viewModel.loadDocument(listOf(panelBlock))

        viewModel.onEnterPressed(panelBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.COMIC_PANEL, state.blocks[0].type)
        assertEquals(BlockType.COMIC_ART_NOTE, state.blocks[1].type)
        assertEquals(state.blocks[1].id, state.focusedBlockId)
    }

    @Test
    fun comicBookEnterFromArtNoteCreatesNextArtNoteWhenNonEmpty() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val artBlock = ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "Night falls over the city.")
        viewModel.loadDocument(listOf(artBlock))

        viewModel.onEnterPressed(artBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.COMIC_ART_NOTE, state.blocks[1].type)
    }

    @Test
    fun comicBookEnterFromEmptyArtNoteAdvancesToNextPanel() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val panelBlock = ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1")
        val artBlock = ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "")
        viewModel.loadDocument(listOf(panelBlock, artBlock))

        viewModel.onEnterPressed(artBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        // Empty art direction line promotes into the next sequential panel!
        assertEquals(BlockType.COMIC_PANEL, state.blocks[1].type)
        assertEquals("PANEL 2", state.blocks[1].text)
    }

    @Test
    fun comicBookEnterFromCharacterCreatesBalloon() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val charBlock = ScreenplayBlock(type = BlockType.CHARACTER, text = "SPIDER-MAN")
        viewModel.loadDocument(listOf(charBlock))

        viewModel.onEnterPressed(charBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.CHARACTER, state.blocks[0].type)
        assertEquals(BlockType.COMIC_BALLOON, state.blocks[1].type)
    }

    @Test
    fun comicBookEnterFromBalloonCreatesNextCharacter() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val balloonBlock = ScreenplayBlock(type = BlockType.COMIC_BALLOON, text = "Heads up!")
        viewModel.loadDocument(listOf(balloonBlock))

        viewModel.onEnterPressed(balloonBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.CHARACTER, state.blocks[1].type)
    }

    @Test
    fun comicBookEnterFromEmptyBalloonAdvancesToNextPanel() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val panelBlock = ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 3")
        val balloonBlock = ScreenplayBlock(type = BlockType.COMIC_BALLOON, text = "")
        viewModel.loadDocument(listOf(panelBlock, balloonBlock))

        viewModel.onEnterPressed(balloonBlock.id)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.COMIC_PANEL, state.blocks[1].type)
        assertEquals("PANEL 4", state.blocks[1].text)
    }

    @Test
    fun comicBookTabCyclesArtNoteToCharacterToCaptionToSfx() {
        viewModel.setProjectFormatForTest(ProjectFormat.COMIC_BOOK)
        val block = ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "")
        viewModel.loadDocument(listOf(block))

        // TAB 1: Art Note -> Character
        viewModel.onTabPressed(block.id)
        assertEquals(BlockType.CHARACTER, viewModel.uiState.value.blocks[0].type)

        // TAB 2: Character -> Caption
        viewModel.onTabPressed(block.id)
        assertEquals(BlockType.COMIC_CAPTION, viewModel.uiState.value.blocks[0].type)

        // TAB 3: Caption -> SFX
        viewModel.onTabPressed(block.id)
        assertEquals(BlockType.COMIC_SFX, viewModel.uiState.value.blocks[0].type)

        // TAB 4: SFX -> Art Note
        viewModel.onTabPressed(block.id)
        assertEquals(BlockType.COMIC_ART_NOTE, viewModel.uiState.value.blocks[0].type)
    }
}
