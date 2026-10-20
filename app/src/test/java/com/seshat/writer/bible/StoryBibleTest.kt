package com.seshat.writer.bible

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryBibleTest {

    @Test
    fun testLoreDocumentWordCountAndCompleteness() {
        val emptyLore = LoreDocument()
        assertEquals(0, emptyLore.wordCount())
        assertEquals(0, emptyLore.completenessScore())

        val onePillar = LoreDocument(cosmology = "In the beginning the celestial void coalesced into twin moons.")
        assertTrue(onePillar.wordCount() > 0)
        assertEquals(25, onePillar.completenessScore())

        val twoPillars = onePillar.copy(anthropology = "The solar priesthood commands the upper terraces.")
        assertEquals(50, twoPillars.completenessScore())

        val threePillars = twoPillars.copy(technology = "Aether-driven chronometers regulate civic life.")
        assertEquals(75, threePillars.completenessScore())

        val fourPillars = threePillars.copy(chronology = "Year 0: The Cataclysm. Year 412: Great Scribe Rebellion.")
        assertEquals(100, fourPillars.completenessScore())
        assertEquals(31, fourPillars.wordCount())
    }

    @Test
    fun testLoreDocumentSerialization() {
        val original = LoreDocument(
            cosmology = "Cosmic origins and planar drift.",
            anthropology = "Guild of Archivists and Desert Nomads.",
            technology = "Steam turbines and crystal telegraphs.",
            chronology = "First Age to the Third Sun."
        )

        val json = original.toJson()
        val parsed = LoreDocument.fromJson(json)

        assertEquals(original.cosmology, parsed.cosmology)
        assertEquals(original.anthropology, parsed.anthropology)
        assertEquals(original.technology, parsed.technology)
        assertEquals(original.chronology, parsed.chronology)
    }

    @Test
    fun testCharacterDossierSerialization() {
        val dossier = CharacterDossier(
            id = "char-001",
            name = "Kaelen Voss",
            role = "Protagonist",
            factionOrCaste = "Shadow Archivists",
            arcAndNeed = "Needs to forgive himself for the archive fire.",
            goalAndStakes = "Recover the forbidden manuscript before dawn.",
            voiceCadence = "Quiet, measured, rhythmic cadence.",
            secretsAndLore = "Secret bloodline of the First Cartographer."
        )

        val json = dossier.toJson()
        val parsed = CharacterDossier.fromJson(json)

        assertEquals("char-001", parsed.id)
        assertEquals("Kaelen Voss", parsed.name)
        assertEquals("Protagonist", parsed.role)
        assertEquals("Shadow Archivists", parsed.factionOrCaste)
        assertEquals("Needs to forgive himself for the archive fire.", parsed.arcAndNeed)
        assertEquals("Recover the forbidden manuscript before dawn.", parsed.goalAndStakes)
        assertEquals("Quiet, measured, rhythmic cadence.", parsed.voiceCadence)
        assertEquals("Secret bloodline of the First Cartographer.", parsed.secretsAndLore)
    }

    @Test
    fun testLocationCodexSerialization() {
        val location = LocationCodex(
            id = "loc-001",
            name = "GRAND SCRIPTORIUM",
            sluglinePrefix = "INT.",
            visualAtmosphere = "Dust motes dancing in amber sunlight, towering cedar shelves.",
            culturalSignificance = "Sanctuary of universal knowledge, neutral ground.",
            loreAndHazards = "Automaton guardians patrolling the lower subterranean vaults."
        )

        val json = location.toJson()
        val parsed = LocationCodex.fromJson(json)

        assertEquals("loc-001", parsed.id)
        assertEquals("GRAND SCRIPTORIUM", parsed.name)
        assertEquals("INT.", parsed.sluglinePrefix)
        assertEquals(location.visualAtmosphere, parsed.visualAtmosphere)
        assertEquals(location.culturalSignificance, parsed.culturalSignificance)
        assertEquals(location.loreAndHazards, parsed.loreAndHazards)
    }

    @Test
    fun testStoryBeatSerialization() {
        val beat = StoryBeat(
            id = "beat-001",
            act = "Act I",
            title = "Inciting Incident: The Torn Scroll",
            summary = "Kaelen discovers an illegible erasure in the royal genealogy.",
            charactersInvolved = "Kaelen, High Scribe Mirela",
            dramaticShift = "+ to -",
            colorTag = "#F59E0B"
        )

        val json = beat.toJson()
        val parsed = StoryBeat.fromJson(json)

        assertEquals("beat-001", parsed.id)
        assertEquals("Act I", parsed.act)
        assertEquals("Inciting Incident: The Torn Scroll", parsed.title)
        assertEquals(beat.summary, parsed.summary)
        assertEquals(beat.charactersInvolved, parsed.charactersInvolved)
        assertEquals("+ to -", parsed.dramaticShift)
        assertEquals("#F59E0B", parsed.colorTag)
    }

    @Test
    fun testTvSeriesBibleConfigSerialization() {
        val ep1 = TvEpisodeOutline(
            episodeNumber = 1,
            title = "The Scribe's Heresy",
            aStory = "Kaelen discovers the forbidden vault.",
            bStory = "Mirela investigates an internal audit leak.",
            status = "Drafted"
        )
        val ep2 = TvEpisodeOutline(
            episodeNumber = 2,
            title = "The Obsidian Seal",
            aStory = "Fleeing the citadel across the dunes.",
            bStory = "Council debates royal decree.",
            status = "Outlining"
        )

        val tvConfig = TvSeriesBibleConfig(
            isTvSeries = true,
            seriesTitle = "CHRONICLES OF SESHAT",
            format = "1-Hour Drama (60 min)",
            seasonalArcSummary = "Season 1 chronicles the fall of the solar dynasty through the archives.",
            episodes = listOf(ep1, ep2)
        )

        val json = tvConfig.toJson()
        val parsed = TvSeriesBibleConfig.fromJson(json)

        assertTrue(parsed.isTvSeries)
        assertEquals("CHRONICLES OF SESHAT", parsed.seriesTitle)
        assertEquals("1-Hour Drama (60 min)", parsed.format)
        assertEquals(tvConfig.seasonalArcSummary, parsed.seasonalArcSummary)
        assertEquals(2, parsed.episodes.size)
        assertEquals("The Scribe's Heresy", parsed.episodes[0].title)
        assertEquals("The Obsidian Seal", parsed.episodes[1].title)
    }

    @Test
    fun testEmptyStoryBibleHasContentAndSnippet() {
        val emptyBible = StoryBible(scriptTitle = "UNTITLED SCREENPLAY")
        assertFalse("Empty story bible should report hasContent == false", emptyBible.hasContent())
        assertEquals("Empty story bible should return empty AI snippet", "", emptyBible.toAiContextSnippet())
    }

    @Test
    fun testPopulatedStoryBibleToAiContextSnippet() {
        val bible = StoryBible(
            scriptTitle = "THE GOLDEN COMPASS OF ALEXANDRIA",
            author = "Seshat Scribe",
            lore = LoreDocument(
                cosmology = "The world rests atop tectonic tortoises swimming in light.",
                anthropology = "Scholars revere parchment; traders barter with memory coins.",
                technology = "Alchemical brass lenses allow peering through time.",
                chronology = "500 BCE: Library founded. 48 BCE: Fire averted by the Guild."
            ),
            characters = listOf(
                CharacterDossier(
                    name = "HYPATIA",
                    role = "Protagonist",
                    factionOrCaste = "Mathematicians",
                    arcAndNeed = "Must learn to trust students over scrolls.",
                    secretsAndLore = "Possesses the final star charts."
                )
            ),
            beats = listOf(
                StoryBeat(
                    act = "Act I",
                    title = "The Shadow over the Harbor",
                    summary = "Fleet arrives demanding immediate surrender of the archive."
                )
            ),
            tvConfig = TvSeriesBibleConfig(
                isTvSeries = true,
                format = "Limited Series (8 Episodes)",
                seasonalArcSummary = "The struggle to preserve human knowledge across three sieges."
            )
        )

        assertTrue("Populated story bible has content", bible.hasContent())

        val snippet = bible.toAiContextSnippet()
        assertTrue(snippet.contains("THE GOLDEN COMPASS OF ALEXANDRIA"))
        assertTrue(snippet.contains("1. COSMOLOGY & METAPHYSICAL LAWS:"))
        assertTrue(snippet.contains("tectonic tortoises"))
        assertTrue(snippet.contains("2. ANTHROPOLOGY, FACTIONS & SOCIAL CUSTOMS:"))
        assertTrue(snippet.contains("memory coins"))
        assertTrue(snippet.contains("3. TECHNOLOGY LEVEL & MAGIC/WEAPONS:"))
        assertTrue(snippet.contains("Alchemical brass lenses"))
        assertTrue(snippet.contains("4. CHRONOLOGY & WORLD TIMELINE:"))
        assertTrue(snippet.contains("Library founded"))
        assertTrue(snippet.contains("HYPATIA"))
        assertTrue(snippet.contains("The Shadow over the Harbor"))
        assertTrue(snippet.contains("TV SERIES BIBLE:"))
        assertTrue(snippet.contains("Limited Series (8 Episodes)"))

        // Test word capping
        val truncatedSnippet = bible.toAiContextSnippet(maxWords = 15)
        assertTrue(truncatedSnippet.endsWith("...[truncated]"))
    }

    @Test
    fun testExportBibleToMarkdown() {
        val bible = StoryBible(
            scriptTitle = "THE DUNE ARCHIVES",
            author = "Frank H.",
            lore = LoreDocument(
                cosmology = "Desert universe defined by arid ecology and spice melange.",
                anthropology = "Fremen tribes, Spacing Guild, Bene Gesserit sisterhood.",
                technology = "Holtzman shields, ornithopters, crysknives.",
                chronology = "Butlerian Jihad to the rise of Muad'Dib."
            ),
            characters = listOf(
                CharacterDossier(
                    name = "PAUL",
                    role = "Protagonist",
                    factionOrCaste = "House Atreides",
                    arcAndNeed = "Embrace terrible purpose.",
                    goalAndStakes = "Survive Arrakis.",
                    voiceCadence = "Commanding, prescient.",
                    secretsAndLore = "Kwisatz Haderach."
                )
            ),
            locations = listOf(
                LocationCodex(
                    name = "SIETCH TABR",
                    sluglinePrefix = "INT.",
                    visualAtmosphere = "Cool subterranean cavern lit by glowglobes.",
                    culturalSignificance = "Sacred refuge of the Fremen.",
                    loreAndHazards = "Water discipline strictly enforced."
                )
            ),
            beats = listOf(
                StoryBeat(
                    act = "Act I",
                    title = "Arrival on Arrakis",
                    summary = "The Duke's standard is raised amidst hidden threats."
                )
            ),
            tvConfig = TvSeriesBibleConfig(
                isTvSeries = true,
                seriesTitle = "THE DUNE SAGA",
                format = "Prestige Serial (10 Episodes)",
                seasonalArcSummary = "House Atreides navigates the trap laid on Arrakis."
            )
        )

        val markdown = StoryBibleRepository.exportBibleToMarkdown(bible)
        assertTrue(markdown.contains("# SESHAT STORY BIBLE & LORE DOCUMENT"))
        assertTrue(markdown.contains("THE DUNE ARCHIVES"))
        assertTrue(markdown.contains("### 1. COSMOLOGY & METAPHYSICS"))
        assertTrue(markdown.contains("### 2. ANTHROPOLOGY & FACTIONS"))
        assertTrue(markdown.contains("### 3. TECHNOLOGY & MAGIC LAWS"))
        assertTrue(markdown.contains("### 4. CHRONOLOGY & TIMELINE"))
        assertTrue(markdown.contains("### 5. CHARACTER DOSSIERS"))
        assertTrue(markdown.contains("PAUL"))
        assertTrue(markdown.contains("### 6. LOCATION CODEX"))
        assertTrue(markdown.contains("SIETCH TABR"))
        assertTrue(markdown.contains("### 7. OUTLINE BEAT SHEET"))
        assertTrue(markdown.contains("Arrival on Arrakis"))
        assertTrue(markdown.contains("### 8. TELEVISION SERIES BIBLE & EPISODE GRID"))
        assertTrue(markdown.contains("Prestige Serial (10 Episodes)"))
    }
}
