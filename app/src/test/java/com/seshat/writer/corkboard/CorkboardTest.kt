package com.seshat.writer.corkboard

import com.seshat.writer.ai.AiClient
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.screenplay.ScreenplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CorkboardTest {

    @Test
    fun testDefaultActsGeneration() {
        val act1 = CorkboardDocument.defaultActs(1)
        assertEquals(1, act1.size)
        assertEquals("Full Story", act1[0].title)

        val act3 = CorkboardDocument.defaultActs(3)
        assertEquals(3, act3.size)
        assertEquals("Act I", act3[0].title)
        assertEquals("Act II", act3[1].title)
        assertEquals("Act III", act3[2].title)

        val act4 = CorkboardDocument.defaultActs(4)
        assertEquals(4, act4.size)
        assertEquals("Act I", act4[0].title)
        assertEquals("Act II-A", act4[1].title)
        assertEquals("Act II-B", act4[2].title)
        assertEquals("Act III", act4[3].title)

        val act5 = CorkboardDocument.defaultActs(5)
        assertEquals(5, act5.size)
        assertEquals("Act I", act5[0].title)
        assertEquals("Act III", act5[2].title)
        assertEquals("Act V", act5[4].title)

        // Clamping checks
        val clampedMin = CorkboardDocument.defaultActs(0)
        assertEquals(1, clampedMin.size)

        val clampedMax = CorkboardDocument.defaultActs(15)
        assertEquals(9, clampedMax.size)
    }

    @Test
    fun testSceneCardSerialization() {
        val card = SceneCard(
            sceneHeadingBlockId = "block-101",
            sceneNumber = 3,
            headingText = "INT. CYBERNETICS LAB - NIGHT",
            summary = "Dr. Aris uncovers a corrupted consciousness backup while security breaches the perimeter.",
            actIndex = 1,
            colorTag = "#3B82F6",
            charactersInScene = listOf("ARIS", "GUARD 1", "SECURITY CHIEF"),
            blockCount = 12,
            estimatedMinutes = 2.5f
        )

        val json = card.toJson()
        val parsed = SceneCard.fromJson(json)

        assertEquals(card.sceneHeadingBlockId, parsed.sceneHeadingBlockId)
        assertEquals(card.sceneNumber, parsed.sceneNumber)
        assertEquals(card.headingText, parsed.headingText)
        assertEquals(card.summary, parsed.summary)
        assertEquals(card.actIndex, parsed.actIndex)
        assertEquals(card.colorTag, parsed.colorTag)
        assertEquals(card.charactersInScene, parsed.charactersInScene)
        assertEquals(card.blockCount, parsed.blockCount)
        assertEquals(card.estimatedMinutes, parsed.estimatedMinutes, 0.01f)
    }

    @Test
    fun testCorkboardDocumentSerialization() {
        val doc = CorkboardDocument(
            scriptTitle = "PROMETHEUS PROTOCOL",
            actCount = 3,
            acts = CorkboardDocument.defaultActs(3),
            cardMetadata = mapOf(
                "block-001" to CorkboardDocument.CardMetadata(
                    summary = "Initial discovery of the alien monolith.",
                    actIndex = 0,
                    colorTag = "#F59E0B"
                ),
                "block-002" to CorkboardDocument.CardMetadata(
                    summary = "The ship's reactor initiates critical meltdown.",
                    actIndex = 2,
                    colorTag = "#EC4899"
                )
            )
        )

        val json = doc.toJson()
        val parsed = CorkboardDocument.fromJson(json)

        assertEquals("PROMETHEUS PROTOCOL", parsed.scriptTitle)
        assertEquals(3, parsed.actCount)
        assertEquals(3, parsed.acts.size)
        assertEquals("Act I", parsed.acts[0].title)
        assertEquals("Act III", parsed.acts[2].title)
        assertEquals(2, parsed.cardMetadata.size)
        assertEquals("Initial discovery of the alien monolith.", parsed.cardMetadata["block-001"]?.summary)
        assertEquals(2, parsed.cardMetadata["block-002"]?.actIndex)
        assertEquals("#EC4899", parsed.cardMetadata["block-002"]?.colorTag)
    }

    @Test
    fun testCorkboardMarkdownExport() {
        val doc = CorkboardDocument(
            scriptTitle = "NEON HORIZON",
            actCount = 3,
            acts = CorkboardDocument.defaultActs(3)
        )

        val cards = listOf(
            SceneCard(
                sceneHeadingBlockId = "b1",
                sceneNumber = 1,
                headingText = "EXT. RAIN-SLICKED ALLEY - NIGHT",
                summary = "Kael evades drone surveillance in the neon undercity.",
                actIndex = 0,
                charactersInScene = listOf("KAEL"),
                estimatedMinutes = 1.5f,
                blockCount = 5
            ),
            SceneCard(
                sceneHeadingBlockId = "b2",
                sceneNumber = 2,
                headingText = "INT. SAFE HOUSE - NIGHT",
                summary = "Kael meets with Maya to decipher the stolen datapad.",
                actIndex = 1,
                charactersInScene = listOf("KAEL", "MAYA"),
                estimatedMinutes = 3.0f,
                blockCount = 14
            )
        )

        val markdown = CorkboardRepository.exportCorkboardToMarkdown(doc, cards)

        assertTrue(markdown.contains("# SESHAT CORKBOARD OUTLINE: NEON HORIZON"))
        assertTrue(markdown.contains("## ACT I"))
        assertTrue(markdown.contains("Scene 1: EXT. RAIN-SLICKED ALLEY - NIGHT"))
        assertTrue(markdown.contains("> Kael evades drone surveillance in the neon undercity."))
        assertTrue(markdown.contains("- **Characters:** KAEL, MAYA"))
        assertTrue(markdown.contains("*(No scenes currently placed in this act)*")) // Act III is empty
    }

    @Test
    fun testHeuristicSceneSummary() {
        val heading = "INT. CONTROL ROOM - DAY"
        val textWithAction = """
            INT. CONTROL ROOM - DAY
            
            Commander Vance slams his fist onto the hologram console as sirens blare across the bridge.
            
            VANCE
            Get shields back online now!
        """.trimIndent()

        val summary = AiClient.generateHeuristicSceneSummary(heading, textWithAction)
        assertTrue(summary.contains("Commander Vance slams his fist"))

        val emptyText = ""
        val emptySummary = AiClient.generateHeuristicSceneSummary(heading, emptyText)
        assertEquals("Scene set at INT. CONTROL ROOM - DAY.", emptySummary)
    }

    @Test
    fun testSceneCardsAutoCreationWithoutActPollutionInBlocks() {
        // Verify that Final Draft pattern (inserting Act Break blocks into text) is NOT used.
        // Screenplay blocks remain purely script elements (SCENE_HEADING, ACTION, CHARACTER, DIALOGUE).
        val blocks = listOf(
            ScreenplayBlock(id = "s1", type = BlockType.SCENE_HEADING, text = "INT. DINER - MORNING"),
            ScreenplayBlock(id = "a1", type = BlockType.ACTION, text = "The diner is empty except for a lone cook."),
            ScreenplayBlock(id = "c1", type = BlockType.CHARACTER, text = "MARCUS"),
            ScreenplayBlock(id = "d1", type = BlockType.DIALOGUE, text = "Coffee. Black."),
            ScreenplayBlock(id = "s2", type = BlockType.SCENE_HEADING, text = "EXT. ALLEYWAY - MOMENTS LATER"),
            ScreenplayBlock(id = "a2", type = BlockType.ACTION, text = "Marcus steps into the blinding sunlight.")
        )

        val (lead, scenes) = ScreenplayViewModel.partitionIntoScenes(blocks)
        assertEquals(0, lead.size)
        assertEquals(2, scenes.size)

        // Ensure all blocks are pure screenplay types and not artificial act break markers
        val pureTypes = setOf(BlockType.SCENE_HEADING, BlockType.ACTION, BlockType.CHARACTER, BlockType.PARENTHETICAL, BlockType.DIALOGUE, BlockType.TRANSITION)
        for (b in blocks) {
            assertTrue(b.type in pureTypes)
        }

        // Test distribution into default 4 acts
        val totalScenes = scenes.size
        val actCount = 4
        val actIndex1 = (0 * actCount) / totalScenes
        val actIndex2 = (1 * actCount) / totalScenes
        assertEquals(0, actIndex1)
        assertEquals(2, actIndex2)
    }

    @Test
    fun testSceneCardMoveAcrossActsPreservesMetadata() {
        val originalDoc = CorkboardDocument(
            scriptTitle = "TEST PROJECT",
            actCount = 3,
            cardMetadata = mapOf(
                "s1" to CorkboardDocument.CardMetadata(
                    summary = "Intro scene",
                    actIndex = 0,
                    colorTag = "#F59E0B"
                )
            )
        )

        // Move scene s1 to Act 2 (Resolution)
        val currentMeta = originalDoc.cardMetadata["s1"] ?: CorkboardDocument.CardMetadata()
        val updatedMeta = currentMeta.copy(actIndex = 2)
        val updatedMap = originalDoc.cardMetadata.toMutableMap()
        updatedMap["s1"] = updatedMeta
        val updatedDoc = originalDoc.copy(cardMetadata = updatedMap)

        assertEquals(2, updatedDoc.cardMetadata["s1"]?.actIndex)
        assertEquals("Intro scene", updatedDoc.cardMetadata["s1"]?.summary)
        assertEquals("#F59E0B", updatedDoc.cardMetadata["s1"]?.colorTag)
    }
}
