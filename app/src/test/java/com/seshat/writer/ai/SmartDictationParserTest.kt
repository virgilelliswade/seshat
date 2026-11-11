package com.seshat.writer.ai

import com.seshat.writer.model.BlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartDictationParserTest {

    @Test
    fun parsesNaturalSceneHeadingWithCommas() {
        val blocks = SmartDictationParser.parse("interior, bedroom, night")
        assertEquals(1, blocks.size)
        assertEquals(BlockType.SCENE_HEADING, blocks[0].type)
        assertEquals("INT. BEDROOM - NIGHT", blocks[0].text)
    }

    @Test
    fun parsesExteriorAndComboSceneHeadingsWithoutCommas() {
        val ext = SmartDictationParser.parse("exterior dark alley continuous")
        assertEquals(1, ext.size)
        assertEquals(BlockType.SCENE_HEADING, ext[0].type)
        assertEquals("EXT. DARK ALLEY - CONTINUOUS", ext[0].text)

        val combo = SmartDictationParser.parse("interior exterior vintage sedan dawn")
        assertEquals(1, combo.size)
        assertEquals(BlockType.SCENE_HEADING, combo[0].type)
        assertEquals("I/E. VINTAGE SEDAN - DAWN", combo[0].text)
    }

    @Test
    fun parsesNextLineActionDirectiveAndMultiBlockSpeech() {
        val blocks = SmartDictationParser.parse(
            "interior, bedroom, night. next line action: Elena sits up in bed gasping for air"
        )
        assertEquals(2, blocks.size)
        assertEquals(BlockType.SCENE_HEADING, blocks[0].type)
        assertEquals("INT. BEDROOM - NIGHT", blocks[0].text)
        assertEquals(BlockType.ACTION, blocks[1].type)
        assertEquals("Elena sits up in bed gasping for air.", blocks[1].text)
    }

    @Test
    fun parsesStandaloneModeSwitchCommands() {
        val actionCmd = SmartDictationParser.parseDetailed("next line: action")
        assertTrue(actionCmd.blocks.isEmpty())
        assertEquals(BlockType.ACTION, actionCmd.pendingBlockType)

        val bareActionLine = SmartDictationParser.parseDetailed("action line")
        assertTrue(bareActionLine.blocks.isEmpty())
        assertEquals(BlockType.ACTION, bareActionLine.pendingBlockType)

        val dialogueCmd = SmartDictationParser.parseDetailed("next line dialogue")
        assertTrue(dialogueCmd.blocks.isEmpty())
        assertEquals(BlockType.DIALOGUE, dialogueCmd.pendingBlockType)
    }

    @Test
    fun parsesCharacterSpeechWithParentheticalAndNewCharacters() {
        val blocks = SmartDictationParser.parse("Elena whispers don't open that door")
        assertEquals(3, blocks.size)
        assertEquals(BlockType.CHARACTER, blocks[0].type)
        assertEquals("ELENA", blocks[0].text)
        assertEquals(BlockType.PARENTHETICAL, blocks[1].type)
        assertEquals("(whispers)", blocks[1].text)
        assertEquals(BlockType.DIALOGUE, blocks[2].type)
        assertEquals("Don't open that door.", blocks[2].text)
    }

    @Test
    fun parsesMultiCommandScriptSequence() {
        val blocks = SmartDictationParser.parse(
            "action line Marcus locks the vault. character Marcus. parenthetical breathless dialogue We made it! transition cut to"
        )
        assertEquals(5, blocks.size)
        assertEquals(BlockType.ACTION, blocks[0].type)
        assertEquals("Marcus locks the vault.", blocks[0].text)
        assertEquals(BlockType.CHARACTER, blocks[1].type)
        assertEquals("MARCUS", blocks[1].text)
        assertEquals(BlockType.PARENTHETICAL, blocks[2].type)
        assertEquals("(breathless)", blocks[2].text)
        assertEquals(BlockType.DIALOGUE, blocks[3].type)
        assertEquals("We made it!", blocks[3].text)
        assertEquals(BlockType.TRANSITION, blocks[4].type)
        assertEquals("CUT TO:", blocks[4].text)
    }

    @Test
    fun detectsStopDictationCommands() {
        assertTrue(SmartDictationParser.isStopCommand("stop dictation"))
        assertTrue(SmartDictationParser.isStopCommand("End dictation."))
    }
}
