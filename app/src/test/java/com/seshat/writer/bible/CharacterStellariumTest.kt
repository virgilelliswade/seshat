package com.seshat.writer.bible

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterStellariumTest {

    @Test
    fun stellariumCatalogHasAllEightDomainsAndDrawContrastingStars() {
        assertEquals(8, StellariumDomain.entries.size)
        assertEquals(48, StellariumCatalog.stars.size)

        val drawn = StellariumCatalog.drawContrastingStars(count = 4)
        assertEquals(4, drawn.size)
        // Contrasting draw should pick distinct domains when available
        assertEquals(4, drawn.map { it.domain }.toSet().size)
    }

    @Test
    fun characterDossierJsonRoundTripPreservesDemographicsWantsNeedsShardAndStellarium() {
        val rebel = StellariumCatalog.findById("rebel")!!
        val vampire = StellariumCatalog.findById("vampire")!!
        val caregiver = StellariumCatalog.findById("caregiver")!!

        val dossier = CharacterDossier(
            name = "ELENA VANCE",
            role = "Protagonist",
            age = "34",
            gender = "Female",
            factionOrCaste = "Solar Archivists",
            physicalDescription = "Silver-streaked braid, burn scar over left collarbone.",
            wants = "Recover the stolen Codex before the council convenes.",
            needs = "Accept that she cannot save everyone alone.",
            shardOfGlass = "Believes asking for help always gets someone killed.",
            stellariumStars = listOf(
                StellariumStarSelection.fromCatalogStar(rebel, StarPolarity.POSITIVE),
                StellariumStarSelection.fromCatalogStar(vampire, StarPolarity.SHADOW),
                StellariumStarSelection.fromCatalogStar(caregiver, StarPolarity.DUAL)
            ),
            stellariumNotes = "Protects fiercely, yet quietly drains allies' loyalty."
        )

        val restored = CharacterDossier.fromJson(dossier.toJson())

        assertEquals("ELENA VANCE", restored.name)
        assertEquals("34", restored.age)
        assertEquals("Female", restored.gender)
        assertEquals("Recover the stolen Codex before the council convenes.", restored.wants)
        assertEquals("Accept that she cannot save everyone alone.", restored.needs)
        assertEquals("Believes asking for help always gets someone killed.", restored.shardOfGlass)
        assertEquals(3, restored.stellariumStars.size)
        assertEquals("Rebel", restored.stellariumStars[0].name)
        assertEquals(StarPolarity.POSITIVE, restored.stellariumStars[0].polarity)
        assertEquals("Vampire", restored.stellariumStars[1].name)
        assertEquals(StarPolarity.SHADOW, restored.stellariumStars[1].polarity)
        assertEquals("Protects fiercely, yet quietly drains allies' loyalty.", restored.stellariumNotes)
    }

    @Test
    fun constellationSummaryAndBibleAiSnippetIncludeStellariumAndShard() {
        val seeker = StellariumCatalog.findById("seeker")!!
        val skeptic = StellariumCatalog.findById("skeptic")!!
        val dossier = CharacterDossier(
            name = "MARCUS",
            age = "42",
            gender = "Male",
            wants = "Expose the syndicate.",
            needs = "Let go of his cynicism.",
            shardOfGlass = "Trust is just a delayed betrayal.",
            stellariumStars = listOf(
                StellariumStarSelection.fromCatalogStar(seeker, StarPolarity.POSITIVE),
                StellariumStarSelection.fromCatalogStar(skeptic, StarPolarity.SHADOW)
            )
        )
        val bible = StoryBible(scriptTitle = "NOCTURNE", characters = listOf(dossier))
        val snippet = bible.toAiContextSnippet()
        assertTrue(snippet.contains("Age 42, Male"))
        assertTrue(snippet.contains("Wants: Expose the syndicate."))
        assertTrue(snippet.contains("Shard of Glass: Trust is just a delayed betrayal."))
        assertTrue(snippet.contains("Seeker"))
        assertTrue(snippet.contains("Skeptic"))
    }
}
