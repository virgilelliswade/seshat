package com.seshat.writer.screenplay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FountainParserTest {

    private val parser = FountainParser()

    @Test
    fun parseSimpleSceneWithHeadingActionCharacterAndDialogue() {
        val fountainScript = """
            INT. ALEXANDRIA LIBRARY - NIGHT
            
            Torches flicker along the massive papyrus scrolls. SESHAT stands before an ancient ledger, quill in hand.
            
            SESHAT
            Every word inscribed here outlasts empires.
            
            (smiling faintly)
            Even Rome.
            
            FADE OUT.
        """.trimIndent()

        val elements = parser.parse(fountainScript)

        assertTrue(elements.isNotEmpty())

        val heading = elements[0] as ScreenplayElement.SceneHeading
        assertEquals("INT. ALEXANDRIA LIBRARY - NIGHT", heading.text)

        val action = elements[1] as ScreenplayElement.Action
        assertTrue(action.text.contains("Torches flicker"))

        val character = elements[2] as ScreenplayElement.Character
        assertEquals("SESHAT", character.name)

        val dialogue = elements[3] as ScreenplayElement.Dialogue
        assertEquals("Every word inscribed here outlasts empires.", dialogue.text)

        val parenthetical = elements[4] as ScreenplayElement.Parenthetical
        assertEquals("(smiling faintly)", parenthetical.text)

        val dialogue2 = elements[5] as ScreenplayElement.Dialogue
        assertEquals("Even Rome.", dialogue2.text)

        val transition = elements[6] as ScreenplayElement.Transition
        assertEquals("FADE OUT.", transition.text)
    }

    @Test
    fun extractSceneHeadingsForNavigation() {
        val script = """
            INT. SCRIBE CHAMBER - DAY
            Action here.
            
            EXT. NILE RIVER - SUNSET
            More action.
            
            INT. TEMPLE ARCHIVE - NIGHT
            Final scene.
        """.trimIndent()

        val scenes = parser.extractScenes(script)
        assertEquals(3, scenes.size)
        assertEquals("INT. SCRIBE CHAMBER - DAY", scenes[0].heading)
        assertEquals("EXT. NILE RIVER - SUNSET", scenes[1].heading)
        assertEquals("INT. TEMPLE ARCHIVE - NIGHT", scenes[2].heading)
    }

    @Test
    fun extractUniqueCharacterNames() {
        val script = """
            SESHAT
            Greetings.
            
            THOTH
            Welcome, Seshat.
            
            SESHAT
            The ledger is ready.
        """.trimIndent()

        val characters = parser.extractCharacters(script)
        assertEquals(listOf("SESHAT", "THOTH"), characters)
    }

    @Test
    fun calculatePageCountEstimate() {
        // Industry standard: approximately 54 formatted screenplay lines = 1 page
        val lines = (1..108).joinToString("\n") { "Action line $it in the grand library." }
        val pageCount = parser.estimatePageCount(lines)
        assertEquals(2.0, pageCount, 0.1)
    }
}
