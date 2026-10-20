package com.seshat.writer.screenplay

import java.util.Locale

data class KeyTransitionResult(
    val newText: String,
    val newCursor: Int
)

class ScreenplayKeyboardController(
    private val parser: FountainParser = FountainParser()
) {

    var activeCharacterLineStart: Int? = null

    companion object {
        const val CHARACTER_INDENT = "                      " // 22 spaces = 2.2" from 1.5" margin (Courier 10 cpi)
        const val PARENTHETICAL_INDENT = "                "   // 16 spaces = 1.6" from 1.5" margin
        const val DIALOGUE_INDENT = "          "              // 10 spaces = 1.0" from 1.5" margin
        val CHARACTER_EXTENSIONS = listOf("(V.O.)", "(O.S.)", "(O.C.)", "(PRE-LAP)")
        val TIME_OF_DAY_COMPLETIONS = listOf("- DAY", "- NIGHT", "- DUSK", "- DAWN", "- CONTINUOUS", "- LATER", "- SAME")
    }

    fun handleEnter(
        text: String,
        cursor: Int,
        knownCharacters: List<String> = emptyList()
    ): KeyTransitionResult {
        val safeCursor = cursor.coerceIn(0, text.length)
        val before = text.substring(0, safeCursor)
        val after = text.substring(safeCursor)

        val lastNewline = before.lastIndexOf('\n')
        val lineStart = if (lastNewline >= 0) lastNewline + 1 else 0
        val rawCurrentLine = before.substring(lineStart)
        val currentLine = rawCurrentLine.trim()
        val textBeforeCurrentLine = before.substring(0, lineStart)

        // 1. Check FADE IN: special rule
        if (isFadeIn(currentLine)) {
            activeCharacterLineStart = null
            val formatted = "FADE IN:\n\nINT. "
            val newText = textBeforeCurrentLine + formatted + after
            return KeyTransitionResult(newText, textBeforeCurrentLine.length + formatted.length)
        }

        // 2. Check Scene Heading (force ALL CAPS, 1 blank line before action)
        if (isSceneHeading(currentLine)) {
            activeCharacterLineStart = null
            val upperHeading = currentLine.uppercase(Locale.US)
            val formatted = "$upperHeading\n\n"
            val newText = textBeforeCurrentLine + formatted + after
            return KeyTransitionResult(newText, textBeforeCurrentLine.length + formatted.length)
        }

        val elements = parser.parse(before)

        // 3. Check Character (Zero blank lines -> immediate next line is dialogue with 10 spaces)
        val isChar = isCharacterTransition(rawCurrentLine, textBeforeCurrentLine, elements, lineStart, knownCharacters)
        if (isChar) {
            activeCharacterLineStart = null
            val upperLine = currentLine.uppercase(Locale.US)
            val formattedCharacter = formatCharacterWithContd(upperLine, textBeforeCurrentLine)
            val charIndent = rawCurrentLine.takeWhile { it == ' ' }
            val formatted = "$charIndent$formattedCharacter\n$DIALOGUE_INDENT"
            val newText = textBeforeCurrentLine + formatted + after
            return KeyTransitionResult(newText, textBeforeCurrentLine.length + formatted.length)
        }

        // 4. Check Parenthetical (Zero blank lines -> immediate next line is dialogue with 10 spaces)
        if (isParentheticalLine(currentLine)) {
            activeCharacterLineStart = null
            val lowerLine = autoLowercaseParenthetical(currentLine)
            val closedLine = if (lowerLine.endsWith(")")) lowerLine else "$lowerLine)"
            val parenIndent = rawCurrentLine.takeWhile { it == ' ' }
            val formatted = "$parenIndent$closedLine\n$DIALOGUE_INDENT"
            val newText = textBeforeCurrentLine + formatted + after
            return KeyTransitionResult(newText, textBeforeCurrentLine.length + formatted.length)
        }

        // 5. Check Dialogue (Enter from dialogue -> moves to new Action block with 1 blank line)
        val isDial = isDialogueLine(rawCurrentLine, textBeforeCurrentLine, knownCharacters)
        if (isDial) {
            activeCharacterLineStart = null
            val casedCurrent = autoCapitalizeSentences(rawCurrentLine)
            val formatted = "$casedCurrent\n\n"
            val newText = textBeforeCurrentLine + formatted + after
            return KeyTransitionResult(newText, textBeforeCurrentLine.length + formatted.length)
        }

        // 6. Action or generic line (single newline, zero blank lines)
        activeCharacterLineStart = null
        val casedCurrent = applyLineCasing(rawCurrentLine, textBeforeCurrentLine, knownCharacters, lineStart)
        val formatted = "$casedCurrent\n"
        val newText = textBeforeCurrentLine + formatted + after
        val newCursor = textBeforeCurrentLine.length + formatted.length
        return KeyTransitionResult(newText, newCursor)
    }

    fun handleTab(
        text: String,
        cursor: Int,
        knownCharacters: List<String> = emptyList()
    ): KeyTransitionResult {
        val safeCursor = cursor.coerceIn(0, text.length)
        val before = text.substring(0, safeCursor)
        val after = text.substring(safeCursor)

        val lastNewline = before.lastIndexOf('\n')
        val lineStart = if (lastNewline >= 0) lastNewline + 1 else 0
        val rawCurrentLine = before.substring(lineStart)
        val currentLine = rawCurrentLine.trim()
        val textBeforeCurrentLine = before.substring(0, lineStart)

        val elements = parser.parse(before)
        val isChar = isCharacterTransition(rawCurrentLine, textBeforeCurrentLine, elements, lineStart, knownCharacters)
        val isDial = isDialogueLine(rawCurrentLine, textBeforeCurrentLine, knownCharacters)

        // 1. From Character or Dialogue: Press Tab -> Inserts Parenthetical on immediate next line (\n + PARENTHETICAL_INDENT + "()")
        if (isChar || isDial) {
            val insertion = "\n$PARENTHETICAL_INDENT()"
            val newText = before + insertion + after
            val newCursor = safeCursor + insertion.length - 1 // Inside ()
            activeCharacterLineStart = null
            return KeyTransitionResult(newText, newCursor)
        }

        // 2. Tab on Parenthetical -> Dialogue on immediate next line (\n + DIALOGUE_INDENT)
        if (isParentheticalLine(currentLine)) {
            val insertion = "\n$DIALOGUE_INDENT"
            val newText = before + insertion + after
            activeCharacterLineStart = null
            return KeyTransitionResult(newText, safeCursor + insertion.length)
        }

        // 3. Tab on Action or Blank -> Character line (Inserts 22 spaces character indent and jumps cursor immediately)
        val separator = when {
            before.isEmpty() -> ""
            before.endsWith("\n\n") -> ""
            before.endsWith("\n") -> "\n"
            else -> "\n\n"
        }
        val insertion = separator + CHARACTER_INDENT
        val newText = before + insertion + after
        val newCursor = safeCursor + insertion.length
        activeCharacterLineStart = safeCursor + separator.length
        return KeyTransitionResult(newText, newCursor)
    }

    fun isCharacterTransition(
        rawLine: String,
        textBefore: String,
        elements: List<ScreenplayElement>,
        lineStart: Int = -1,
        knownCharacters: List<String> = emptyList()
    ): Boolean {
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty() || isSceneHeading(trimmed) || isFadeIn(trimmed) || isParentheticalLine(trimmed)) {
            return false
        }

        if (lineStart >= 0 && lineStart == activeCharacterLineStart) return true
        if (rawLine.takeWhile { it == ' ' }.length >= 15) return true
        if (trimmed.startsWith("@")) return true

        val clean = trimmed.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        if (knownCharacters.any { it.equals(clean, ignoreCase = true) }) return true

        val hasExt = CHARACTER_EXTENSIONS.any { trimmed.contains(it, ignoreCase = true) } || trimmed.contains("(CONT'D)", ignoreCase = true)
        val isAllUpper = clean.length in 1..40 && clean.all { it.isUpperCase() || it.isDigit() || it.isWhitespace() || it == '-' || it == '\'' || it == '.' }

        return hasExt || (isAllUpper && isPrecededByBlank(textBefore))
    }

    fun isParentheticalLine(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith("(") && (trimmed.endsWith(")") || !trimmed.contains(")"))
    }

    fun applyCharacterExtension(currentLine: String, extension: String): String {
        val base = currentLine.replace(Regex("\\s*\\(CONT'D\\)", RegexOption.IGNORE_CASE), "").trim()
        val hasContd = currentLine.contains("(CONT'D)", ignoreCase = true)

        var cleaned = base
        for (ext in CHARACTER_EXTENSIONS) {
            cleaned = cleaned.replace(ext, "").trim()
        }

        val withExt = "$cleaned $extension"
        return if (hasContd) "$withExt (CONT'D)" else withExt
    }

    fun cycleCharacterExtension(currentLine: String): String {
        val upper = currentLine.uppercase(Locale.US)
        val hasContd = upper.contains("(CONT'D)")
        val cleanNoContd = upper.replace(Regex("\\s*\\(CONT'D\\)"), "").trim()

        var existingExt: String? = null
        var base = cleanNoContd
        for (ext in CHARACTER_EXTENSIONS) {
            if (cleanNoContd.endsWith(ext)) {
                existingExt = ext
                base = cleanNoContd.substring(0, cleanNoContd.length - ext.length).trim()
                break
            }
        }

        val nextExt = when (existingExt) {
            null -> "(V.O.)"
            "(V.O.)" -> "(O.S.)"
            "(O.S.)" -> "(O.C.)"
            "(O.C.)" -> "(PRE-LAP)"
            "(PRE-LAP)" -> null
            else -> "(V.O.)"
        }

        val withExt = if (nextExt != null) "$base $nextExt" else base
        return if (hasContd) "$withExt (CONT'D)" else withExt
    }

    fun handleSpace(text: String, cursor: Int): KeyTransitionResult? {
        val safeCursor = cursor.coerceIn(0, text.length)
        val before = text.substring(0, safeCursor)

        val lastNewline = before.lastIndexOf('\n')
        val lineStart = if (lastNewline >= 0) lastNewline + 1 else 0
        val nextNewline = text.indexOf('\n', safeCursor)
        val lineEnd = if (nextNewline >= 0) nextNewline else text.length

        val rawLine = text.substring(lineStart, lineEnd)
        val currentLine = rawLine.trim()
        val textBeforeCurrentLine = text.substring(0, lineStart)
        val textAfterCurrentLine = text.substring(lineEnd)

        // Only trigger if on a character line and cursor is at end of current line
        if (!isCharacterLine(rawLine, textBeforeCurrentLine) || safeCursor < lineEnd) {
            return null
        }

        val indent = rawLine.takeWhile { it == ' ' }
        val updatedLine = indent + cycleCharacterExtension(currentLine)
        val newText = textBeforeCurrentLine + updatedLine + textAfterCurrentLine
        val newCursor = textBeforeCurrentLine.length + updatedLine.length
        return KeyTransitionResult(newText, newCursor)
    }

    fun isFadeIn(line: String): Boolean {
        val trimmed = line.trim().uppercase(Locale.US)
        return trimmed.startsWith("FADE IN:") || trimmed.startsWith("FADE IN")
    }

    fun isSceneHeading(line: String): Boolean {
        val upper = line.trim().uppercase(Locale.US)
        return upper.startsWith("INT.") ||
               upper.startsWith("EXT.") ||
               upper.startsWith("I/E.") ||
               upper.startsWith("INT/EXT.") ||
               upper.startsWith("INT./EXT.") ||
               upper.startsWith("EST.") ||
               upper.startsWith("INT ") ||
               upper.startsWith("EXT ") ||
               upper.startsWith("I/E ") ||
               upper.startsWith("INT/EXT ") ||
               upper.startsWith("INT./EXT ") ||
               upper.startsWith("INT-") ||
               upper.startsWith("EXT-") ||
               upper.startsWith("INT/") ||
               upper.startsWith("EXT/") ||
               upper.startsWith("OVER BLACK") ||
               (upper.startsWith(".") && upper.length > 1 && !upper.startsWith("..."))
    }

    fun isCharacterLine(line: String, textBefore: String = ""): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || isSceneHeading(trimmed) || isFadeIn(trimmed) || isParentheticalLine(trimmed)) return false
        val clean = trimmed.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        if (clean.isEmpty()) return false
        val leadingSpaces = line.takeWhile { it == ' ' }.length
        if (leadingSpaces >= 15) return true
        if (trimmed.startsWith("@")) return true
        return clean.length <= 40 && clean.all { it.isUpperCase() || it.isDigit() || it.isWhitespace() || it == '-' || it == '\'' || it == '.' }
    }

    fun formatCharacterWithContd(
        line: String,
        textBeforeLine: String = "",
        previousElements: List<ScreenplayElement> = emptyList()
    ): String {
        val upperLine = line.uppercase(Locale.US)
        val baseName = upperLine.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        val hasAlreadyContd = upperLine.contains("(CONT'D)")

        // Only parse elements that appeared BEFORE this character line
        val elementsBefore = if (textBeforeLine.isNotEmpty()) {
            parser.parse(textBeforeLine)
        } else {
            previousElements
        }

        val previousCharacters = elementsBefore.filterIsInstance<ScreenplayElement.Character>()
        val lastSpeakingCharacter = previousCharacters.lastOrNull()

        // CONT'D is ONLY needed if it is the second or greater character speaking in a row, not the first
        val shouldAddContd = !hasAlreadyContd &&
                lastSpeakingCharacter != null &&
                lastSpeakingCharacter.name.equals(baseName, ignoreCase = true)

        return if (shouldAddContd) {
            "$upperLine (CONT'D)"
        } else {
            upperLine
        }
    }

    fun handleTextChange(
        oldText: String,
        newText: String,
        cursor: Int,
        knownCharacters: List<String> = emptyList()
    ): KeyTransitionResult {
        val safeCursor = cursor.coerceIn(0, newText.length)
        val wasNewlineInserted = newText.length == oldText.length + 1 &&
                safeCursor > 0 &&
                newText[safeCursor - 1] == '\n'

        if (wasNewlineInserted) {
            val newlineIdx = safeCursor - 1
            val lastNlBefore = newText.lastIndexOf('\n', (newlineIdx - 1).coerceAtLeast(0))
            val lineStart = if (lastNlBefore >= 0) lastNlBefore + 1 else 0
            val finishedLineRaw = newText.substring(lineStart, newlineIdx)
            val finishedLineTrimmed = finishedLineRaw.trim()
            val textBeforeLine = newText.substring(0, lineStart)
            val textAfter = newText.substring(safeCursor)

            // 1. FADE IN:
            if (isFadeIn(finishedLineTrimmed)) {
                activeCharacterLineStart = null
                val formatted = "FADE IN:\n\nINT. "
                val updated = textBeforeLine + formatted + textAfter
                return KeyTransitionResult(updated, textBeforeLine.length + formatted.length)
            }

            // 2. Scene Heading: 1 blank line before action
            if (isSceneHeading(finishedLineTrimmed)) {
                activeCharacterLineStart = null
                val upper = finishedLineTrimmed.uppercase(Locale.US)
                val formatted = "$upper\n\n"
                val updated = textBeforeLine + formatted + textAfter
                return KeyTransitionResult(updated, textBeforeLine.length + formatted.length)
            }

            // 3. Character line: single newline to dialogue with 10 spaces
            val elementsBefore = parser.parse(textBeforeLine)
            val isChar = isCharacterTransition(finishedLineRaw, textBeforeLine, elementsBefore, lineStart, knownCharacters)
            if (isChar) {
                activeCharacterLineStart = null
                val upper = finishedLineTrimmed.uppercase(Locale.US)
                val formattedChar = formatCharacterWithContd(upper, textBeforeLine, elementsBefore)
                val charIndent = finishedLineRaw.takeWhile { it == ' ' }
                val formatted = "$charIndent$formattedChar\n$DIALOGUE_INDENT"
                val updated = textBeforeLine + formatted + textAfter
                return KeyTransitionResult(updated, textBeforeLine.length + formatted.length)
            }

            // 4. Parenthetical line: single newline to dialogue with 10 spaces
            if (isParentheticalLine(finishedLineTrimmed)) {
                activeCharacterLineStart = null
                val lower = autoLowercaseParenthetical(finishedLineTrimmed)
                val closed = if (lower.endsWith(")")) lower else "$lower)"
                val parenIndent = finishedLineRaw.takeWhile { it == ' ' }
                val formatted = "$parenIndent$closed\n$DIALOGUE_INDENT"
                val updated = textBeforeLine + formatted + textAfter
                return KeyTransitionResult(updated, textBeforeLine.length + formatted.length)
            }

            // 5. Dialogue line: Enter moves to new Action block (1 blank line)
            val isDial = isDialogueLine(finishedLineRaw, textBeforeLine, knownCharacters)
            if (isDial) {
                activeCharacterLineStart = null
                val cased = autoCapitalizeSentences(finishedLineRaw)
                val formatted = "$cased\n\n"
                val updated = textBeforeLine + formatted + textAfter
                return KeyTransitionResult(updated, textBeforeLine.length + formatted.length)
            }

            // 6. Action or generic line: single newline
            activeCharacterLineStart = null
            val cased = applyLineCasing(finishedLineRaw, textBeforeLine, knownCharacters, lineStart)
            val formatted = "$cased\n"
            val updated = textBeforeLine + formatted + textAfter
            return KeyTransitionResult(updated, textBeforeLine.length + formatted.length)
        }

        return formatCasing(newText, safeCursor, knownCharacters)
    }

    fun isDialogueLine(
        rawLine: String,
        textBeforeLine: String,
        knownCharacters: List<String> = emptyList()
    ): Boolean {
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty() || isSceneHeading(trimmed) || isFadeIn(trimmed) || isParentheticalLine(trimmed)) {
            return false
        }

        val leadingSpaces = rawLine.takeWhile { it == ' ' }.length
        if (leadingSpaces in 8..14) return true

        val elements = parser.parse(textBeforeLine)
        val lastElement = elements.lastOrNull()
        if (lastElement is ScreenplayElement.Dialogue) return true

        val precedingLine = getImmediatelyPrecedingLine(textBeforeLine)
        return precedingLine != null && (isCharacterCue(precedingLine, knownCharacters) || isParentheticalLine(precedingLine))
    }

    fun formatCasing(text: String, cursor: Int, knownCharacters: List<String> = emptyList()): KeyTransitionResult {
        val safeCursor = cursor.coerceIn(0, text.length)
        if (text.isEmpty()) return KeyTransitionResult(text, safeCursor)

        val lastNewline = text.lastIndexOf('\n', (safeCursor - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline >= 0) lastNewline + 1 else 0
        val nextNewline = text.indexOf('\n', safeCursor)
        val lineEnd = if (nextNewline >= 0) nextNewline else text.length

        val currentLine = text.substring(lineStart, lineEnd)
        if (currentLine.isEmpty()) return KeyTransitionResult(text, safeCursor)

        val beforeLine = text.substring(0, lineStart)
        val afterLine = text.substring(lineEnd)

        val casedLine = applyLineCasing(currentLine, beforeLine, knownCharacters, lineStart)
        if (casedLine == currentLine) {
            return KeyTransitionResult(text, safeCursor)
        }

        val newText = beforeLine + casedLine + afterLine
        val cursorOffsetInLine = (safeCursor - lineStart).coerceIn(0, casedLine.length)
        val newCursor = lineStart + cursorOffsetInLine
        return KeyTransitionResult(newText, newCursor)
    }

    fun applyLineCasing(
        currentLine: String,
        beforeLine: String,
        knownCharacters: List<String> = emptyList(),
        lineStart: Int = -1
    ): String {
        val trimmed = currentLine.trim()
        if (trimmed.isEmpty()) return currentLine

        // 1. Scene / Slugline -> ALL CAPS for all of the scene/slugline
        if (isSceneHeading(trimmed) || isFadeIn(trimmed)) {
            return currentLine.uppercase(Locale.US)
        }

        // 2. Parenthetical -> lowercase for parentheticals
        if (trimmed.startsWith("(") || isParentheticalLine(trimmed)) {
            return autoLowercaseParenthetical(currentLine)
        }

        // 3. Dialogue -> follows Character or Parenthetical with no blank line, or has dialogue indent
        if (isDialogueLine(currentLine, beforeLine, knownCharacters)) {
            return autoCapitalizeSentences(currentLine)
        }

        // 4. Character Name -> ALL CAPS
        val cleanName = trimmed.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        val hasExt = CHARACTER_EXTENSIONS.any { trimmed.contains(it, ignoreCase = true) } || trimmed.contains("(CONT'D)", ignoreCase = true)
        val isKnownChar = knownCharacters.any { it.equals(cleanName, ignoreCase = true) }
        val isTabbedChar = lineStart >= 0 && lineStart == activeCharacterLineStart
        val isForcedFountainChar = cleanName.startsWith("@")
        val isExplicitAllUpper = cleanName.length in 1..40 && cleanName.all { it.isUpperCase() || it.isDigit() || it.isWhitespace() || it == '-' || it == '\'' || it == '.' }
        val hasCharIndent = currentLine.takeWhile { it == ' ' }.length >= 15

        if (isKnownChar || hasExt || isTabbedChar || isForcedFountainChar || hasCharIndent || (isExplicitAllUpper && isPrecededByBlank(beforeLine))) {
            return currentLine.uppercase(Locale.US)
        }

        // 5. Action -> Auto-capitalize first letter of action sentences (preserve all spaces)
        return autoCapitalizeSentences(currentLine)
    }

    fun autoCapitalizeSentences(text: String): String {
        if (text.isEmpty()) return text
        val sb = StringBuilder(text.length)
        var capitalizeNext = true

        for (i in text.indices) {
            val c = text[i]
            if (capitalizeNext && c.isLetter()) {
                sb.append(c.uppercaseChar())
                capitalizeNext = false
            } else {
                if (c == 'i' && !capitalizeNext) {
                    val prevIsSpace = i == 0 || text[i - 1].isWhitespace()
                    val nextIsBoundary = i == text.length - 1 ||
                            text[i + 1].isWhitespace() ||
                            text[i + 1] == '\'' ||
                            text[i + 1] == ',' ||
                            text[i + 1] == '.' ||
                            text[i + 1] == '?' ||
                            text[i + 1] == '!'
                    if (prevIsSpace && nextIsBoundary) {
                        sb.append('I')
                    } else {
                        sb.append(c)
                    }
                } else {
                    sb.append(c)
                }

                if (c == '.' || c == '?' || c == '!') {
                    capitalizeNext = true
                } else if (capitalizeNext && !c.isWhitespace() && c != '"' && c != '\'' && c != '“' && c != '‘') {
                    capitalizeNext = false
                }
            }
        }
        return sb.toString()
    }

    fun autoLowercaseParenthetical(line: String): String {
        val openIdx = line.indexOf('(')
        if (openIdx < 0) return line
        val closeIdx = line.indexOf(')', openIdx)
        return if (closeIdx > openIdx) {
            line.substring(0, openIdx + 1) +
                    line.substring(openIdx + 1, closeIdx).lowercase(Locale.US) +
                    line.substring(closeIdx)
        } else {
            line.substring(0, openIdx + 1) +
                    line.substring(openIdx + 1).lowercase(Locale.US)
        }
    }

    private fun isCharacterCue(line: String, knownCharacters: List<String>): Boolean {
        val clean = line.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        if (clean.isEmpty()) return false
        val leadingSpaces = line.takeWhile { it == ' ' }.length
        if (leadingSpaces >= 15) return true
        return knownCharacters.any { it.equals(clean, ignoreCase = true) } ||
               (clean.length <= 40 && clean.all { it.isUpperCase() || it.isDigit() || it.isWhitespace() || it == '-' || it == '\'' || it == '.' })
    }

    private fun isPrecededByBlank(before: String): Boolean {
        if (before.isEmpty()) return true
        val trimmed = before.dropLastWhile { it == '\n' }
        val nlCount = before.length - trimmed.length
        return nlCount >= 2 || trimmed.isEmpty()
    }

    private fun getImmediatelyPrecedingLine(before: String): String? {
        if (before.isEmpty()) return null
        val trimmedEnd = before.dropLastWhile { it == '\n' }
        val lastNl = trimmedEnd.lastIndexOf('\n')
        val start = if (lastNl >= 0) lastNl + 1 else 0
        val line = trimmedEnd.substring(start).trim()
        val newlineCount = before.length - trimmedEnd.length
        if (newlineCount > 1) return null // separated by blank line, not immediate
        return line.ifEmpty { null }
    }
}
