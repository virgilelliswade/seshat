package com.seshat.writer.pitch

import com.seshat.writer.bible.CharacterDossier
import com.seshat.writer.bible.LoreDocument
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.importer.PdfPositionedLine
import com.seshat.writer.importer.ScreenplayPdfClassifier
import com.seshat.writer.model.BlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PitchDeckAndPdfImportTest {

    @Test
    fun elevatorPitchEstimatesSpeakingTimeAndSerializesRoundTrip() {
        val sampleWords = List(140) { "word" }.joinToString(" ")
        val deck = PitchDeckDocument(
            projectTitle = "NIGHT CITY 2077",
            author = "Virgil Ellis Wade",
            theme = PitchDeckTheme.CYBER_YELLOW_OBSIDIAN,
            elevatorPitch = ElevatorPitchData(
                authorAndCourse = "Virgil Ellis Wade / SCWR 685",
                logline = "A mercenary must survive a rogue brain implant.",
                passionWhyMe = "Raised on 80s sci-fi.",
                spokenPitch = sampleWords
            ),
            slides = listOf(
                PitchSlide(title = "It is the year 2077...", subtitle = "OPENING", body = "Corporate greed.")
            )
        )

        assertEquals(140, deck.elevatorPitch.wordCount())
        assertEquals(60, deck.elevatorPitch.estimatedSeconds(wpm = 140))

        val restored = PitchDeckDocument.fromJson(deck.toJson())
        assertEquals("NIGHT CITY 2077", restored.projectTitle)
        assertEquals(PitchDeckTheme.CYBER_YELLOW_OBSIDIAN, restored.theme)
        assertEquals("Virgil Ellis Wade / SCWR 685", restored.elevatorPitch.authorAndCourse)
        assertEquals(1, restored.slides.size)
        assertEquals("It is the year 2077...", restored.slides.first().title)
    }

    @Test
    fun defaultPitchDeckSeedsFromStoryBibleCharactersAndLore() {
        val bible = StoryBible(
            scriptTitle = "THE EMERALD TABLET",
            lore = LoreDocument(cosmology = "Alchemy rules a flooded Cairo."),
            characters = listOf(
                CharacterDossier(
                    name = "LAYLA",
                    role = "Protagonist",
                    age = "29",
                    gender = "Female",
                    wants = "Steal back the astrolabe.",
                    shardOfGlass = "Never relies on anyone."
                )
            )
        )
        val deck = PitchDeckDocument.defaultForProject("THE EMERALD TABLET", "Ada", bible, CorkboardDocument())
        assertEquals(8, deck.slides.size)
        assertTrue(deck.slides.first().body.contains("Alchemy rules a flooded Cairo."))
        assertTrue(deck.slides[3].body.contains("LAYLA (Age 29, Female)"))
        assertTrue(deck.slides[3].body.contains("Shard of Glass: Never relies on anyone."))
    }

    @Test
    fun pdfClassifierConvertsIndentedScreenplayLinesAndStripsPageNumbers() {
        val lines = listOf(
            PdfPositionedLine("1.", minX = 500f, pageIndex = 0),
            PdfPositionedLine("EXT. NIGHT CITY - RAIN - NIGHT", minX = 108f, pageIndex = 0),
            PdfPositionedLine("Neon signs flicker above wet asphalt.", minX = 108f, pageIndex = 0),
            PdfPositionedLine("VICTORIA", minX = 266f, pageIndex = 0),
            PdfPositionedLine("(into comms)", minX = 223f, pageIndex = 0),
            PdfPositionedLine("We're inside.", minX = 180f, pageIndex = 0),
            PdfPositionedLine("Stay sharp.", minX = 180f, pageIndex = 0),
            PdfPositionedLine("CUT TO:", minX = 430f, pageIndex = 0)
        )

        val imported = ScreenplayPdfClassifier.classifyLines(lines, "Night_City_Pilot.pdf")
        assertEquals("Night City Pilot", imported.titlePage.title)
        assertEquals(6, imported.blocks.size)
        assertEquals(BlockType.SCENE_HEADING, imported.blocks[0].type)
        assertEquals(BlockType.ACTION, imported.blocks[1].type)
        assertEquals(BlockType.CHARACTER, imported.blocks[2].type)
        assertEquals(BlockType.PARENTHETICAL, imported.blocks[3].type)
        assertEquals(BlockType.DIALOGUE, imported.blocks[4].type)
        assertEquals("We're inside. Stay sharp.", imported.blocks[4].text)
        assertEquals(BlockType.TRANSITION, imported.blocks[5].type)
    }
}
