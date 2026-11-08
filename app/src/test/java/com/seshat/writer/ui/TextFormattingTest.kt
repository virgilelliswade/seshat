package com.seshat.writer.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class TextFormattingTest {

    @Test
    fun testWrapUnselectedAtCursor() {
        // Cursor at index 5 in "Hello"
        val value = TextFieldValue(text = "Hello", selection = TextRange(5))
        val result = TextFormattingHelper.toggleFormatting(value, "**")
        assertEquals("Hello****", result.text)
        assertEquals(TextRange(7), result.selection)
    }

    @Test
    fun testUnwrapCollapsedBetweenDelimiters() {
        // Cursor between "****" (index 2)
        val value = TextFieldValue(text = "Hello****", selection = TextRange(7))
        val result = TextFormattingHelper.toggleFormatting(value, "**")
        assertEquals("Hello", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun testWrapSelectedText() {
        // "Hello world" with "world" selected (indices 6 to 11)
        val value = TextFieldValue(text = "Hello world", selection = TextRange(6, 11))
        val result = TextFormattingHelper.toggleFormatting(value, "**")
        assertEquals("Hello **world**", result.text)
        assertEquals(TextRange(8, 13), result.selection)
    }

    @Test
    fun testUnwrapWhenSelectionIncludesDelimiters() {
        // Selection includes "**world**"
        val value = TextFieldValue(text = "Hello **world**", selection = TextRange(6, 15))
        val result = TextFormattingHelper.toggleFormatting(value, "**")
        assertEquals("Hello world", result.text)
        assertEquals(TextRange(6, 11), result.selection)
    }

    @Test
    fun testUnwrapWhenSelectionIsInsideDelimiters() {
        // "Hello **world**" with "world" selected (indices 8 to 13)
        val value = TextFieldValue(text = "Hello **world**", selection = TextRange(8, 13))
        val result = TextFormattingHelper.toggleFormatting(value, "**")
        assertEquals("Hello world", result.text)
        assertEquals(TextRange(6, 11), result.selection)
    }

    @Test
    fun testItalicAndUnderlineFormatting() {
        val italicVal = TextFieldValue(text = "Prose", selection = TextRange(0, 5))
        val italicResult = TextFormattingHelper.toggleFormatting(italicVal, "*")
        assertEquals("*Prose*", italicResult.text)

        val underlineVal = TextFieldValue(text = "Title", selection = TextRange(0, 5))
        val underlineResult = TextFormattingHelper.toggleFormatting(underlineVal, "_")
        assertEquals("_Title_", underlineResult.text)
    }
}
