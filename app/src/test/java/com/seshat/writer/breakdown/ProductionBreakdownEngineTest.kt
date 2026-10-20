package com.seshat.writer.breakdown

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductionBreakdownEngineTest {

    private lateinit var blocks: List<ScreenplayBlock>
    private lateinit var blockPageNumbers: Map<String, Int>

    @Before
    fun setUp() {
        val b1 = ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. CASINO VAULT - NIGHT", sceneNumber = "14")
        val b2 = ScreenplayBlock(type = BlockType.ACTION, text = "DANNY OCEAN slips on black leather gloves. RUSTY rumbles through his duffel bag, pulling out a heavy blowtorch, dynamite, and a silenced pistol.")
        val b3 = ScreenplayBlock(type = BlockType.CHARACTER, text = "DANNY OCEAN")
        val b4 = ScreenplayBlock(type = BlockType.DIALOGUE, text = "Suit looks sharp, Rusty. Tuxedo fitting?")
        val b5 = ScreenplayBlock(type = BlockType.CHARACTER, text = "RUSTY")
        val b6 = ScreenplayBlock(type = BlockType.DIALOGUE, text = "Always wear Armani on a heist. A siren wails outside.")
        val b7 = ScreenplayBlock(type = BlockType.ACTION, text = "Sparks explode as the vault door hinges burn. Rusty fires his gun at the security camera. Glass shatters.")
        val b8 = ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. LAS VEGAS STRIP - NIGHT", sceneNumber = "15")
        val b9 = ScreenplayBlock(type = BlockType.ACTION, text = "A black getaway van screeches around the corner. Sirens echo as two police cruisers give chase in a high-speed vehicle collision.")

        blocks = listOf(b1, b2, b3, b4, b5, b6, b7, b8, b9)
        blockPageNumbers = mapOf(
            b1.id to 12, b2.id to 12, b3.id to 12, b4.id to 12, b5.id to 12, b6.id to 12, b7.id to 12,
            b8.id to 13, b9.id to 13
        )
    }

    @Test
    fun generateBreakdownExtractsSceneDetails() {
        val summary = ProductionBreakdownEngine.generateBreakdown(blocks, blockPageNumbers)

        assertEquals(2, summary.scenes.size)
        val scene1 = summary.scenes[0]
        assertEquals("INT. CASINO VAULT - NIGHT", scene1.heading)
        assertEquals("14", scene1.sceneNumber)
        assertEquals("INT.", scene1.intExt)
        assertEquals("CASINO VAULT", scene1.location)
        assertEquals("NIGHT", scene1.timeOfDay)
        assertTrue(scene1.pageCountEighths.contains("pgs") || scene1.pageCountEighths.contains("/8"))

        // Check cast extraction
        assertTrue(scene1.castMembers.contains("DANNY OCEAN"))
        assertTrue(scene1.castMembers.contains("RUSTY"))
    }

    @Test
    fun generateBreakdownIntelligentlyDetectsProductionElements() {
        val summary = ProductionBreakdownEngine.generateBreakdown(blocks, blockPageNumbers)
        val scene1 = summary.scenes[0]

        // Check props detected from action (bag, blowtorch, dynamite, pistol/gun)
        assertTrue("Props should include pistol or gun or bag", scene1.props.any { it.contains("pistol", ignoreCase = true) || it.contains("gun", ignoreCase = true) || it.contains("bag", ignoreCase = true) })

        // Check wardrobe detected (gloves, tuxedo, suit)
        assertTrue("Wardrobe should detect gloves, suit or tuxedo", scene1.wardrobe.any { it.contains("gloves", ignoreCase = true) || it.contains("tuxedo", ignoreCase = true) || it.contains("suit", ignoreCase = true) })

        // Check SFX detected (sparks, explode, fire)
        assertTrue("SFX should detect sparks, explode, or fire", scene1.specialEffects.any { it.contains("sparks", ignoreCase = true) || it.contains("explode", ignoreCase = true) || it.contains("fire", ignoreCase = true) })

        // Check Sound detected (siren, glass)
        assertTrue("Sound notes should detect siren or glass", scene1.soundNotes.any { it.contains("siren", ignoreCase = true) || it.contains("glass", ignoreCase = true) })

        // Scene 2 checks for vehicles & stunts
        val scene2 = summary.scenes[1]
        assertTrue("Vehicles should detect van or police cruiser", scene2.vehicles.any { it.contains("van", ignoreCase = true) || it.contains("cruiser", ignoreCase = true) || it.contains("car", ignoreCase = true) })
        assertTrue("Stunts should detect chase or collision", scene2.stunts.any { it.contains("chase", ignoreCase = true) || it.contains("collision", ignoreCase = true) || it.contains("crash", ignoreCase = true) })
    }

    @Test
    fun computeSummaryTotals() {
        val summary = ProductionBreakdownEngine.generateBreakdown(blocks, blockPageNumbers)

        assertEquals(2, summary.totalScenes)
        assertTrue(summary.uniqueCastCount >= 2)
        assertTrue(summary.allProps.isNotEmpty())
        assertTrue(summary.allVehicles.isNotEmpty())
        assertTrue(summary.allStunts.isNotEmpty())
        assertTrue(summary.allSfx.isNotEmpty())
        assertNotNull(summary.totalPagesFormatted)
    }

    @Test
    fun generateBreakdownTextReport() {
        val summary = ProductionBreakdownEngine.generateBreakdown(blocks, blockPageNumbers)
        val reportText = ProductionBreakdownEngine.generateBreakdownTextReport(summary, "OCEAN'S ELEVEN")

        assertTrue(reportText.contains("ASSISTANT DIRECTOR PRODUCTION BREAKDOWN"))
        assertTrue(reportText.contains("OCEAN'S ELEVEN"))
        assertTrue(reportText.contains("SCENE 14: INT. CASINO VAULT - NIGHT"))
        assertTrue(reportText.contains("CAST:"))
        assertTrue(reportText.contains("PROPS:"))
    }
}
