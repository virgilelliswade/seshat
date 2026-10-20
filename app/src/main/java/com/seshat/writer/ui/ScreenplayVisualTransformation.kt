package com.seshat.writer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.seshat.writer.screenplay.FountainParser
import com.seshat.writer.screenplay.ScreenplayElement

class ScreenplayVisualTransformation(
    private val parser: FountainParser = FountainParser()
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val builder = AnnotatedString.Builder(raw)
        val elements = parser.parse(raw)
        val lines = raw.lines()

        // Track byte offset per line
        val lineOffsets = mutableListOf<Pair<Int, Int>>()
        var curOffset = 0
        for (line in lines) {
            val start = curOffset
            val end = curOffset + line.length
            lineOffsets.add(Pair(start, end))
            curOffset = end + 1 // +1 for '\n'
        }

        for (element in elements) {
            val lineIdx = when (element) {
                is ScreenplayElement.SceneHeading -> element.lineNumber
                is ScreenplayElement.Action -> element.lineNumber
                is ScreenplayElement.Character -> element.lineNumber
                is ScreenplayElement.Parenthetical -> element.lineNumber
                is ScreenplayElement.Dialogue -> element.lineNumber
                is ScreenplayElement.Transition -> element.lineNumber
                is ScreenplayElement.SectionHeading -> element.lineNumber
                is ScreenplayElement.Synopsis -> element.lineNumber
            }

            if (lineIdx < 0 || lineIdx >= lineOffsets.size) continue
            val (start, end) = lineOffsets[lineIdx]
            if (end <= start) continue

            when (element) {
                is ScreenplayElement.SceneHeading -> {
                    builder.addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        ),
                        start,
                        end
                    )
                }
                is ScreenplayElement.Character -> {
                    builder.addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        ),
                        start,
                        end
                    )
                }
                is ScreenplayElement.Parenthetical -> {
                    builder.addStyle(
                        SpanStyle(color = Color(0xFF444444)),
                        start,
                        end
                    )
                }
                is ScreenplayElement.Dialogue -> {
                    builder.addStyle(
                        SpanStyle(color = Color(0xFF111111)),
                        start,
                        end
                    )
                }
                is ScreenplayElement.Transition -> {
                    builder.addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        ),
                        start,
                        end
                    )
                }
                is ScreenplayElement.Action -> {
                    builder.addStyle(
                        SpanStyle(color = Color(0xFF111111)),
                        start,
                        end
                    )
                }
                else -> {}
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
