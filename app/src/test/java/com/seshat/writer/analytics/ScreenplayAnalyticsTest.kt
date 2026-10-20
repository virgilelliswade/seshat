package com.seshat.writer.analytics

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.DualDialogueSide
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenplayAnalyticsTest {

    @Test
    fun testBasicCharacterBreakdownAndWordCounts() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. COFFEE SHOP - DAY"),
            ScreenplayBlock(type = BlockType.ACTION, text = "A cozy corner booth."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "ALICE"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Hello Bob. How are you today?"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "BOB"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "I am doing well, thank you.")
        )
        val pageMap = blocks.associate { it.id to 1 }

        val analytics = ScreenplayAnalyticsEngine.analyzeScreenplay(blocks, pageMap)

        assertEquals(2, analytics.characterBreakdowns.size)
        assertEquals(12, analytics.totalDialogueWords)
        assertEquals(2, analytics.totalDialogueCues)

        val alice = analytics.characterBreakdowns.find { it.characterName == "ALICE" }
        assertNotNull(alice)
        assertEquals(1, alice!!.totalSpeeches)
        assertEquals(6, alice.totalWords)
        assertEquals(50.0f, alice.percentageOfDialogue, 0.1f)
        assertEquals(1, alice.firstAppearancePage)
        assertEquals(1, alice.lastAppearancePage)
        assertEquals(1, alice.speakingScenes.size)
        assertEquals("INT. COFFEE SHOP - DAY", alice.speakingScenes[0])

        val bob = analytics.characterBreakdowns.find { it.characterName == "BOB" }
        assertNotNull(bob)
        assertEquals(1, bob!!.totalSpeeches)
        assertEquals(6, bob.totalWords)
        assertEquals(50.0f, bob.percentageOfDialogue, 0.1f)
    }

    @Test
    fun testCharacterExtensionsAreNormalized() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. POLICE STATION - NIGHT"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "DETECTIVE MILLER (V.O.)"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "I should have seen it coming."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "DETECTIVE MILLER (CONT'D)"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "The signs were everywhere.")
        )
        val pageMap = blocks.associate { it.id to 1 }

        val analytics = ScreenplayAnalyticsEngine.analyzeScreenplay(blocks, pageMap)

        assertEquals(1, analytics.characterBreakdowns.size)
        val miller = analytics.characterBreakdowns[0]
        assertEquals("DETECTIVE MILLER", miller.characterName)
        assertEquals(2, miller.totalSpeeches)
        assertEquals(10, miller.totalWords)
        assertEquals(100.0f, miller.percentageOfDialogue, 0.01f)
    }

    @Test
    fun testParentheticalsAreCapturedWithDialogue() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. PARK - DAY"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "SARAH"),
            ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "(whispering)"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Don't make a sound.")
        )
        val pageMap = blocks.associate { it.id to 1 }

        val analytics = ScreenplayAnalyticsEngine.analyzeScreenplay(blocks, pageMap)
        val sarah = analytics.characterBreakdowns[0]
        assertEquals(1, sarah.speeches.size)
        assertEquals("whispering", sarah.speeches[0].parenthetical)
        assertEquals("Don't make a sound.", sarah.speeches[0].dialogueText)
    }

    @Test
    fun testDualDialogueSpeakerSeparation() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. LAB - NIGHT"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "DR. EVANS", dualDialogueId = "dual1", dualSide = DualDialogueSide.LEFT),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Press the button now!", dualDialogueId = "dual1", dualSide = DualDialogueSide.LEFT),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "ASSISTANT", dualDialogueId = "dual1", dualSide = DualDialogueSide.RIGHT),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Wait, it's not ready!", dualDialogueId = "dual1", dualSide = DualDialogueSide.RIGHT)
        )
        val pageMap = blocks.associate { it.id to 1 }

        val analytics = ScreenplayAnalyticsEngine.analyzeScreenplay(blocks, pageMap)
        assertEquals(2, analytics.characterBreakdowns.size)

        val evans = analytics.characterBreakdowns.find { it.characterName == "DR. EVANS" }
        assertNotNull(evans)
        assertEquals(1, evans!!.totalSpeeches)
        assertEquals(4, evans.totalWords)
        assertEquals("Press the button now!", evans.speeches[0].dialogueText)

        val assistant = analytics.characterBreakdowns.find { it.characterName == "ASSISTANT" }
        assertNotNull(assistant)
        assertEquals(1, assistant!!.totalSpeeches)
        assertEquals(4, assistant.totalWords)
        assertEquals("Wait, it's not ready!", assistant.speeches[0].dialogueText)
    }

    @Test
    fun testTextReportGeneration() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. STUDIO - DAY"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "HOST"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Welcome to the show everyone!")
        )
        val pageMap = blocks.associate { it.id to 1 }
        val analytics = ScreenplayAnalyticsEngine.analyzeScreenplay(blocks, pageMap)

        val report = ScreenplayAnalyticsEngine.formatTextReport(analytics, "THE PILOT")
        assertTrue(report.contains("CHARACTER BREAKDOWN & DIALOGUE REPORT"))
        assertTrue(report.contains("THE PILOT"))
        assertTrue(report.contains("HOST"))
        assertTrue(report.contains("Welcome to the show everyone!"))
    }
}
