package com.seshat.writer.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.seshat.writer.model.InlineFormatSpan
import com.seshat.writer.model.InlineFormatType

/**
 * Contiguous text segment with unified character formatting.
 */
data class FormattedRun(
    val text: String,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false
) {
    val isPlain: Boolean
        get() = !isBold && !isItalic && !isUnderline && !isStrikethrough
}

/**
 * VisualTransformation that renders rich character styles (Bold, Italic, Underline, Strikethrough)
 * in-place on pure plain-text without displaying any raw asterisks or delimiters.
 * Uses [OffsetMapping.Identity] for 100% glitch-free cursor positioning.
 */
class RichTextVisualTransformation(
    private val formats: List<InlineFormatSpan>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = RichTextHelper.toAnnotatedString(text.text, formats)
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

/**
 * Core utility for true WYSIWYG rich text formatting across Seshat (Word / Final Draft style).
 * Keeps raw text clean (free of markdown symbols) and maintains format spans.
 */
object RichTextHelper {

    /**
     * Converts clean plain-text and format spans into a styled Compose [AnnotatedString].
     */
    fun toAnnotatedString(text: String, formats: List<InlineFormatSpan>): AnnotatedString {
        if (text.isEmpty() || formats.isEmpty()) {
            return AnnotatedString(text)
        }

        val builder = AnnotatedString.Builder(text)
        val textLen = text.length

        for (span in formats) {
            val start = span.start.coerceIn(0, textLen)
            val end = span.end.coerceIn(0, textLen)
            if (start >= end) continue

            when (span.type) {
                InlineFormatType.BOLD -> {
                    builder.addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start,
                        end
                    )
                }
                InlineFormatType.ITALIC -> {
                    builder.addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        start,
                        end
                    )
                }
                InlineFormatType.UNDERLINE -> {
                    builder.addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        start,
                        end
                    )
                }
                InlineFormatType.STRIKETHROUGH -> {
                    builder.addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        start,
                        end
                    )
                }
            }
        }

        return builder.toAnnotatedString()
    }

    /**
     * Toggles an [InlineFormatType] on the specified [selection] or current word boundary.
     * If the target range already has the style throughout, the style is removed (toggled off).
     * Otherwise, the style is applied to the range.
     */
    fun toggleSpan(
        text: String,
        formats: List<InlineFormatSpan>,
        selection: TextRange,
        type: InlineFormatType
    ): List<InlineFormatSpan> {
        val range = if (!selection.collapsed) {
            TextRange(selection.min.coerceIn(0, text.length), selection.max.coerceIn(0, text.length))
        } else {
            findWordBoundaries(text, selection.start)
        }

        val targetStart = range.min
        val targetEnd = range.max
        if (targetStart >= targetEnd) return formats

        val sameTypeSpans = formats.filter { it.type == type }
        val otherTypeSpans = formats.filter { it.type != type }

        // Check if the entire target range is already covered by sameTypeSpans
        val isFullyCovered = isRangeCovered(sameTypeSpans, targetStart, targetEnd)

        val updatedSameType = if (isFullyCovered) {
            // Remove / punch out the target range
            removeRangeFromSpans(sameTypeSpans, targetStart, targetEnd)
        } else {
            // Add target range and merge overlapping/adjacent spans
            val merged = sameTypeSpans + InlineFormatSpan(targetStart, targetEnd, type)
            mergeSpans(merged)
        }

        return (otherTypeSpans + updatedSameType).sortedWith(compareBy({ it.start }, { it.end }))
    }

    /**
     * Adjusts format spans when text changes (typing or deleting).
     */
    fun adjustSpansForEdit(
        oldText: String,
        newText: String,
        oldFormats: List<InlineFormatSpan>,
        cursor: Int = -1
    ): List<InlineFormatSpan> {
        if (oldFormats.isEmpty()) return emptyList()
        val delta = newText.length - oldText.length
        if (delta == 0) return oldFormats

        val maxLen = newText.length
        val adjusted = mutableListOf<InlineFormatSpan>()

        // Find common prefix to detect edit location
        var prefix = 0
        while (prefix < oldText.length && prefix < newText.length && oldText[prefix] == newText[prefix]) {
            prefix++
        }

        if (delta > 0) {
            // Insertion of delta characters
            val insertPoint = if (cursor in delta..newText.length) {
                cursor - delta
            } else {
                prefix
            }

            for (span in oldFormats) {
                when {
                    span.end < insertPoint -> {
                        // Entirely before insertion
                        adjusted.add(span)
                    }
                    span.start >= insertPoint -> {
                        // Entirely after or at insertion point -> shift right
                        val newStart = (span.start + delta).coerceIn(0, maxLen)
                        val newEnd = (span.end + delta).coerceIn(0, maxLen)
                        if (newStart < newEnd) {
                            adjusted.add(span.copy(start = newStart, end = newEnd))
                        }
                    }
                    else -> {
                        // Span encloses insertion point -> expand span
                        val newEnd = (span.end + delta).coerceIn(0, maxLen)
                        if (span.start < newEnd) {
                            adjusted.add(span.copy(end = newEnd))
                        }
                    }
                }
            }
        } else {
            // Deletion of (-delta) characters
            val deleteCount = -delta
            val deleteStart = if (cursor in 0..oldText.length && cursor != -1) {
                cursor
            } else {
                prefix
            }
            val deleteEnd = (deleteStart + deleteCount).coerceIn(0, oldText.length)

            for (span in oldFormats) {
                when {
                    span.end <= deleteStart -> {
                        adjusted.add(span)
                    }
                    span.start >= deleteEnd -> {
                        val newStart = (span.start - deleteCount).coerceIn(0, maxLen)
                        val newEnd = (span.end - deleteCount).coerceIn(0, maxLen)
                        if (newStart < newEnd) {
                            adjusted.add(span.copy(start = newStart, end = newEnd))
                        }
                    }
                    else -> {
                        // Span overlaps deletion
                        val newStart = span.start.coerceAtMost(deleteStart).coerceIn(0, maxLen)
                        val remainingRight = (span.end - deleteEnd).coerceAtLeast(0)
                        val newEnd = (newStart + (deleteStart - span.start).coerceAtLeast(0) + remainingRight).coerceIn(0, maxLen)
                        if (newStart < newEnd) {
                            adjusted.add(span.copy(start = newStart, end = newEnd))
                        }
                    }
                }
            }
        }

        return mergeAllTypes(adjusted)
    }

    fun adjustSpansForEdit(
        oldText: String,
        newText: String,
        spans: List<InlineFormatSpan>
    ): List<InlineFormatSpan> = adjustSpansForEdit(oldText, newText, spans, -1)

    /**
     * Resolves clean text and format spans. If legacy rawText contains markdown delimiters,
     * strips them, converts to clean text, and extracts the format spans.
     */
    fun getCleanTextAndFormats(
        rawText: String,
        existingFormats: List<InlineFormatSpan>
    ): Pair<String, List<InlineFormatSpan>> {
        if (!hasMarkdownDelimiters(rawText)) {
            return Pair(rawText, existingFormats)
        }

        val (cleanText, extractedSpans) = parseAndStripMarkdownTokens(rawText)
        val combined = (existingFormats + extractedSpans)
        return Pair(cleanText, mergeAllTypes(combined))
    }

    /**
     * Parses legacy markdown symbols (`***`, `**`, `*`, `_`, `~~`) from [rawText],
     * strips the symbols, and returns the clean string along with the resulting spans.
     */
    fun parseAndStripMarkdownTokens(rawText: String): Pair<String, List<InlineFormatSpan>> {
        if (rawText.isEmpty() || !hasMarkdownDelimiters(rawText)) {
            return Pair(rawText, emptyList())
        }

        // Tokenize and build clean string while recording spans
        val cleanBuilder = java.lang.StringBuilder()
        val spans = mutableListOf<InlineFormatSpan>()

        var i = 0
        val len = rawText.length

        while (i < len) {
            // Check for Bold+Italic (***text***)
            if (rawText.startsWith("***", i)) {
                val closeIdx = rawText.indexOf("***", i + 3)
                if (closeIdx != -1) {
                    val inner = rawText.substring(i + 3, closeIdx)
                    val start = cleanBuilder.length
                    cleanBuilder.append(inner)
                    val end = cleanBuilder.length
                    spans.add(InlineFormatSpan(start, end, InlineFormatType.BOLD))
                    spans.add(InlineFormatSpan(start, end, InlineFormatType.ITALIC))
                    i = closeIdx + 3
                    continue
                }
            }

            // Check for Bold (**text**)
            if (rawText.startsWith("**", i)) {
                val closeIdx = rawText.indexOf("**", i + 2)
                if (closeIdx != -1) {
                    val inner = rawText.substring(i + 2, closeIdx)
                    val start = cleanBuilder.length
                    cleanBuilder.append(inner)
                    val end = cleanBuilder.length
                    spans.add(InlineFormatSpan(start, end, InlineFormatType.BOLD))
                    i = closeIdx + 2
                    continue
                }
            }

            // Check for Strikethrough (~~text~~)
            if (rawText.startsWith("~~", i)) {
                val closeIdx = rawText.indexOf("~~", i + 2)
                if (closeIdx != -1) {
                    val inner = rawText.substring(i + 2, closeIdx)
                    val start = cleanBuilder.length
                    cleanBuilder.append(inner)
                    val end = cleanBuilder.length
                    spans.add(InlineFormatSpan(start, end, InlineFormatType.STRIKETHROUGH))
                    i = closeIdx + 2
                    continue
                }
            }

            // Check for Underline (_text_)
            if (rawText[i] == '_' && i + 1 < len && !rawText[i + 1].isWhitespace()) {
                val closeIdx = rawText.indexOf('_', i + 1)
                if (closeIdx != -1 && closeIdx > i + 1 && !rawText[closeIdx - 1].isWhitespace()) {
                    val inner = rawText.substring(i + 1, closeIdx)
                    val start = cleanBuilder.length
                    cleanBuilder.append(inner)
                    val end = cleanBuilder.length
                    spans.add(InlineFormatSpan(start, end, InlineFormatType.UNDERLINE))
                    i = closeIdx + 1
                    continue
                }
            }

            // Check for Italic (*text*)
            if (rawText[i] == '*' && i + 1 < len && !rawText[i + 1].isWhitespace()) {
                val closeIdx = rawText.indexOf('*', i + 1)
                if (closeIdx != -1 && closeIdx > i + 1 && !rawText[closeIdx - 1].isWhitespace()) {
                    val inner = rawText.substring(i + 1, closeIdx)
                    val start = cleanBuilder.length
                    cleanBuilder.append(inner)
                    val end = cleanBuilder.length
                    spans.add(InlineFormatSpan(start, end, InlineFormatType.ITALIC))
                    i = closeIdx + 1
                    continue
                }
            }

            // Plain character
            cleanBuilder.append(rawText[i])
            i++
        }

        return Pair(cleanBuilder.toString(), mergeAllTypes(spans))
    }

    /**
     * Breaks text and format spans into non-overlapping contiguous styled runs for PDF / FDX export.
     */
    fun toFormattedRuns(text: String, formats: List<InlineFormatSpan>): List<FormattedRun> {
        if (text.isEmpty()) return emptyList()
        if (formats.isEmpty()) return listOf(FormattedRun(text))

        val textLen = text.length
        // Collect split boundaries
        val boundaries = sortedSetOf<Int>()
        boundaries.add(0)
        boundaries.add(textLen)

        for (span in formats) {
            boundaries.add(span.start.coerceIn(0, textLen))
            boundaries.add(span.end.coerceIn(0, textLen))
        }

        val points = boundaries.toList()
        val runs = mutableListOf<FormattedRun>()

        for (idx in 0 until points.size - 1) {
            val rStart = points[idx]
            val rEnd = points[idx + 1]
            if (rStart >= rEnd) continue

            val segmentText = text.substring(rStart, rEnd)
            val isBold = formats.any { it.type == InlineFormatType.BOLD && it.start <= rStart && it.end >= rEnd }
            val isItalic = formats.any { it.type == InlineFormatType.ITALIC && it.start <= rStart && it.end >= rEnd }
            val isUnderline = formats.any { it.type == InlineFormatType.UNDERLINE && it.start <= rStart && it.end >= rEnd }
            val isStrikethrough = formats.any { it.type == InlineFormatType.STRIKETHROUGH && it.start <= rStart && it.end >= rEnd }

            runs.add(
                FormattedRun(
                    text = segmentText,
                    isBold = isBold,
                    isItalic = isItalic,
                    isUnderline = isUnderline,
                    isStrikethrough = isStrikethrough
                )
            )
        }

        // Merge adjacent runs with identical styling
        if (runs.size <= 1) return runs
        val coalesced = mutableListOf<FormattedRun>()
        var current = runs[0]

        for (k in 1 until runs.size) {
            val next = runs[k]
            if (current.isBold == next.isBold &&
                current.isItalic == next.isItalic &&
                current.isUnderline == next.isUnderline &&
                current.isStrikethrough == next.isStrikethrough
            ) {
                current = current.copy(text = current.text + next.text)
            } else {
                coalesced.add(current)
                current = next
            }
        }
        coalesced.add(current)

        return coalesced
    }

    /**
     * Slices and shifts format spans for a wrapped line within `[rangeStart, rangeEnd)`.
     */
    fun sliceFormatsForRange(
        formats: List<InlineFormatSpan>,
        rangeStart: Int,
        rangeEnd: Int
    ): List<InlineFormatSpan> {
        if (formats.isEmpty() || rangeStart >= rangeEnd) return emptyList()

        val sliced = mutableListOf<InlineFormatSpan>()
        val lineLen = rangeEnd - rangeStart

        for (span in formats) {
            if (span.end <= rangeStart || span.start >= rangeEnd) continue

            val clampedStart = (span.start - rangeStart).coerceIn(0, lineLen)
            val clampedEnd = (span.end - rangeStart).coerceIn(0, lineLen)
            if (clampedStart < clampedEnd) {
                sliced.add(span.copy(start = clampedStart, end = clampedEnd))
            }
        }

        return sliced
    }

    /**
     * Converts clean text and format spans to a Fountain-compatible string with markdown delimiters
     * for standard .fountain script file export.
     */
    fun toFountainString(text: String, formats: List<InlineFormatSpan>): String {
        if (text.isEmpty() || formats.isEmpty()) return text
        val runs = toFormattedRuns(text, formats)
        val sb = java.lang.StringBuilder()

        for (run in runs) {
            val pre = java.lang.StringBuilder()
            val post = java.lang.StringBuilder()

            if (run.isUnderline) { pre.append("_"); post.insert(0, "_") }
            if (run.isBold && run.isItalic) {
                pre.append("***"); post.insert(0, "***")
            } else if (run.isBold) {
                pre.append("**"); post.insert(0, "**")
            } else if (run.isItalic) {
                pre.append("*"); post.insert(0, "*")
            }
            if (run.isStrikethrough) { pre.append("~~"); post.insert(0, "~~") }

            sb.append(pre).append(run.text).append(post)
        }

        return sb.toString()
    }

    /**
     * Finds word boundaries around the given [cursor] position.
     */
    fun findWordBoundaries(text: String, cursor: Int): TextRange {
        if (text.isEmpty()) return TextRange.Zero
        val c = cursor.coerceIn(0, text.length)

        var start = c
        while (start > 0 && isWordChar(text[start - 1])) {
            start--
        }

        var end = c
        while (end < text.length && isWordChar(text[end])) {
            end++
        }

        return TextRange(start, end)
    }

    private fun isWordChar(ch: Char): Boolean {
        return ch.isLetterOrDigit() || ch == '\'' || ch == '’'
    }

    private fun hasMarkdownDelimiters(text: String): Boolean {
        return text.contains("**") || text.contains("~~") ||
            (text.contains('*') && !text.contains("* * *")) ||
            text.contains('_')
    }

    private fun isRangeCovered(spans: List<InlineFormatSpan>, start: Int, end: Int): Boolean {
        if (spans.isEmpty() || start >= end) return false
        val overlapping = spans.filter { it.overlaps(start, end) }.sortedBy { it.start }
        if (overlapping.isEmpty()) return false

        var cur = start
        for (span in overlapping) {
            if (span.start > cur) return false
            cur = cur.coerceAtLeast(span.end)
            if (cur >= end) return true
        }
        return cur >= end
    }

    private fun removeRangeFromSpans(
        spans: List<InlineFormatSpan>,
        targetStart: Int,
        targetEnd: Int
    ): List<InlineFormatSpan> {
        val result = mutableListOf<InlineFormatSpan>()
        for (s in spans) {
            when {
                s.end <= targetStart || s.start >= targetEnd -> {
                    // Strictly outside
                    result.add(s)
                }
                s.start < targetStart && s.end > targetEnd -> {
                    // Target is in middle -> split into two
                    result.add(s.copy(end = targetStart))
                    result.add(s.copy(start = targetEnd))
                }
                s.start < targetStart && s.end <= targetEnd -> {
                    result.add(s.copy(end = targetStart))
                }
                s.start >= targetStart && s.end > targetEnd -> {
                    result.add(s.copy(start = targetEnd))
                }
                else -> {
                    // Completely covered by target -> remove
                }
            }
        }
        return result.filter { it.start < it.end }
    }

    private fun mergeSpans(spans: List<InlineFormatSpan>): List<InlineFormatSpan> {
        if (spans.size <= 1) return spans
        val sorted = spans.sortedWith(compareBy({ it.start }, { it.end }))
        val merged = mutableListOf<InlineFormatSpan>()
        var current = sorted[0]

        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (next.start <= current.end) {
                // Overlapping or adjacent
                current = current.copy(end = maxOf(current.end, next.end))
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }

    private fun mergeAllTypes(spans: List<InlineFormatSpan>): List<InlineFormatSpan> {
        val byType = spans.groupBy { it.type }
        val merged = mutableListOf<InlineFormatSpan>()
        for ((_, list) in byType) {
            merged.addAll(mergeSpans(list))
        }
        return merged.sortedWith(compareBy({ it.start }, { it.end }))
    }
}
