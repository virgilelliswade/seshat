package com.seshat.writer.dictation

import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiConfig
import com.seshat.writer.ai.AiProvider
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceDictationTest {

    @Test
    fun testDefaultAiModelsAreUpdatedToLatest() {
        val config = AiConfig()
        assertEquals("gemini-3.8-flash", AiProvider.GOOGLE_GEMINI.defaultModel)
        assertEquals("claude-3-7-sonnet", AiProvider.ANTHROPIC_CLAUDE.defaultModel)
        assertEquals("llama3.3", AiProvider.CUSTOM_LOCAL.defaultModel)
        assertEquals("gemini-3.8-flash", config.modelName)
        assertTrue(config.enableVoiceDictation)
    }

    @Test
    fun testOfflineFallbackSluglineParsing() {
        val characters = listOf("MARCUS", "MAYA")

        val interiorResult = AiClient.formatDictationOfflineFallback("interior coffee shop day", characters)
        assertEquals(1, interiorResult.size)
        assertEquals(BlockType.SCENE_HEADING, interiorResult[0].type)
        assertEquals("INT. COFFEE SHOP DAY", interiorResult[0].text)

        val exteriorResult = AiClient.formatDictationOfflineFallback("exterior warehouse night", characters)
        assertEquals(1, exteriorResult.size)
        assertEquals(BlockType.SCENE_HEADING, exteriorResult[0].type)
        assertEquals("EXT. WAREHOUSE NIGHT", exteriorResult[0].text)

        val shortSlugResult = AiClient.formatDictationOfflineFallback("int. apartment bedroom morning", characters)
        assertEquals(1, shortSlugResult.size)
        assertEquals(BlockType.SCENE_HEADING, shortSlugResult[0].type)
        assertEquals("INT. APARTMENT BEDROOM MORNING", shortSlugResult[0].text)
    }

    @Test
    fun testOfflineFallbackCharacterDialogueParsing() {
        val characters = listOf("MARCUS", "MAYA")

        // Format: "MARCUS: We have to leave immediately"
        val colonResult = AiClient.formatDictationOfflineFallback("MARCUS: we have to leave immediately", characters)
        assertEquals(2, colonResult.size)
        assertEquals(BlockType.CHARACTER, colonResult[0].type)
        assertEquals("MARCUS", colonResult[0].text)
        assertEquals(BlockType.DIALOGUE, colonResult[1].type)
        assertEquals("We have to leave immediately", colonResult[1].text)

        // Format: "Maya says do you hear that sound"
        val saysResult = AiClient.formatDictationOfflineFallback("maya says do you hear that sound", characters)
        assertEquals(2, saysResult.size)
        assertEquals(BlockType.CHARACTER, saysResult[0].type)
        assertEquals("MAYA", saysResult[0].text)
        assertEquals(BlockType.DIALOGUE, saysResult[1].type)
        assertEquals("Do you hear that sound", saysResult[1].text)

        // Format: "Marcus said get down"
        val saidResult = AiClient.formatDictationOfflineFallback("marcus said get down", characters)
        assertEquals(2, saidResult.size)
        assertEquals(BlockType.CHARACTER, saidResult[0].type)
        assertEquals("MARCUS", saidResult[0].text)
        assertEquals(BlockType.DIALOGUE, saidResult[1].type)
        assertEquals("Get down", saidResult[1].text)
    }

    @Test
    fun testOfflineFallbackActionLinePunctuation() {
        val characters = listOf("MARCUS", "MAYA")

        val actionResult = AiClient.formatDictationOfflineFallback("she turns around slowly and gasps", characters)
        assertEquals(1, actionResult.size)
        assertEquals(BlockType.ACTION, actionResult[0].type)
        assertEquals("She turns around slowly and gasps.", actionResult[0].text)
    }

    @Test
    fun testFountainSerializationOfAiGeneratedDictation() {
        // Sample Fountain string that Gemini 3.8 / 4 produces from spoken speech
        val aiGeneratedFountain = """
            INT. HOTEL CORRIDOR - NIGHT
            
            Marcus draws his weapon, footsteps echoing down the hallway.
            
            MARCUS
            (whispering)
            Cover the fire escape.
            
            MAYA
            Already on it.
        """.trimIndent()

        val parsedDoc = FountainSerializer.importFromFountain(aiGeneratedFountain)
        assertEquals(7, parsedDoc.blocks.size)

        assertEquals(BlockType.SCENE_HEADING, parsedDoc.blocks[0].type)
        assertEquals("INT. HOTEL CORRIDOR - NIGHT", parsedDoc.blocks[0].text)

        assertEquals(BlockType.ACTION, parsedDoc.blocks[1].type)
        assertEquals("Marcus draws his weapon, footsteps echoing down the hallway.", parsedDoc.blocks[1].text)

        assertEquals(BlockType.CHARACTER, parsedDoc.blocks[2].type)
        assertEquals("MARCUS", parsedDoc.blocks[2].text)

        assertEquals(BlockType.PARENTHETICAL, parsedDoc.blocks[3].type)
        assertEquals("(whispering)", parsedDoc.blocks[3].text)

        assertEquals(BlockType.DIALOGUE, parsedDoc.blocks[4].type)
        assertEquals("Cover the fire escape.", parsedDoc.blocks[4].text)

        assertEquals(BlockType.CHARACTER, parsedDoc.blocks[5].type)
        assertEquals("MAYA", parsedDoc.blocks[5].text)

        assertEquals(BlockType.DIALOGUE, parsedDoc.blocks[6].type)
        assertEquals("Already on it.", parsedDoc.blocks[6].text)
    }
}
