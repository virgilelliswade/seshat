package com.seshat.writer.model

/**
 * Types of inline character formatting supported across all Seshat editors.
 */
enum class InlineFormatType {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH
}

/**
 * Represents a rich text format span over a half-open character range `[start, end)`
 * on a clean plain-text string (with zero asterisks or markdown symbols).
 */
data class InlineFormatSpan(
    val start: Int,
    val end: Int,
    val type: InlineFormatType
) {
    init {
        require(start <= end) { "start ($start) must be <= end ($end)" }
    }

    val length: Int get() = end - start

    fun overlaps(otherStart: Int, otherEnd: Int): Boolean {
        return start < otherEnd && otherStart < end
    }

    fun contains(index: Int): Boolean {
        return index in start until end
    }
}
