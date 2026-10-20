package com.seshat.writer.model

import com.seshat.writer.screenplay.ScreenplayPagination
import com.seshat.writer.screenplay.ScreenplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScreenplayBlockDocumentTest {

    private lateinit var viewModel: ScreenplayViewModel

    @Before
    fun setUp() {
        viewModel = ScreenplayViewModel()
    }

    @Test
    fun initialDocumentHasSingleEmptySceneHeadingBlock() {
        val state = viewModel.uiState.value
        assertEquals(1, state.blocks.size)
        val firstBlock = state.blocks.first()
        assertEquals(BlockType.SCENE_HEADING, firstBlock.type)
        assertEquals("", firstBlock.text)
        assertEquals(firstBlock.id, state.focusedBlockId)
    }

    @Test
    fun updateSceneHeadingForcesUppercase() {
        val blockId = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(blockId, "int. writers room - day")

        val block = viewModel.uiState.value.blocks.first()
        assertEquals("INT. WRITERS ROOM - DAY", block.text)
    }

    @Test
    fun updateSceneHeadingPreservesSpaces() {
        val blockId = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(blockId, "INT. WRITERS ROOM - DAY ")

        val block = viewModel.uiState.value.blocks.first()
        assertEquals("INT. WRITERS ROOM - DAY ", block.text)
    }

    @Test
    fun enterOnSceneHeadingCreatesActionBlockAndFocusesIt() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(headingId, "INT. WRITERS ROOM - DAY")
        viewModel.onEnterPressed(headingId)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(BlockType.SCENE_HEADING, state.blocks[0].type)
        assertEquals(BlockType.ACTION, state.blocks[1].type)
        assertEquals("", state.blocks[1].text)
        assertEquals(state.blocks[1].id, state.focusedBlockId)
    }

    @Test
    fun backspaceOnEmptyActionBlockDeletesItAndFocusesSceneHeading() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(headingId)

        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.onBackspaceOnEmptyBlock(actionId)

        val state = viewModel.uiState.value
        assertEquals(1, state.blocks.size)
        assertEquals(headingId, state.blocks[0].id)
        assertEquals(headingId, state.focusedBlockId)
    }

    @Test
    fun typingSceneHeadingPrefixInActionBlockAutoPromotesToSceneHeading() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. FIRST ROOM - DAY")
        viewModel.onEnterPressed(heading1Id)

        val actionBlock = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.ACTION, actionBlock.type)

        // User types slugline prefix in action block
        viewModel.updateBlockText(actionBlock.id, "ext. second location - night")

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        val secondBlock = state.blocks[1]
        assertEquals(BlockType.SCENE_HEADING, secondBlock.type)
        assertEquals("EXT. SECOND LOCATION - NIGHT", secondBlock.text)
        assertEquals(2, state.sceneCount)
        assertEquals(2, state.scenes.size)
        assertEquals("EXT. SECOND LOCATION - NIGHT", state.scenes[1].heading)
    }

    @Test
    fun prefixesLikeEstAndIeAreRecognized() {
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("INT."))
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("int. "))
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("EXT."))
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("I/E."))
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("INT/EXT."))
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("EST."))
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("INT "))
        assertTrue(ScreenplayViewModel.isSceneHeadingPrefix("ext "))
    }

    @Test
    fun regularActionWordsStartingWithIntOrExtDoNotPromote() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. ROOM - DAY")
        viewModel.onEnterPressed(heading1Id)

        val actionBlock = viewModel.uiState.value.blocks[1]
        viewModel.updateBlockText(actionBlock.id, "Interesting conversation begins.")

        val state = viewModel.uiState.value
        val block = state.blocks[1]
        assertEquals(BlockType.ACTION, block.type)
        assertEquals("Interesting conversation begins.", block.text)
        assertEquals(1, state.sceneCount)
    }

    @Test
    fun manualDropdownChangeBlockTypeUpdatesTypeAndSceneCount() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. ROOM - DAY")
        viewModel.onEnterPressed(heading1Id)

        val secondBlockId = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(secondBlockId, "Basement stairs leading down")
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)
        assertEquals(1, viewModel.uiState.value.sceneCount)

        // User selects SCENE_HEADING from dropdown
        viewModel.changeBlockType(secondBlockId, BlockType.SCENE_HEADING)

        val updatedState = viewModel.uiState.value
        assertEquals(BlockType.SCENE_HEADING, updatedState.blocks[1].type)
        assertEquals("BASEMENT STAIRS LEADING DOWN", updatedState.blocks[1].text)
        assertEquals(2, updatedState.sceneCount)
        assertEquals(2, updatedState.scenes.size)

        // User changes it back to ACTION from dropdown
        viewModel.changeBlockType(secondBlockId, BlockType.ACTION)
        val revertedState = viewModel.uiState.value
        assertEquals(BlockType.ACTION, revertedState.blocks[1].type)
        assertEquals(1, revertedState.sceneCount)
        assertEquals(1, revertedState.scenes.size)
    }

    @Test
    fun insertNewBlockAddsSceneHeadingAndFocusesIt() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. ROOM - DAY")

        // User clicks "+ Insert Scene Heading"
        viewModel.insertNewBlock(afterId = heading1Id, type = BlockType.SCENE_HEADING)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        val newBlock = state.blocks[1]
        assertEquals(BlockType.SCENE_HEADING, newBlock.type)
        assertEquals("", newBlock.text)
        assertEquals(newBlock.id, state.focusedBlockId)
        assertEquals(2, state.sceneCount)
    }

    @Test
    fun typingSoloPrefixAutoFormatsWithTrailingSpace() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. ROOM - DAY")
        viewModel.onEnterPressed(heading1Id)

        val actionId = viewModel.uiState.value.blocks[1].id
        // User types "int." in action block
        viewModel.updateBlockText(actionId, "int.")

        val state = viewModel.uiState.value
        val block = state.blocks[1]
        assertEquals(BlockType.SCENE_HEADING, block.type)
        // Newly created slugline has trailing space so typing continues immediately after slugline
        assertEquals("INT. ", block.text)

        // User types next character
        viewModel.updateBlockText(actionId, "INT. K")
        assertEquals("INT. K", viewModel.uiState.value.blocks[1].text)
    }

    @Test
    fun backspacingSluglineDoesNotReExpandTrailingSpace() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "int.")
        assertEquals("INT. ", viewModel.uiState.value.blocks[0].text)

        // User backspaces the trailing space
        viewModel.updateBlockText(heading1Id, "INT.")
        assertEquals("INT.", viewModel.uiState.value.blocks[0].text)

        // User backspaces the dot
        viewModel.updateBlockText(heading1Id, "INT")
        assertEquals("INT", viewModel.uiState.value.blocks[0].text)
    }

    @Test
    fun tabOnActionBlockConvertsToCharacterAndForcesUppercase() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(actionId, "sarah")
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)

        // User presses Tab
        viewModel.onTabPressed(actionId)

        val block = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.CHARACTER, block.type)
        assertEquals("SARAH", block.text)
    }

    @Test
    fun tabOnEmptyActionBlockConvertsToCharacter() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(actionId)

        val block = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.CHARACTER, block.type)
        assertEquals("", block.text)
    }

    @Test
    fun enterOnCharacterCreatesDialogueBlockAndFocusesIt() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val blockId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(blockId)
        viewModel.updateBlockText(blockId, "SARAH")

        // User presses Enter on Character
        viewModel.onEnterPressed(blockId)

        val state = viewModel.uiState.value
        assertEquals(3, state.blocks.size)
        val dialogueBlock = state.blocks[2]
        assertEquals(BlockType.DIALOGUE, dialogueBlock.type)
        assertEquals("", dialogueBlock.text)
        assertEquals(dialogueBlock.id, state.focusedBlockId)
    }

    @Test
    fun enterOnDialogueCreatesActionBlockAndFocusesIt() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onEnterPressed(charId)

        val dialogueId = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(dialogueId, "I told you not to open that door.")

        // User presses Enter on Dialogue
        viewModel.onEnterPressed(dialogueId)

        val state = viewModel.uiState.value
        assertEquals(4, state.blocks.size)
        val actionBlock = state.blocks[3]
        assertEquals(BlockType.ACTION, actionBlock.type)
        assertEquals("", actionBlock.text)
        assertEquals(actionBlock.id, state.focusedBlockId)
    }

    @Test
    fun backspaceOnEmptyCharacterRevertsToAction() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val blockId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(blockId)
        assertEquals(BlockType.CHARACTER, viewModel.uiState.value.blocks[1].type)

        // Press Backspace on empty Character
        viewModel.onBackspaceOnEmptyBlock(blockId)
        val reverted = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.ACTION, reverted.type)
        assertEquals("", reverted.text)
    }

    @Test
    fun backspaceOnEmptyDialogueDeletesItAndFocusesCharacter() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onEnterPressed(charId)

        val dialogueId = viewModel.uiState.value.blocks[2].id
        assertEquals(BlockType.DIALOGUE, viewModel.uiState.value.blocks[2].type)

        // User presses Backspace on empty Dialogue
        viewModel.onBackspaceOnEmptyBlock(dialogueId)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(charId, state.blocks[1].id)
        assertEquals(charId, state.focusedBlockId)
    }

    @Test
    fun characterForcesUppercaseWhileDialoguePreservesMixedCase() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "detective miller")
        assertEquals("DETECTIVE MILLER", viewModel.uiState.value.blocks[1].text)

        viewModel.onEnterPressed(charId)
        val dialogueId = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(dialogueId, "Where were you on June 5th?")
        assertEquals("Where were you on June 5th?", viewModel.uiState.value.blocks[2].text)
    }

    @Test
    fun typingOpenParenInActionPromotesToParenthetical() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val actionId = viewModel.uiState.value.blocks[1].id
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)

        // Type "(" in action block
        viewModel.updateBlockText(actionId, "(whispering)")

        val updated = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.PARENTHETICAL, updated.type)
        assertEquals("(whispering)", updated.text)
    }

    @Test
    fun typingOpenParenInDialoguePromotesToParenthetical() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onEnterPressed(charId)

        val dialogueId = viewModel.uiState.value.blocks[2].id
        assertEquals(BlockType.DIALOGUE, viewModel.uiState.value.blocks[2].type)

        // User starts new dialogue line with "("
        viewModel.updateBlockText(dialogueId, "(beat)")

        val updated = viewModel.uiState.value.blocks[2]
        assertEquals(BlockType.PARENTHETICAL, updated.type)
        assertEquals("(beat)", updated.text)
    }

    @Test
    fun typingOpenParenInCharacterDoesNotPromoteToParenthetical() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH (V.O.)")

        val block = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.CHARACTER, block.type)
        assertEquals("SARAH (V.O.)", block.text)
    }

    @Test
    fun enterOnParentheticalCreatesDialogueAndEnclosesParens() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onTabPressed(charId) // Tab on Character creates Parenthetical

        val parenId = viewModel.uiState.value.blocks[2].id
        assertEquals(BlockType.PARENTHETICAL, viewModel.uiState.value.blocks[2].type)
        viewModel.updateBlockText(parenId, "smiling")

        // Press Enter on parenthetical
        viewModel.onEnterPressed(parenId)

        val state = viewModel.uiState.value
        assertEquals(4, state.blocks.size)
        // Parenthetical auto-closes with parens
        assertEquals("(smiling)", state.blocks[2].text)
        // Next block is dialogue and focused
        assertEquals(BlockType.DIALOGUE, state.blocks[3].type)
        assertEquals("", state.blocks[3].text)
        assertEquals(state.blocks[3].id, state.focusedBlockId)
    }

    @Test
    fun tabOnCharacterInsertsParentheticalBlock() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "DETECTIVE")

        // Tab on Character
        viewModel.onTabPressed(charId)

        val state = viewModel.uiState.value
        assertEquals(3, state.blocks.size)
        assertEquals(BlockType.PARENTHETICAL, state.blocks[2].type)
        assertEquals("", state.blocks[2].text)
        assertEquals(state.blocks[2].id, state.focusedBlockId)
    }

    @Test
    fun tabOnEmptyDialogueConvertsToParenthetical() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "DETECTIVE")
        viewModel.onEnterPressed(charId)

        val dialogueId = viewModel.uiState.value.blocks[2].id
        assertEquals(BlockType.DIALOGUE, viewModel.uiState.value.blocks[2].type)

        // Tab on empty Dialogue
        viewModel.onTabPressed(dialogueId)

        val state = viewModel.uiState.value
        assertEquals(3, state.blocks.size)
        assertEquals(BlockType.PARENTHETICAL, state.blocks[2].type)
    }

    @Test
    fun tabAfterDialogueGoesToParentheticalWhileEnterGoesToAction() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onEnterPressed(charId)

        val dialogueId = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(dialogueId, "I didn't expect to see you here.")

        // Tab after dialogue -> creates PARENTHETICAL!
        viewModel.onTabPressed(dialogueId)

        val stateAfterTab = viewModel.uiState.value
        assertEquals(4, stateAfterTab.blocks.size)
        val parenBlock = stateAfterTab.blocks[3]
        assertEquals(BlockType.PARENTHETICAL, parenBlock.type)
        assertEquals("", parenBlock.text)
        assertEquals(parenBlock.id, stateAfterTab.focusedBlockId)

        // Enter on Parenthetical -> creates DIALOGUE!
        viewModel.updateBlockText(parenBlock.id, "whispering")
        viewModel.onEnterPressed(parenBlock.id)

        val stateAfterParen = viewModel.uiState.value
        assertEquals(5, stateAfterParen.blocks.size)
        assertEquals("(whispering)", stateAfterParen.blocks[3].text)
        val secondDial = stateAfterParen.blocks[4]
        assertEquals(BlockType.DIALOGUE, secondDial.type)
        assertEquals("", secondDial.text)
        assertEquals(secondDial.id, stateAfterParen.focusedBlockId)

        // Enter after Dialogue -> goes to ACTION!
        viewModel.updateBlockText(secondDial.id, "Not after everything that happened.")
        viewModel.onEnterPressed(secondDial.id)

        val stateAfterDial = viewModel.uiState.value
        assertEquals(6, stateAfterDial.blocks.size)
        val actionBlock = stateAfterDial.blocks[5]
        assertEquals(BlockType.ACTION, actionBlock.type)
        assertEquals("", actionBlock.text)
        assertEquals(actionBlock.id, stateAfterDial.focusedBlockId)
    }

    @Test
    fun backspaceOnEmptyParentheticalDeletesAndFocusesPreviousBlock() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onTabPressed(charId)

        val parenId = viewModel.uiState.value.blocks[2].id
        assertEquals(BlockType.PARENTHETICAL, viewModel.uiState.value.blocks[2].type)

        // Backspace on empty parenthetical
        viewModel.onBackspaceOnEmptyBlock(parenId)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        assertEquals(charId, state.blocks[1].id)
        assertEquals(charId, state.focusedBlockId)
    }

    @Test
    fun characterExtensionRegexFormatsVoAndOs() {
        assertEquals("SARAH (V.O.)", ScreenplayViewModel.formatCharacterText("SARAH VO"))
        assertEquals("SARAH (V.O.)", ScreenplayViewModel.formatCharacterText("SARAH V.O."))
        assertEquals("SARAH (V.O.)", ScreenplayViewModel.formatCharacterText("SARAH (VO)"))
        assertEquals("SARAH (V.O.)", ScreenplayViewModel.formatCharacterText("SARAH (V.O.)"))
        assertEquals("SARAH (O.S.)", ScreenplayViewModel.formatCharacterText("SARAH OS"))
        assertEquals("SARAH (O.S.)", ScreenplayViewModel.formatCharacterText("SARAH O.S."))
        assertEquals("SARAH (O.S.)", ScreenplayViewModel.formatCharacterText("SARAH (OS)"))
        assertEquals("SARAH (O.C.)", ScreenplayViewModel.formatCharacterText("SARAH OC"))
        assertEquals("SARAH (CONT'D)", ScreenplayViewModel.formatCharacterText("SARAH CONT'D"))
        assertEquals("SARAH (CONT'D)", ScreenplayViewModel.formatCharacterText("SARAH CONTD"))
    }

    @Test
    fun characterExtensionRegexDoesNotMangleRegularNames() {
        assertEquals("GUSTAVO", ScreenplayViewModel.formatCharacterText("GUSTAVO"))
        assertEquals("CARLOS", ScreenplayViewModel.formatCharacterText("CARLOS"))
        assertEquals("DOC", ScreenplayViewModel.formatCharacterText("DOC"))
    }

    @Test
    fun cleanCharacterNameStripsExtensionsProperly() {
        assertEquals("SARAH", ScreenplayViewModel.cleanCharacterName("SARAH (V.O.)"))
        assertEquals("SARAH", ScreenplayViewModel.cleanCharacterName("SARAH (O.S.)"))
        assertEquals("SARAH", ScreenplayViewModel.cleanCharacterName("SARAH (CONT'D)"))
        assertEquals("SARAH", ScreenplayViewModel.cleanCharacterName("SARAH (V.O.) (CONT'D)"))
        assertEquals("DETECTIVE MILLER", ScreenplayViewModel.cleanCharacterName("DETECTIVE MILLER"))
    }

    @Test
    fun toggleCharacterExtensionAddsAndRemovesExtension() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")

        // Toggle (V.O.) on
        viewModel.toggleCharacterExtension(charId, "(V.O.)")
        assertEquals("SARAH (V.O.)", viewModel.uiState.value.blocks[1].text)

        // Toggle (V.O.) off
        viewModel.toggleCharacterExtension(charId, "(V.O.)")
        assertEquals("SARAH", viewModel.uiState.value.blocks[1].text)
    }

    @Test
    fun toggleCharacterExtensionReplacesOsWithVo() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH (O.S.)")

        // Toggle (V.O.)
        viewModel.toggleCharacterExtension(charId, "(V.O.)")
        assertEquals("SARAH (V.O.)", viewModel.uiState.value.blocks[1].text)
    }

    @Test
    fun autoContdAppendedOnlyOnSecondOrGreaterSpeechInScene() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. COFFEE SHOP - DAY")
        viewModel.onEnterPressed(heading1Id)

        // Sarah speaks first time
        val char1Id = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(char1Id)
        viewModel.updateBlockText(char1Id, "SARAH")
        viewModel.onEnterPressed(char1Id)
        // First speech does NOT have CONT'D
        assertEquals("SARAH", viewModel.uiState.value.blocks[1].text)

        val dial1Id = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(dial1Id, "I'll have a coffee.")
        viewModel.onEnterPressed(dial1Id)

        // Action in between
        val actionId = viewModel.uiState.value.blocks[3].id
        viewModel.updateBlockText(actionId, "The barista nods and grinds beans.")
        viewModel.onEnterPressed(actionId)

        // Sarah speaks second time in a row in the same scene
        val char2Id = viewModel.uiState.value.blocks[4].id
        viewModel.onTabPressed(char2Id)
        viewModel.updateBlockText(char2Id, "SARAH")
        viewModel.onEnterPressed(char2Id)

        // Second speech MUST auto-append (CONT'D)!
        assertEquals("SARAH (CONT'D)", viewModel.uiState.value.blocks[4].text)
    }

    @Test
    fun autoContdNotAppendedIfDifferentCharacterSpoke() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. COFFEE SHOP - DAY")
        viewModel.onEnterPressed(heading1Id)

        // Sarah speaks
        val char1Id = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(char1Id)
        viewModel.updateBlockText(char1Id, "SARAH")
        viewModel.onEnterPressed(char1Id)

        val dial1Id = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(dial1Id, "I'll have a coffee.")
        viewModel.onEnterPressed(dial1Id)

        // John speaks next
        val char2Id = viewModel.uiState.value.blocks[3].id
        viewModel.onTabPressed(char2Id)
        viewModel.updateBlockText(char2Id, "JOHN")
        viewModel.onEnterPressed(char2Id)
        assertEquals("JOHN", viewModel.uiState.value.blocks[3].text)

        val dial2Id = viewModel.uiState.value.blocks[4].id
        viewModel.updateBlockText(dial2Id, "Make that two.")
        viewModel.onEnterPressed(dial2Id)

        // Sarah speaks again - but John spoke in between, so NOT in a row!
        val char3Id = viewModel.uiState.value.blocks[5].id
        viewModel.onTabPressed(char3Id)
        viewModel.updateBlockText(char3Id, "SARAH")
        viewModel.onEnterPressed(char3Id)

        assertEquals("SARAH", viewModel.uiState.value.blocks[5].text)
    }

    @Test
    fun autoContdNotAppendedAcrossSceneHeading() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(heading1Id, "INT. COFFEE SHOP - DAY")
        viewModel.onEnterPressed(heading1Id)

        // Sarah speaks in Scene 1
        val char1Id = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(char1Id)
        viewModel.updateBlockText(char1Id, "SARAH")
        viewModel.onEnterPressed(char1Id)

        val dial1Id = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(dial1Id, "Let's go outside.")
        viewModel.onEnterPressed(dial1Id)

        // New Scene Heading: EXT. STREET - DAY
        val heading2Id = viewModel.uiState.value.blocks[3].id
        viewModel.updateBlockText(heading2Id, "ext. street - day")
        viewModel.onEnterPressed(heading2Id)

        // Sarah speaks in Scene 2 for the first time
        val char2Id = viewModel.uiState.value.blocks[4].id
        viewModel.onTabPressed(char2Id)
        viewModel.updateBlockText(char2Id, "SARAH")
        viewModel.onEnterPressed(char2Id)

        // Must NOT have CONT'D because it's a new scene!
        assertEquals("SARAH", viewModel.uiState.value.blocks[4].text)
    }

    @Test
    fun typingCutToInActionPromotesToTransitionAndForcesUppercase() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val actionId = viewModel.uiState.value.blocks[1].id
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)

        // Type "cut to:" in action block
        viewModel.updateBlockText(actionId, "cut to:")

        val block = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.TRANSITION, block.type)
        assertEquals("CUT TO:", block.text)
    }

    @Test
    fun typingFadeOutPromotesToTransition() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(actionId, "fade out.")

        val block = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.TRANSITION, block.type)
        assertEquals("FADE OUT.", block.text)
    }

    @Test
    fun enterOnTransitionCreatesSceneHeadingAndFocusesIt() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val transId = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(transId, "CUT TO:")
        assertEquals(BlockType.TRANSITION, viewModel.uiState.value.blocks[1].type)

        // Press Enter on Transition
        viewModel.onEnterPressed(transId)

        val state = viewModel.uiState.value
        assertEquals(3, state.blocks.size)
        val newSceneHeading = state.blocks[2]
        assertEquals(BlockType.SCENE_HEADING, newSceneHeading.type)
        assertEquals("", newSceneHeading.text)
        assertEquals(newSceneHeading.id, state.focusedBlockId)
    }

    @Test
    fun enterOnTransitionAutoAppendsColonIfMissing() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val transId = viewModel.uiState.value.blocks[1].id
        viewModel.changeBlockType(transId, BlockType.TRANSITION)
        viewModel.updateBlockText(transId, "SMASH CUT TO")

        // Press Enter on Transition without colon
        viewModel.onEnterPressed(transId)

        val state = viewModel.uiState.value
        assertEquals("SMASH CUT TO:", state.blocks[1].text)
        assertEquals(BlockType.SCENE_HEADING, state.blocks[2].type)
    }

    @Test
    fun backspaceOnEmptyTransitionDemotesToAction() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val blockId = viewModel.uiState.value.blocks[1].id
        viewModel.changeBlockType(blockId, BlockType.TRANSITION)
        assertEquals(BlockType.TRANSITION, viewModel.uiState.value.blocks[1].type)

        // Backspace on empty Transition
        viewModel.onBackspaceOnEmptyBlock(blockId)

        val reverted = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.ACTION, reverted.type)
        assertEquals("", reverted.text)
    }

    @Test
    fun setTransitionPresetSetsTextAndPreservesType() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(heading1Id)

        val blockId = viewModel.uiState.value.blocks[1].id
        viewModel.setTransitionPreset(blockId, "DISSOLVE TO:")

        val block = viewModel.uiState.value.blocks[1]
        assertEquals(BlockType.TRANSITION, block.type)
        assertEquals("DISSOLVE TO:", block.text)
    }

    @Test
    fun insertNewBlockTransitionAddsTransitionBlock() {
        val heading1Id = viewModel.uiState.value.blocks.first().id
        viewModel.insertNewBlock(afterId = heading1Id, type = BlockType.TRANSITION)

        val state = viewModel.uiState.value
        assertEquals(2, state.blocks.size)
        val transBlock = state.blocks[1]
        assertEquals(BlockType.TRANSITION, transBlock.type)
        assertEquals("", transBlock.text)
        assertEquals(transBlock.id, state.focusedBlockId)
    }

    @Test
    fun isTransitionTriggerRecognizesStandardTransitions() {
        assertTrue(ScreenplayViewModel.isTransitionTrigger("CUT TO:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("cut to:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("DISSOLVE TO:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("SMASH CUT TO:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("FADE OUT."))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("FADE TO BLACK."))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("FADE IN:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("MATCH CUT TO:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("JUMP CUT TO:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("BACK TO:"))
        assertTrue(ScreenplayViewModel.isTransitionTrigger("SLOW FADE TO:"))
    }

    @Test
    fun typingToInDialogueDoesNotPromoteToTransition() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(headingId)
        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(actionId)
        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onEnterPressed(charId)
        val dialogueId = viewModel.uiState.value.blocks[2].id
        assertEquals(BlockType.DIALOGUE, viewModel.uiState.value.blocks[2].type)

        // Type common sentences with "to" in dialogue
        viewModel.updateBlockText(dialogueId, "I want to go to the store.")
        assertEquals(BlockType.DIALOGUE, viewModel.uiState.value.blocks[2].type)
        assertEquals("I want to go to the store.", viewModel.uiState.value.blocks[2].text)

        // Even typing explicit transition keywords in dialogue stays dialogue
        viewModel.updateBlockText(dialogueId, "CUT TO: the truth.")
        assertEquals(BlockType.DIALOGUE, viewModel.uiState.value.blocks[2].type)
    }

    @Test
    fun typingToInActionDoesNotPromoteToTransition() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(headingId)
        val actionId = viewModel.uiState.value.blocks[1].id
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)

        // Type words with "to" in action
        viewModel.updateBlockText(actionId, "He walks to the door.")
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)
        assertEquals("He walks to the door.", viewModel.uiState.value.blocks[1].text)

        // "CUT TO" without colon does NOT trigger transition
        viewModel.updateBlockText(actionId, "CUT TO")
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)

        // "CUT TO:" with colon DOES trigger transition
        viewModel.updateBlockText(actionId, "CUT TO:")
        assertEquals(BlockType.TRANSITION, viewModel.uiState.value.blocks[1].type)
        assertEquals("CUT TO:", viewModel.uiState.value.blocks[1].text)
    }

    @Test
    fun firstLetterInActionLineIsAutomaticallyCapitalized() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(headingId)
        val actionId = viewModel.uiState.value.blocks[1].id
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)

        // User types lowercase start
        viewModel.updateBlockText(actionId, "the door swings open.")
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)
        assertEquals("The door swings open.", viewModel.uiState.value.blocks[1].text)

        // Preserves leading spaces with capitalized first letter
        viewModel.updateBlockText(actionId, "  a cold breeze enters.")
        assertEquals("  A cold breeze enters.", viewModel.uiState.value.blocks[1].text)

        // Changing block type to ACTION capitalizes first letter
        viewModel.updateBlockText(actionId, "shadows stretch across the room.")
        viewModel.changeBlockType(actionId, BlockType.ACTION)
        assertEquals("Shadows stretch across the room.", viewModel.uiState.value.blocks[1].text)

        // On Enter pressed, action line is capitalized
        viewModel.updateBlockText(actionId, "quiet steps approach")
        viewModel.onEnterPressed(actionId)
        assertEquals("Quiet steps approach", viewModel.uiState.value.blocks[1].text)
    }

    @Test
    fun parseSceneHeadingExtractsComponentsCorrectly() {
        val parts1 = ScreenplayViewModel.parseSceneHeading("INT. COFFEE SHOP - DAY")
        assertEquals("INT.", parts1.prefix)
        assertEquals("COFFEE SHOP", parts1.location)
        assertEquals("DAY", parts1.timeOfDay)
        assertTrue(parts1.hasHyphen)

        val parts2 = ScreenplayViewModel.parseSceneHeading("EXT. APARTMENT BUILDING - ROOFTOP - NIGHT")
        assertEquals("EXT.", parts2.prefix)
        assertEquals("APARTMENT BUILDING - ROOFTOP", parts2.location)
        assertEquals("NIGHT", parts2.timeOfDay)
        assertTrue(parts2.hasHyphen)

        val parts3 = ScreenplayViewModel.parseSceneHeading("INT. WAREHOUSE")
        assertEquals("INT.", parts3.prefix)
        assertEquals("WAREHOUSE", parts3.location)
        org.junit.Assert.assertNull(parts3.timeOfDay)
        org.junit.Assert.assertFalse(parts3.hasHyphen)

        val parts4 = ScreenplayViewModel.parseSceneHeading("INT. ")
        assertEquals("INT.", parts4.prefix)
        assertEquals("", parts4.location)
        org.junit.Assert.assertNull(parts4.timeOfDay)
        org.junit.Assert.assertFalse(parts4.hasHyphen)

        val parts5 = ScreenplayViewModel.parseSceneHeading("I/E. CAR - CONTINUOUS")
        assertEquals("I/E.", parts5.prefix)
        assertEquals("CAR", parts5.location)
        assertEquals("CONTINUOUS", parts5.timeOfDay)
    }

    @Test
    fun extractCharactersAndLocationsAggregatesUniqueItemsSortedByFrequency() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. COFFEE SHOP - DAY"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "SARAH"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Hello."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "JOHN (V.O.)"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Hi Sarah."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "SARAH (CONT'D)"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Good to see you."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. STREET - NIGHT"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "SARAH"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Let's go."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. COFFEE SHOP - NIGHT")
        )

        val characters = ScreenplayViewModel.extractCharacters(blocks)
        // SARAH appeared 3 times, JOHN appeared 1 time
        assertEquals(listOf("SARAH", "JOHN"), characters)

        val locations = ScreenplayViewModel.extractLocations(blocks)
        // COFFEE SHOP appeared twice, STREET appeared once
        assertEquals(listOf("COFFEE SHOP", "STREET"), locations)
    }

    @Test
    fun applyCharacterSuggestionSetsNameAndAutoAppendsContd() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(headingId) // Action
        val actionId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(actionId) // Character
        val char1Id = viewModel.uiState.value.blocks[1].id

        viewModel.applyCharacterSuggestion(char1Id, "DETECTIVE MILLER")
        assertEquals("DETECTIVE MILLER", viewModel.uiState.value.blocks[1].text)
        assertEquals(listOf("DETECTIVE MILLER"), viewModel.uiState.value.characters)

        viewModel.onEnterPressed(char1Id) // Dialogue
        val dial1Id = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(dial1Id, "Where were you last night?")

        viewModel.onEnterPressed(dial1Id) // Action
        val action2Id = viewModel.uiState.value.blocks[3].id
        viewModel.updateBlockText(action2Id, "He steps closer.")

        viewModel.onEnterPressed(action2Id) // Action
        val action3Id = viewModel.uiState.value.blocks[4].id
        viewModel.onTabPressed(action3Id) // Character
        val char2Id = viewModel.uiState.value.blocks[4].id

        // Detective Miller speaks again in the same scene after action -> auto (CONT'D)
        viewModel.applyCharacterSuggestion(char2Id, "DETECTIVE MILLER")
        assertEquals("DETECTIVE MILLER (CONT'D)", viewModel.uiState.value.blocks[4].text)
    }

    @Test
    fun applySceneSmartSuggestionsFillsPrefixLocationAndTime() {
        val headingId = viewModel.uiState.value.blocks.first().id

        // Step 1: Prefix
        viewModel.applyScenePrefix(headingId, "INT.")
        assertEquals("INT. ", viewModel.uiState.value.blocks.first().text)

        // Step 2: Location
        viewModel.applySceneLocation(headingId, "BANK VAULT")
        assertEquals("INT. BANK VAULT - ", viewModel.uiState.value.blocks.first().text)

        // Step 3: Time
        viewModel.applySceneTime(headingId, "NIGHT")
        assertEquals("INT. BANK VAULT - NIGHT", viewModel.uiState.value.blocks.first().text)
        assertEquals(listOf("BANK VAULT"), viewModel.uiState.value.locations)
    }

    @Test
    fun countWrappedLinesWordWrapsAppropriately() {
        // Under max length
        assertEquals(1, ScreenplayPagination.countWrappedLines("Short line.", 60))
        // Exactly at max length
        assertEquals(1, ScreenplayPagination.countWrappedLines("A".repeat(60), 60))
        // Multiple lines word wrapped
        val text = "The detective walks into the dimly lit room and slowly examines the old wooden desk in the corner."
        // 99 chars, maxChars 35 -> wraps to 3 lines
        assertEquals(3, ScreenplayPagination.countWrappedLines(text, 35))
    }

    @Test
    fun initialDocumentInitializesPaginationToSinglePage() {
        val state = viewModel.uiState.value
        assertEquals(1, state.pageCount)
        assertEquals(1, state.estimatedMinutes)
        assertEquals(1, state.totalLines)
        assertTrue(state.pageBreakBeforeBlockIds.isEmpty())
        val firstBlockId = state.blocks.first().id
        assertEquals(1, state.blockPageNumbers[firstBlockId])
    }

    @Test
    fun orphanSceneHeadingBreaksToNextPageWhenNearBottom() {
        val blocks = mutableListOf<ScreenplayBlock>()
        blocks.add(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. INITIAL ROOM - DAY"))
        for (i in 1..26) {
            blocks.add(ScreenplayBlock(type = BlockType.ACTION, text = "Action line $i."))
        }
        val secondHeading = ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. SECOND LOCATION - NIGHT")
        blocks.add(secondHeading)

        val result = ScreenplayPagination.calculatePagination(blocks)
        assertEquals(2, result.totalPages)
        assertEquals(2, result.estimatedMinutes)
        assertTrue(result.pageBreakBeforeBlockIds.contains(secondHeading.id))
        assertEquals(2, result.blockPageNumbers[secondHeading.id])
    }

    @Test
    fun orphanCharacterHeadingBreaksToNextPageWhenNearBottom() {
        val blocks = mutableListOf<ScreenplayBlock>()
        blocks.add(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. INITIAL ROOM - DAY"))
        for (i in 1..26) {
            blocks.add(ScreenplayBlock(type = BlockType.ACTION, text = "Action line $i."))
        }
        val charBlock = ScreenplayBlock(type = BlockType.CHARACTER, text = "DETECTIVE")
        val dialBlock = ScreenplayBlock(type = BlockType.DIALOGUE, text = "Hold on.")
        blocks.add(charBlock)
        blocks.add(dialBlock)

        val result = ScreenplayPagination.calculatePagination(blocks)
        assertEquals(2, result.totalPages)
        assertTrue(result.pageBreakBeforeBlockIds.contains(charBlock.id))
        assertEquals(2, result.blockPageNumbers[charBlock.id])
        assertEquals(2, result.blockPageNumbers[dialBlock.id])
    }

    @Test
    fun generalOverflowIncreasesPageCountAndSyncsWithViewModel() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(headingId, "INT. FIRST ROOM - DAY")

        var currentId = headingId
        for (i in 1..30) {
            viewModel.onEnterPressed(currentId)
            val nextBlock = viewModel.uiState.value.blocks.last()
            viewModel.updateBlockText(nextBlock.id, "Action paragraph $i describes the scene in rich detail.")
            currentId = nextBlock.id
        }

        val state = viewModel.uiState.value
        assertTrue(state.pageCount >= 2)
        assertEquals(state.pageCount, state.estimatedMinutes)
        assertTrue(state.pageBreakBeforeBlockIds.isNotEmpty())
    }

    @Test
    fun fadeInDoesNotCountTowardSceneCount() {
        val firstBlockId = viewModel.uiState.value.blocks.first().id
        // User sets first block to FADE IN:
        viewModel.updateBlockText(firstBlockId, "FADE IN:")

        val stateAfterFadeIn = viewModel.uiState.value
        assertEquals(0, stateAfterFadeIn.sceneCount)
        assertEquals(0, stateAfterFadeIn.scenes.size)

        // Press Enter on FADE IN: -> creates a SCENE_HEADING block
        viewModel.onEnterPressed(firstBlockId)
        val secondBlockId = viewModel.uiState.value.blocks[1].id
        viewModel.updateBlockText(secondBlockId, "INT. APARTMENT - DAY")

        val stateAfterFirstScene = viewModel.uiState.value
        // FADE IN: does not count, so sceneCount is 1, and Scene 1 is INT. APARTMENT - DAY
        assertEquals(1, stateAfterFirstScene.sceneCount)
        assertEquals(1, stateAfterFirstScene.scenes.size)
        assertEquals(1, stateAfterFirstScene.scenes[0].index)
        assertEquals("INT. APARTMENT - DAY", stateAfterFirstScene.scenes[0].heading)
    }

    @Test
    fun overBlackIsRecognizedAsSceneHeadingAndCountsAsScene() {
        val firstBlockId = viewModel.uiState.value.blocks.first().id
        viewModel.updateBlockText(firstBlockId, "OVER BLACK")

        val state = viewModel.uiState.value
        assertEquals(BlockType.SCENE_HEADING, state.blocks[0].type)
        assertEquals(1, state.sceneCount)
        assertEquals(1, state.scenes.size)
        assertEquals("OVER BLACK", state.scenes[0].heading)

        // Typing action under OVER BLACK
        viewModel.onEnterPressed(firstBlockId)
        val actionId = viewModel.uiState.value.blocks[1].id
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)
        viewModel.updateBlockText(actionId, "we hear footsteps echo in the dark.")

        // First letter in action line is capitalized
        assertEquals("We hear footsteps echo in the dark.", viewModel.uiState.value.blocks[1].text)

        // Enter FADE IN: next
        viewModel.insertNewBlock(afterId = actionId, type = BlockType.SCENE_HEADING)
        val fadeInId = viewModel.uiState.value.blocks[2].id
        viewModel.updateBlockText(fadeInId, "FADE IN:")

        // Still only 1 scene (OVER BLACK) because FADE IN: doesn't count
        assertEquals(1, viewModel.uiState.value.sceneCount)
        assertEquals(1, viewModel.uiState.value.scenes.size)

        // Add next scene INT. CELL - NIGHT
        viewModel.onEnterPressed(fadeInId)
        val intId = viewModel.uiState.value.blocks[3].id
        viewModel.updateBlockText(intId, "INT. CELL - NIGHT")

        // Now total 2 scenes: Scene 1: OVER BLACK, Scene 2: INT. CELL - NIGHT
        val finalState = viewModel.uiState.value
        assertEquals(2, finalState.sceneCount)
        assertEquals(2, finalState.scenes.size)
        assertEquals("OVER BLACK", finalState.scenes[0].heading)
        assertEquals("INT. CELL - NIGHT", finalState.scenes[1].heading)
    }

    @Test
    fun dialogueAutoCapitalizesFirstLetter() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(headingId)
        val charId = viewModel.uiState.value.blocks[1].id
        viewModel.onTabPressed(charId)
        viewModel.updateBlockText(charId, "SARAH")
        viewModel.onEnterPressed(charId)

        val dialId = viewModel.uiState.value.blocks[2].id
        assertEquals(BlockType.DIALOGUE, viewModel.uiState.value.blocks[2].type)

        // User types lowercase start in dialogue
        viewModel.updateBlockText(dialId, "we don't have much time.")
        assertEquals("We don't have much time.", viewModel.uiState.value.blocks[2].text)

        // Preserves leading quotes/spaces while capitalizing first letter
        viewModel.updateBlockText(dialId, "  \"can you hear me?\"")
        assertEquals("  \"Can you hear me?\"", viewModel.uiState.value.blocks[2].text)

        // Changing block type to DIALOGUE auto-capitalizes first letter
        viewModel.changeBlockType(dialId, BlockType.ACTION)
        viewModel.updateBlockText(dialId, "whispers in the dark.")
        viewModel.changeBlockType(dialId, BlockType.DIALOGUE)
        assertEquals("Whispers in the dark.", viewModel.uiState.value.blocks[2].text)
    }

    @Test
    fun typingOverBlackInActionAutoPromotesToSceneHeading() {
        val headingId = viewModel.uiState.value.blocks.first().id
        viewModel.onEnterPressed(headingId)
        val actionId = viewModel.uiState.value.blocks[1].id
        assertEquals(BlockType.ACTION, viewModel.uiState.value.blocks[1].type)

        // Type "over black" in action block
        viewModel.updateBlockText(actionId, "over black")
        assertEquals(BlockType.SCENE_HEADING, viewModel.uiState.value.blocks[1].type)
        assertEquals("OVER BLACK", viewModel.uiState.value.blocks[1].text)
    }
}
