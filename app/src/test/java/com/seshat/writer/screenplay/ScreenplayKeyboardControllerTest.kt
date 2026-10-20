package com.seshat.writer.screenplay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScreenplayKeyboardControllerTest {

    private lateinit var controller: ScreenplayKeyboardController

    @Before
    fun setUp() {
        controller = ScreenplayKeyboardController()
    }

    @Test
    fun enterFromSceneHeadingTransitionsToActionAndForcesAllCaps() {
        val script = "int. alexandria library - night"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("INT. ALEXANDRIA LIBRARY - NIGHT\n\n", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun enterFromFadeInTransitionsToOpeningSlugline() {
        val script = "FADE IN:"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("FADE IN:\n\nINT. ", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun enterFromFadeInWithoutColonTransitionsToOpeningSlugline() {
        val script = "FADE IN"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("FADE IN:\n\nINT. ", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun enterFromCharacterTransitionsToDialogue() {
        val script = "INT. LIBRARY - NIGHT\n\nSESHAT"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT\n          ", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun automaticContdWhenSameCharacterSpeaksConsecutively() {
        val script = """
            INT. ARCHIVE - NIGHT
            
            SESHAT
            First line of wisdom.
            
            She turns the scroll.
            
            SESHAT
        """.trimIndent()
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertTrue(result.newText.endsWith("SESHAT (CONT'D)\n          "))
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun contdAppearsAfterOtherExtensions() {
        val script = """
            INT. ARCHIVE - NIGHT
            
            SESHAT
            First line of wisdom.
            
            She turns the scroll.
            
            SESHAT (V.O.)
        """.trimIndent()
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertTrue(result.newText.endsWith("SESHAT (V.O.) (CONT'D)\n          "))
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun noContdWhenDifferentCharacterSpeaks() {
        val script = """
            INT. ARCHIVE - NIGHT
            
            SESHAT
            First line.
            
            THOTH
        """.trimIndent()
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertTrue(result.newText.endsWith("THOTH\n          "))
    }

    @Test
    fun enterFromActionTransitionsWithSingleNewlineAndNoGulf() {
        val script = "INT. ALEXANDRIA LIBRARY - NIGHT\n\nTorches flicker along the scrolls."
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        // Single newline, zero blank line gulfs!
        assertEquals("INT. ALEXANDRIA LIBRARY - NIGHT\n\nTorches flicker along the scrolls.\n", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun tabFromBlankOrActionLineTransitionsToCharacterWith22Spaces() {
        val script = "INT. ALEXANDRIA LIBRARY - NIGHT\n\nTorches flicker along the scrolls."
        val cursor = script.length
        val result = controller.handleTab(script, cursor)

        // Inserts 22 spaces character indent and cursor lands at the end of the spaces
        assertTrue(result.newText.endsWith("\n\n                      "))
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun enterFromIndentedCharacterTransitionsToDialogueWithSingleNewline() {
        val script = "INT. LIBRARY - NIGHT\n\n                      SESHAT"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          ", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun enterFromIndentedLowercaseCharacterForcesUppercaseWithSingleNewline() {
        val script = "INT. LIBRARY - NIGHT\n\n                      seshat"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          ", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun tabFromCharacterTransitionsToParenthetical() {
        val script = "INT. LIBRARY - NIGHT\n\nSESHAT"
        val cursor = script.length
        val result = controller.handleTab(script, cursor)

        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT\n                ()", result.newText)
        assertEquals(result.newText.length - 1, result.newCursor)
    }

    @Test
    fun tabFromDialogueTransitionsToParenthetical() {
        val script = "INT. LIBRARY - NIGHT\n\n                      SESHAT\n          Are the archives aligned?"
        val cursor = script.length
        val result = controller.handleTab(script, cursor)

        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          Are the archives aligned?\n                ()", result.newText)
        assertEquals(result.newText.length - 1, result.newCursor)
    }

    @Test
    fun enterFromParentheticalTransitionsToDialogue() {
        val script = "INT. LIBRARY - NIGHT\n\nSESHAT\n(calmly)"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT\n(calmly)\n          ", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun enterFromDialogueMovesToNewActionBlock() {
        val script = "INT. LIBRARY - NIGHT\n\n                      SESHAT\n          Every word outlasts empires."
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          Every word outlasts empires.\n\n", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun enterFromIsolatedParentheticalTransitionsWithSingleNewline() {
        val script = "SESHAT\n(whispering)"
        val cursor = script.length
        val result = controller.handleEnter(script, cursor)

        assertEquals("SESHAT\n(whispering)\n          ", result.newText)
        assertEquals(result.newText.length, result.newCursor)
    }

    @Test
    fun fullDialogueBlockTransitionsHaveZeroBlankLines() {
        // Character -> Dialogue
        val s1 = "INT. LIBRARY - NIGHT\n\n                      SESHAT"
        val r1 = controller.handleEnter(s1, s1.length)
        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          ", r1.newText)

        // Type Dialogue, then Tab to Parenthetical
        val s2 = r1.newText + "We have found the scrolls."
        val r2 = controller.handleTab(s2, s2.length)
        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          We have found the scrolls.\n                ()", r2.newText)
        assertEquals(r2.newText.length - 1, r2.newCursor)

        // Type Parenthetical, then Enter to Dialogue
        val s3 = r2.newText.substring(0, r2.newCursor) + "whispering" + r2.newText.substring(r2.newCursor)
        val r3 = controller.handleEnter(s3, s3.length)
        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          We have found the scrolls.\n                (whispering)\n          ", r3.newText)
    }

    @Test
    fun spaceOnCharacterCyclesThroughExtensions() {
        val script = "INT. LIBRARY - NIGHT\n\nSESHAT"
        val cursor = script.length

        // First space adds (V.O.)
        val r1 = controller.handleSpace(script, cursor)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT (V.O.)", r1?.newText)

        // Second space cycles to (O.S.)
        val r2 = controller.handleSpace(r1!!.newText, r1.newCursor)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT (O.S.)", r2?.newText)

        // Third space cycles to (O.C.)
        val r3 = controller.handleSpace(r2!!.newText, r2.newCursor)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT (O.C.)", r3?.newText)

        // Fourth space cycles to (PRE-LAP)
        val r4 = controller.handleSpace(r3!!.newText, r3.newCursor)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT (PRE-LAP)", r4?.newText)

        // Fifth space returns to base name
        val r5 = controller.handleSpace(r4!!.newText, r4.newCursor)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT", r5?.newText)
    }

    @Test
    fun spaceOnCharacterPreservesContdAtEnd() {
        val script = "INT. LIBRARY - NIGHT\n\nSESHAT (CONT'D)"
        val cursor = script.length

        val r1 = controller.handleSpace(script, cursor)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT (V.O.) (CONT'D)", r1?.newText)

        val r2 = controller.handleSpace(r1!!.newText, r1.newCursor)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT (O.S.) (CONT'D)", r2?.newText)
    }

    @Test
    fun spaceOnActionReturnsNull() {
        val script = "INT. LIBRARY - NIGHT\n\nShe turns the ancient parchment"
        val cursor = script.length

        val result = controller.handleSpace(script, cursor)
        assertNull(result)
    }

    @Test
    fun autoCapitalizeFirstLetterOfActionAndSentences() {
        val input = "torches flicker in the grand hall. shadows dance on stone."
        val output = controller.autoCapitalizeSentences(input)
        assertEquals("Torches flicker in the grand hall. Shadows dance on stone.", output)
    }

    @Test
    fun autoCapitalizeStandalonePronounI() {
        val input = "when i look up, i see the constellation."
        val output = controller.autoCapitalizeSentences(input)
        assertEquals("When I look up, I see the constellation.", output)
    }

    @Test
    fun autoLowercaseParenthetical() {
        val input = "(WHISPERING CALMLY)"
        val output = controller.autoLowercaseParenthetical(input)
        assertEquals("(whispering calmly)", output)

        val typingInput = "(Whispering"
        val typingOutput = controller.autoLowercaseParenthetical(typingInput)
        assertEquals("(whispering", typingOutput)
    }

    @Test
    fun formatCasingSluglineForcesAllCaps() {
        val script = "int. alexandria archive - dusk"
        val result = controller.formatCasing(script, script.length)
        assertEquals("INT. ALEXANDRIA ARCHIVE - DUSK", result.newText)
        assertEquals("INT. ALEXANDRIA ARCHIVE - DUSK".length, result.newCursor)
    }

    @Test
    fun formatCasingFadeInForcesAllCaps() {
        val script = "fade in:"
        val result = controller.formatCasing(script, script.length)
        assertEquals("FADE IN:", result.newText)
    }

    @Test
    fun formatCasingKnownCharacterForcesAllCaps() {
        val script = "INT. LIBRARY - NIGHT\n\nseshat"
        val result = controller.formatCasing(script, script.length, knownCharacters = listOf("SESHAT"))
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT", result.newText)
    }

    @Test
    fun formatCasingTabbedCharacterForcesAllCaps() {
        // Tab from action creates character line
        val script = "INT. LIBRARY - NIGHT\n\nTorches flicker.\n\n                      "
        val scriptWithChar = script + "thoth"
        val result = controller.formatCasing(scriptWithChar, scriptWithChar.length)
        assertEquals("INT. LIBRARY - NIGHT\n\nTorches flicker.\n\n                      THOTH", result.newText)
    }

    @Test
    fun formatCasingDialogueAutoCapitalizesFirstLetter() {
        val script = "INT. LIBRARY - NIGHT\n\nSESHAT\nevery word outlasts empires. remember that."
        val result = controller.formatCasing(script, script.length)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT\nEvery word outlasts empires. Remember that.", result.newText)
    }

    @Test
    fun formatCasingParentheticalForcesLowercase() {
        val script = "INT. LIBRARY - NIGHT\n\nSESHAT\n(BEAT)"
        val result = controller.formatCasing(script, script.length)
        assertEquals("INT. LIBRARY - NIGHT\n\nSESHAT\n(beat)", result.newText)
    }

    @Test
    fun formatCasingPreservesTrailingSpaceInAction() {
        val script = "INT. ROOM - DAY\n\nhe entered the room "
        val result = controller.formatCasing(script, script.length)
        assertEquals("INT. ROOM - DAY\n\nHe entered the room ", result.newText)
        assertEquals(script.length, result.newCursor)
    }

    @Test
    fun formatCasingPreservesTrailingSpaceInDialogue() {
        val script = "INT. ROOM - DAY\n\nSESHAT\ni believe "
        val result = controller.formatCasing(script, script.length)
        assertEquals("INT. ROOM - DAY\n\nSESHAT\nI believe ", result.newText)
        assertEquals(script.length, result.newCursor)
    }

    @Test
    fun formatCasingPreservesTrailingSpaceInCharacterName() {
        val script = "INT. ROOM - DAY\n\n                      agent "
        val result = controller.formatCasing(script, script.length)
        assertEquals("INT. ROOM - DAY\n\n                      AGENT ", result.newText)
        assertEquals(script.length, result.newCursor)
    }

    @Test
    fun formatCasingPreservesTrailingSpaceInSceneHeading() {
        val script = "int. office "
        val result = controller.formatCasing(script, script.length)
        assertEquals("INT. OFFICE ", result.newText)
        assertEquals(script.length, result.newCursor)
    }

    @Test
    fun handleTextChangeNewlineOnFirstCharacterDoesNotAddContd() {
        val oldText = "INT. LIBRARY - NIGHT\n\n                      SESHAT"
        val newText = "INT. LIBRARY - NIGHT\n\n                      SESHAT\n"
        val result = controller.handleTextChange(oldText, newText, newText.length)
        assertEquals("INT. LIBRARY - NIGHT\n\n                      SESHAT\n          ", result.newText)
    }

    @Test
    fun handleTextChangeNewlineOnConsecutiveCharacterAddsContd() {
        val oldText = "INT. ARCHIVE - NIGHT\n\nSESHAT\nFirst line.\n\nShe turns.\n\n                      SESHAT"
        val newText = oldText + "\n"
        val result = controller.handleTextChange(oldText, newText, newText.length)
        assertTrue(result.newText.endsWith("SESHAT (CONT'D)\n          "))
    }

    @Test
    fun handleTextChangeNewlineOnActionKeepsSingleNewline() {
        val oldText = "INT. ARCHIVE - NIGHT\n\nTorches flicker along stone."
        val newText = oldText + "\n"
        val result = controller.handleTextChange(oldText, newText, newText.length)
        assertEquals("INT. ARCHIVE - NIGHT\n\nTorches flicker along stone.\n", result.newText)
    }

    @Test
    fun handleTextChangeNewlineOnDialogueMovesToNewActionBlock() {
        val oldText = "INT. ARCHIVE - NIGHT\n\n                      SESHAT\n          We must go."
        val newText = oldText + "\n"
        val result = controller.handleTextChange(oldText, newText, newText.length)
        assertEquals("INT. ARCHIVE - NIGHT\n\n                      SESHAT\n          We must go.\n\n", result.newText)
    }

    @Test
    fun handleTextChangeNewlineOnSceneHeadingAddsOneBlankLine() {
        val oldText = "int. library - night"
        val newText = oldText + "\n"
        val result = controller.handleTextChange(oldText, newText, newText.length)
        assertEquals("INT. LIBRARY - NIGHT\n\n", result.newText)
    }
}
