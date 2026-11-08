package com.seshat.writer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em

/**
 * VisualTransformation that renders inline Fountain / Markdown formatting in real-time
 * without modifying character count or raw offsets:
 * - Bold (`**text**`)
 * - Italic (`*text*`)
 * - Underline (`_text_`)
 * - Bold + Italic (`***text***`)
 * - Combinations (`_**bold underline**_`, `_***all three***_`)
 * - Markdown Headings (`# Heading 1`, `## Heading 2`, `### Heading 3`)
 *
 * Uses [OffsetMapping.Identity] so cursor positioning, IME text composition,
 * selection drags, and backspace remain 100% glitch-free across Android & Desktop.
 */
class FountainInlineVisualTransformation(
    private val isDark: Boolean = true
) : VisualTransformation {

    private val syntaxColor = if (isDark) {
        Color(0xFF64748B).copy(alpha = 0.55f)
    } else {
        Color(0xFF94A3B8).copy(alpha = 0.70f)
    }

    private val tripleStarRegex = Regex("""\*\*\*([^\*\n]+?)\*\*\*""")
    private val doubleStarRegex = Regex("""(?<!\*)\*\*([^\*\n]+?)\*\*(?!\*)""")
    private val singleStarRegex = Regex("""(?<!\*)\*([^\*\n]+?)\*(?!\*)""")
    private val underlineRegex = Regex("""(?<!_)_([^_\n]+?)_(?!_)""")
    private val headingRegex = Regex("""^(#{1,3})\s+(.*)$""", RegexOption.MULTILINE)

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val builder = AnnotatedString.Builder(text)

        // 1. Heading markup (# H1, ## H2, ### H3)
        for (match in headingRegex.findAll(raw)) {
            val fullRange = match.range
            val hashes = match.groupValues[1]
            val hashEnd = fullRange.first + hashes.length + 1 // including the trailing space

            builder.addStyle(
                SpanStyle(color = syntaxColor),
                fullRange.first,
                hashEnd.coerceAtMost(raw.length)
            )

            val scale = when (hashes.length) {
                1 -> 1.35.em
                2 -> 1.22.em
                else -> 1.12.em
            }
            builder.addStyle(
                SpanStyle(
                    fontSize = scale,
                    fontWeight = FontWeight.Bold
                ),
                hashEnd.coerceAtMost(raw.length),
                fullRange.last + 1
            )
        }

        // 2. Underline: _text_
        for (match in underlineRegex.findAll(raw)) {
            val range = match.range
            val start = range.first
            val end = range.last + 1

            // Dim opening and closing delimiters
            builder.addStyle(SpanStyle(color = syntaxColor), start, start + 1)
            builder.addStyle(SpanStyle(color = syntaxColor), end - 1, end)

            // Content
            builder.addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline),
                start + 1,
                end - 1
            )
        }

        // 3. Bold + Italic: ***text***
        for (match in tripleStarRegex.findAll(raw)) {
            val range = match.range
            val start = range.first
            val end = range.last + 1

            builder.addStyle(SpanStyle(color = syntaxColor), start, start + 3)
            builder.addStyle(SpanStyle(color = syntaxColor), end - 3, end)

            builder.addStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                start + 3,
                end - 3
            )
        }

        // 4. Bold: **text**
        for (match in doubleStarRegex.findAll(raw)) {
            val range = match.range
            val start = range.first
            val end = range.last + 1

            builder.addStyle(SpanStyle(color = syntaxColor), start, start + 2)
            builder.addStyle(SpanStyle(color = syntaxColor), end - 2, end)

            builder.addStyle(
                SpanStyle(fontWeight = FontWeight.Bold),
                start + 2,
                end - 2
            )
        }

        // 5. Italic: *text*
        for (match in singleStarRegex.findAll(raw)) {
            val range = match.range
            val start = range.first
            val end = range.last + 1

            builder.addStyle(SpanStyle(color = syntaxColor), start, start + 1)
            builder.addStyle(SpanStyle(color = syntaxColor), end - 1, end)

            builder.addStyle(
                SpanStyle(fontStyle = FontStyle.Italic),
                start + 1,
                end - 1
            )
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
