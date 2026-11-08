package com.seshat.writer.ui

import androidx.compose.ui.text.TextRange
import com.seshat.writer.model.InlineFormatSpan
import com.seshat.writer.model.InlineFormatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextFormattingTest {

    @Test
    fun testParseAndStripMarkdownBoldAndItalic() {
        val raw = "Hello **world** and *everyone*!"
        val (clean, spans) = RichTextHelper.parseAndStripMarkdownTokens(raw)
        assertEquals("Hello world and everyone!", clean)
        assertEquals(2, spans.size)

        val boldSpan = spans.first { it.type == InlineFormatType.BOLD }
        assertEquals(6, boldSpan.start)
        assertEquals(11, boldSpan.end)

        val italicSpan = spans.first { it.type == InlineFormatType.ITALIC }
        assertEquals(16, italicSpan.start)
        assertEquals(24, italicSpan.end)
    }

    @Test
    fun testParseAndStripUnderlineAndStrikethrough() {
        val raw = "Important: _read this_ and ~~delete this~~ immediately."
        val (clean, spans) = RichTextHelper.parseAndStripMarkdownTokens(raw)
        assertEquals("Important: read this and delete this immediately.", clean)

        val underlineSpan = spans.first { it.type == InlineFormatType.UNDERLINE }
        assertEquals("read this", clean.substring(underlineSpan.start, underlineSpan.end))

        val strikeSpan = spans.first { it.type == InlineFormatType.STRIKETHROUGH }
        assertEquals("delete this", clean.substring(strikeSpan.start, strikeSpan.end))
    }

    @Test
    fun testParseAndStripTripleAsteriskBoldItalic() {
        val raw = "This is ***critical*** news."
        val (clean, spans) = RichTextHelper.parseAndStripMarkdownTokens(raw)
        assertEquals("This is critical news.", clean)
        assertTrue(spans.any { it.type == InlineFormatType.BOLD && clean.substring(it.start, it.end) == "critical" })
        assertTrue(spans.any { it.type == InlineFormatType.ITALIC && clean.substring(it.start, it.end) == "critical" })
    }

    @Test
    fun testToggleSpanOnSelectedText() {
        val text = "EXT. ABANDONED WAREHOUSE - NIGHT"
        val selection = TextRange(5, 24) // "ABANDONED WAREHOUSE"
        val spans = RichTextHelper.toggleSpan(text, emptyList(), selection, InlineFormatType.BOLD)

        assertEquals(1, spans.size)
        assertEquals(5, spans[0].start)
        assertEquals(24, spans[0].end)
        assertEquals(InlineFormatType.BOLD, spans[0].type)

        // Toggling again should remove it
        val untoggled = RichTextHelper.toggleSpan(text, spans, selection, InlineFormatType.BOLD)
        assertTrue(untoggled.isEmpty())
    }

    @Test
    fun testToggleSpanOnWordBoundaryAtCursor() {
        val text = "A mysterious detective walks in."
        // Cursor inside "detective" (index 15)
        val selection = TextRange(15)
        val spans = RichTextHelper.toggleSpan(text, emptyList(), selection, InlineFormatType.ITALIC)

        assertEquals(1, spans.size)
        assertEquals(InlineFormatType.ITALIC, spans[0].type)
        assertEquals("detective", text.substring(spans[0].start, spans[0].end))
    }

    @Test
    fun testAdjustSpansOnTyping() {
        val text = "The dog barks."
        val initialSpans = listOf(InlineFormatSpan(4, 7, InlineFormatType.BOLD)) // "dog"

        // Type "big " before "dog" at index 4 (length delta +4)
        val shifted = RichTextHelper.adjustSpansForEdit(
            oldText = text,
            newText = "The big dog barks.",
            spans = initialSpans
        )
        assertEquals(1, shifted.size)
        assertEquals(8, shifted[0].start)
        assertEquals(11, shifted[0].end)
    }

    @Test
    fun testAdjustSpansOnBackspace() {
        val text = "The big dog barks."
        val initialSpans = listOf(InlineFormatSpan(8, 11, InlineFormatType.BOLD)) // "dog"

        // Delete "big " (length delta -4) at index 4
        val shifted = RichTextHelper.adjustSpansForEdit(
            oldText = text,
            newText = "The dog barks.",
            spans = initialSpans
        )
        assertEquals(1, shifted.size)
        assertEquals(4, shifted[0].start)
        assertEquals(7, shifted[0].end)
    }

    @Test
    fun testToFormattedRuns() {
        val text = "The quick brown fox"
        // Bold "quick" (4..9), Italic "brown" (10..15)
        val spans = listOf(
            InlineFormatSpan(4, 9, InlineFormatType.BOLD),
            InlineFormatSpan(10, 15, InlineFormatType.ITALIC)
        )
        val runs = RichTextHelper.toFormattedRuns(text, spans)

        assertEquals(5, runs.size)
        assertEquals("The ", runs[0].text)
        assertFalse(runs[0].isBold)

        assertEquals("quick", runs[1].text)
        assertTrue(runs[1].isBold)
        assertFalse(runs[1].isItalic)

        assertEquals(" ", runs[2].text)
        assertFalse(runs[2].isBold)

        assertEquals("brown", runs[3].text)
        assertTrue(runs[3].isItalic)
        assertFalse(runs[3].isBold)

        assertEquals(" fox", runs[4].text)
        assertFalse(runs[4].isBold)
        assertFalse(runs[4].isItalic)
    }

    @Test
    fun testFountainExportAndImportRoundTrip() {
        val cleanText = "He pulls the trigger."
        val spans = listOf(
            InlineFormatSpan(13, 20, InlineFormatType.BOLD) // "trigger"
        )
        val fountainStr = RichTextHelper.toFountainString(cleanText, spans)
        assertEquals("He pulls the **trigger**.", fountainStr)

        val (restoredText, restoredSpans) = RichTextHelper.parseAndStripMarkdownTokens(fountainStr)
        assertEquals(cleanText, restoredText)
        assertEquals(1, restoredSpans.size)
        assertEquals(13, restoredSpans[0].start)
        assertEquals(20, restoredSpans[0].end)
        assertEquals(InlineFormatType.BOLD, restoredSpans[0].type)
    }
}
