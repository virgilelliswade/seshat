package com.seshat.writer.ai

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import java.util.Locale

data class SmartDictationParseResult(
    val blocks: List<ScreenplayBlock> = emptyList(),
    val pendingBlockType: BlockType? = null
)

/**
 * Intelligent screenplay voice-command and natural-language dictation parser.
 *
 * Understands:
 * - Natural scene headings with commas/pauses:
 *   "interior, bedroom, night" -> SCENE_HEADING("INT. BEDROOM - NIGHT")
 *   "exterior abandoned warehouse dawn" -> SCENE_HEADING("EXT. ABANDONED WAREHOUSE - DAWN")
 * - Explicit multi-block / "next line" structural commands:
 *   "interior bedroom night. next line action: John sits up. character John. dialogue Where am I?"
 *   "action line Sarah opens the door" -> ACTION("Sarah opens the door.")
 * - Standalone mode-switch commands:
 *   "next line action", "action line", "next line dialogue", "next line scene heading"
 * - Character speech attribution:
 *   "Marcus says where is she" -> CHARACTER("MARCUS"), DIALOGUE("Where is she.")
 *   "Elena whispers don't move" -> CHARACTER("ELENA"), PARENTHETICAL("(whispers)"), DIALOGUE("Don't move.")
 * - Standard transitions:
 *   "cut to", "fade to black", "smash cut to", "dissolve to" -> TRANSITION(...)
 */
object SmartDictationParser {

    private val TIME_OF_DAY_PHRASES = listOf(
        "MOMENTS LATER",
        "SAME TIME",
        "MAGIC HOUR",
        "LATE NIGHT",
        "EARLY MORNING",
        "NEXT MORNING",
        "NEXT DAY",
        "NEXT NIGHT",
        "CONTINUOUS",
        "MORNING",
        "AFTERNOON",
        "EVENING",
        "NIGHT",
        "DAY",
        "DAWN",
        "DUSK",
        "SUNSET",
        "SUNRISE",
        "MIDNIGHT",
        "NOON",
        "LATER",
        "FLASHBACK"
    )

    private val SCENE_PREFIX_REGEX = Regex(
        """^(?:interior[\s/,&-]+exterior|int[\s./,&-]+ext|i\s*/\s*e|interior|exterior|int\.?|ext\.?)(?:\s+|[,.:-]+|$)(.*)$""",
        RegexOption.IGNORE_CASE
    )

    private val PARENTHETICAL_VERBS = mapOf(
        "WHISPERS" to "whispers",
        "WHISPERED" to "whispers",
        "YELLS" to "yells",
        "YELLED" to "yells",
        "SHOUTS" to "shouts",
        "SHOUTED" to "shouts",
        "MUTTERS" to "mutters",
        "MUMBLES" to "mumbles",
        "SIGHS" to "sighs",
        "LAUGHS" to "laughs",
        "CRIES" to "crying",
        "SCREAMS" to "screaming"
    )

    private val SPEECH_VERBS = setOf(
        "SAYS", "SAID", "ASKS", "ASKED", "REPLIES", "REPLIED", "ANSWERS", "ANSWERED",
        "TELLS", "TOLD", "WHISPERS", "WHISPERED", "YELLS", "YELLED", "SHOUTS", "SHOUTED",
        "MUTTERS", "MUMBLES", "SIGHS", "LAUGHS", "CRIES", "SCREAMS"
    )

    private val TRANSITION_PATTERNS = listOf(
        Regex("""^(?:FADE\s+IN)\.?$""", RegexOption.IGNORE_CASE) to "FADE IN:",
        Regex("""^(?:FADE\s+OUT)\.?$""", RegexOption.IGNORE_CASE) to "FADE OUT.",
        Regex("""^(?:FADE\s+TO\s+BLACK)\.?$""", RegexOption.IGNORE_CASE) to "FADE TO BLACK.",
        Regex("""^(?:CUT\s+TO(?:\s+BLACK)?)\.?$""", RegexOption.IGNORE_CASE) to "CUT TO:",
        Regex("""^(?:SMASH\s+CUT\s+TO)\.?$""", RegexOption.IGNORE_CASE) to "SMASH CUT TO:",
        Regex("""^(?:MATCH\s+CUT\s+TO)\.?$""", RegexOption.IGNORE_CASE) to "MATCH CUT TO:",
        Regex("""^(?:JUMP\s+CUT\s+TO)\.?$""", RegexOption.IGNORE_CASE) to "JUMP CUT TO:",
        Regex("""^(?:DISSOLVE\s+TO)\.?$""", RegexOption.IGNORE_CASE) to "DISSOLVE TO:"
    )

    private val COMMAND_SPLIT_REGEX = Regex(
        """(?:^|(?<=[\s.,;!?]))(?:(?:next\s+line|new\s+line|next\s+block|new\s+block)\s*[:,-]?\s*(scene\s+heading|slug\s*line|slugline|action\s+line|action|character\s+line|character|speaker|dialogue\s+line|dialogue|parenthetical|transition)|(?:next\s+line|new\s+line)\b|(scene\s+heading|slug\s*line|slugline|action\s+line|new\s+action|character\s+line|new\s+character|dialogue\s+line|parenthetical)\s*[:,-]?\s*|(?:^|(?<=[.,;!?\n]\s))\s*(action|character|dialogue|transition)\b\s*[:,-]?\s*|(?:\b(?:action|character|dialogue|transition)\s*:\s*))""",
        RegexOption.IGNORE_CASE
    )

    fun isStopCommand(spokenText: String): Boolean {
        val norm = spokenText.trim().lowercase(Locale.US).trimEnd('.', '!', '?')
        return norm in setOf(
            "stop dictation", "end dictation", "pause dictation",
            "cancel dictation", "close dictation", "stop listening"
        )
    }

    fun parseDetailed(
        spokenText: String,
        activeCharacters: List<String> = emptyList(),
        currentBlockType: BlockType = BlockType.ACTION
    ): SmartDictationParseResult {
        val trimmed = spokenText.trim()
        if (trimmed.isEmpty()) return SmartDictationParseResult()

        // Split on explicit structural commands (e.g. "next line action", "action line", "dialogue:")
        val segments = splitIntoDirectiveSegments(trimmed, currentBlockType)
        val outBlocks = mutableListOf<ScreenplayBlock>()
        var trailingEmptyType: BlockType? = null

        for ((explicitType, segmentText) in segments) {
            val cleanSegment = segmentText.trim().trimStart(':', '-', ',').trim()
            if (cleanSegment.isEmpty()) {
                if (explicitType != null) {
                    trailingEmptyType = explicitType
                }
                continue
            }
            trailingEmptyType = null

            when (explicitType) {
                BlockType.SCENE_HEADING -> {
                    outBlocks.add(
                        ScreenplayBlock(
                            type = BlockType.SCENE_HEADING,
                            text = formatSceneHeading(cleanSegment)
                        )
                    )
                }
                BlockType.ACTION -> {
                    outBlocks.add(
                        ScreenplayBlock(
                            type = BlockType.ACTION,
                            text = formatProseSentence(cleanSegment)
                        )
                    )
                }
                BlockType.CHARACTER -> {
                    val parsedChar = parseExplicitCharacterSegment(cleanSegment)
                    outBlocks.addAll(parsedChar)
                }
                BlockType.PARENTHETICAL -> {
                    val splitDiag = Regex("""^(.+?)\s+\bdialogue\b\s*[:,-]?\s*(.+)$""", RegexOption.IGNORE_CASE).matchEntire(cleanSegment)
                    if (splitDiag != null) {
                        outBlocks.add(ScreenplayBlock(type = BlockType.PARENTHETICAL, text = formatParenthetical(splitDiag.groupValues[1])))
                        outBlocks.add(ScreenplayBlock(type = BlockType.DIALOGUE, text = formatDialogueLine(splitDiag.groupValues[2])))
                    } else {
                        outBlocks.add(
                            ScreenplayBlock(
                                type = BlockType.PARENTHETICAL,
                                text = formatParenthetical(cleanSegment)
                            )
                        )
                    }
                }
                BlockType.DIALOGUE -> {
                    outBlocks.add(
                        ScreenplayBlock(
                            type = BlockType.DIALOGUE,
                            text = formatDialogueLine(cleanSegment)
                        )
                    )
                }
                BlockType.TRANSITION -> {
                    outBlocks.add(
                        ScreenplayBlock(
                            type = BlockType.TRANSITION,
                            text = formatTransition(cleanSegment)
                        )
                    )
                }
                null -> {
                    val lastType = outBlocks.lastOrNull()?.type ?: currentBlockType
                    outBlocks.addAll(
                        parseUnmarkedSegment(
                            segment = cleanSegment,
                            activeCharacters = activeCharacters,
                            contextType = lastType
                        )
                    )
                }
                else -> {
                    outBlocks.add(
                        ScreenplayBlock(
                            type = explicitType,
                            text = formatProseSentence(cleanSegment)
                        )
                    )
                }
            }
        }

        return SmartDictationParseResult(
            blocks = outBlocks,
            pendingBlockType = if (outBlocks.isEmpty()) trailingEmptyType else trailingEmptyType
        )
    }

    fun parse(
        spokenText: String,
        activeCharacters: List<String> = emptyList(),
        currentBlockType: BlockType = BlockType.ACTION
    ): List<ScreenplayBlock> = parseDetailed(spokenText, activeCharacters, currentBlockType).blocks

    private data class DirectiveSegment(
        val type: BlockType?,
        val text: String
    )

    private fun splitIntoDirectiveSegments(
        text: String,
        currentBlockType: BlockType
    ): List<DirectiveSegment> {
        val matches = COMMAND_SPLIT_REGEX.findAll(text).toList()
        if (matches.isEmpty()) {
            return listOf(DirectiveSegment(null, text))
        }

        val result = mutableListOf<DirectiveSegment>()
        var cursor = 0
        var lastKnownType = currentBlockType

        for (i in matches.indices) {
            val match = matches[i]
            if (match.range.first > cursor) {
                val leading = text.substring(cursor, match.range.first).trim()
                if (leading.isNotEmpty()) {
                    result.add(DirectiveSegment(null, leading))
                    lastKnownType = inferSegmentType(leading, lastKnownType)
                }
            }

            val directiveType = mapDirectiveToBlockType(match.value, lastKnownType)
            val contentStart = match.range.last + 1
            val contentEnd = if (i + 1 < matches.size) matches[i + 1].range.first else text.length
            val body = if (contentStart <= contentEnd) text.substring(contentStart, contentEnd).trim() else ""
            result.add(DirectiveSegment(directiveType, body))
            lastKnownType = directiveType
            cursor = contentEnd
        }

        return result
    }

    private fun mapDirectiveToBlockType(rawDirective: String, previousType: BlockType): BlockType {
        val lower = rawDirective.lowercase(Locale.US)
        return when {
            lower.contains("scene") || lower.contains("slug") -> BlockType.SCENE_HEADING
            lower.contains("action") || lower.contains("description") -> BlockType.ACTION
            lower.contains("character") || lower.contains("speaker") -> BlockType.CHARACTER
            lower.contains("parenthetical") -> BlockType.PARENTHETICAL
            lower.contains("dialogue") -> BlockType.DIALOGUE
            lower.contains("transition") -> BlockType.TRANSITION
            else -> when (previousType) {
                BlockType.SCENE_HEADING -> BlockType.ACTION
                BlockType.CHARACTER -> BlockType.DIALOGUE
                BlockType.PARENTHETICAL -> BlockType.DIALOGUE
                BlockType.TRANSITION -> BlockType.SCENE_HEADING
                else -> BlockType.ACTION
            }
        }
    }

    private fun inferSegmentType(segment: String, fallback: BlockType): BlockType {
        val trimmed = segment.trim()
        return when {
            SCENE_PREFIX_REGEX.matches(trimmed) -> BlockType.SCENE_HEADING
            TRANSITION_PATTERNS.any { it.first.matches(trimmed) } -> BlockType.TRANSITION
            else -> fallback
        }
    }

    private fun parseUnmarkedSegment(
        segment: String,
        activeCharacters: List<String>,
        contextType: BlockType
    ): List<ScreenplayBlock> {
        val trimmed = segment.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Check if segment starts with a scene heading (e.g. "interior, bedroom, night" or "Interior, bedroom, night. Elena wakes up.")
        val sceneMatch = SCENE_PREFIX_REGEX.matchEntire(trimmed)
        if (sceneMatch != null) {
            // Check if there is a sentence break (period followed by space + more text) after a time-of-day word
            val sentenceBreak = Regex(
                """^(.+?\b(?:${TIME_OF_DAY_PHRASES.joinToString("|")}))\s*\.\s+(.+)$""",
                RegexOption.IGNORE_CASE
            ).matchEntire(trimmed)
            if (sentenceBreak != null) {
                val slugPart = sentenceBreak.groupValues[1]
                val restPart = sentenceBreak.groupValues[2]
                val blocks = mutableListOf(
                    ScreenplayBlock(
                        type = BlockType.SCENE_HEADING,
                        text = formatSceneHeading(slugPart)
                    )
                )
                blocks.addAll(parse(restPart, activeCharacters, BlockType.ACTION))
                return blocks
            }
            return listOf(
                ScreenplayBlock(
                    type = BlockType.SCENE_HEADING,
                    text = formatSceneHeading(trimmed)
                )
            )
        }

        // Transitions like "cut to", "fade to black", "dissolve to"
        for ((pattern, formatted) in TRANSITION_PATTERNS) {
            if (pattern.matches(trimmed.trimEnd('.', ' '))) {
                return listOf(ScreenplayBlock(type = BlockType.TRANSITION, text = formatted))
            }
        }

        // Known active character attribution ("Marcus: ...", "Marcus says ...", "Maya whispers ...")
        val upper = trimmed.uppercase(Locale.US)
        for (char in activeCharacters) {
            val charUpper = char.trim().uppercase(Locale.US)
            if (charUpper.isBlank()) continue
            val colonMatch = Regex("""^${Regex.escape(charUpper)}\s*:\s*(.+)$""", RegexOption.IGNORE_CASE).matchEntire(trimmed)
            if (colonMatch != null) {
                return listOf(
                    ScreenplayBlock(type = BlockType.CHARACTER, text = charUpper),
                    ScreenplayBlock(type = BlockType.DIALOGUE, text = formatDialogueLine(colonMatch.groupValues[1]))
                )
            }
            val verbMatch = Regex("""^${Regex.escape(charUpper)}\s+(${SPEECH_VERBS.joinToString("|")})\b[,:\s]*(.*)$""", RegexOption.IGNORE_CASE).matchEntire(trimmed)
            if (verbMatch != null) {
                val verb = verbMatch.groupValues[1].uppercase(Locale.US)
                val line = verbMatch.groupValues[2].trim()
                val paren = PARENTHETICAL_VERBS[verb]
                val blocks = mutableListOf(ScreenplayBlock(type = BlockType.CHARACTER, text = charUpper))
                if (paren != null) {
                    blocks.add(ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "($paren)"))
                }
                if (line.isNotEmpty()) {
                    blocks.add(ScreenplayBlock(type = BlockType.DIALOGUE, text = formatDialogueLine(line)))
                }
                return blocks
            }
        }

        // Generic single/two-word proper name speech attribution ("Elena whispers don't open it", "John says hello")
        val genericVerbMatch = Regex(
            """^([A-Z][a-zA-Z'\-]{1,18}(?:\s+[A-Z][a-zA-Z'\-]{1,18})?)\s+(${SPEECH_VERBS.joinToString("|")})\b[,:\s]+(.+)$""",
            RegexOption.IGNORE_CASE
        ).matchEntire(trimmed)
        if (genericVerbMatch != null) {
            val candidateName = genericVerbMatch.groupValues[1].trim()
            val nonCharacterWords = setOf("HE", "SHE", "IT", "THEY", "WE", "YOU", "I", "SOMEONE", "NOBODY", "EVERYONE", "A VOICE", "THE VOICE")
            if (candidateName.uppercase(Locale.US) !in nonCharacterWords) {
                val verb = genericVerbMatch.groupValues[2].uppercase(Locale.US)
                val line = genericVerbMatch.groupValues[3].trim()
                val paren = PARENTHETICAL_VERBS[verb]
                val blocks = mutableListOf(ScreenplayBlock(type = BlockType.CHARACTER, text = candidateName.uppercase(Locale.US)))
                if (paren != null) {
                    blocks.add(ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "($paren)"))
                }
                blocks.add(ScreenplayBlock(type = BlockType.DIALOGUE, text = formatProseSentence(line)))
                return blocks
            }
        }

        // Context-aware continuation when user didn't give a prefix command:
        // If focused on CHARACTER and user speaks dialogue lines, auto-pair as CHARACTER or DIALOGUE cleanly
        if (contextType == BlockType.CHARACTER || contextType == BlockType.PARENTHETICAL) {
            return listOf(ScreenplayBlock(type = BlockType.DIALOGUE, text = formatDialogueLine(trimmed)))
        }

        // Default: formatted Action line
        return listOf(ScreenplayBlock(type = BlockType.ACTION, text = formatProseSentence(trimmed)))
    }

    private fun parseExplicitCharacterSegment(segment: String): List<ScreenplayBlock> {
        val trimmed = segment.trim()
        val verbOrColon = Regex(
            """^([A-Za-z0-9'\-.\s]{1,30}?)(?:\s*:\s*|\s+(?:${SPEECH_VERBS.joinToString("|")})\b[,:\s]*)(.+)$""",
            RegexOption.IGNORE_CASE
        ).matchEntire(trimmed)
        if (verbOrColon != null) {
            val charName = verbOrColon.groupValues[1].trim().uppercase(Locale.US)
            val rest = verbOrColon.groupValues[2].trim()
            return listOf(
                ScreenplayBlock(type = BlockType.CHARACTER, text = charName),
                ScreenplayBlock(type = BlockType.DIALOGUE, text = formatDialogueLine(rest))
            )
        }
        return listOf(
            ScreenplayBlock(type = BlockType.CHARACTER, text = trimmed.trimEnd('.', ',').uppercase(Locale.US))
        )
    }

    /**
     * Converts raw spoken scene heading phrases such as:
     * - "interior, bedroom, night" -> "INT. BEDROOM - NIGHT"
     * - "ext dark alley continuous" -> "EXT. DARK ALLEY - CONTINUOUS"
     * - "interior john's apartment kitchen morning" -> "INT. JOHN'S APARTMENT KITCHEN - MORNING"
     * - "interior, coffee shop, back room, late night" -> "INT. COFFEE SHOP - BACK ROOM - LATE NIGHT"
     */
    fun formatSceneHeading(raw: String): String {
        val trimmed = raw.trim().trimEnd('.', ',', ';', ':')
        val match = SCENE_PREFIX_REGEX.matchEntire(trimmed)

        val prefix: String
        val remainder: String
        if (match != null) {
            val rawPrefix = trimmed.substring(0, trimmed.length - match.groupValues[1].length).uppercase(Locale.US)
            prefix = when {
                rawPrefix.contains("INTERIOR") && rawPrefix.contains("EXTERIOR") -> "I/E."
                rawPrefix.contains("INT") && rawPrefix.contains("EXT") -> "I/E."
                rawPrefix.contains("I") && rawPrefix.contains("E") && !rawPrefix.contains("INT") && !rawPrefix.contains("EXT") -> "I/E."
                rawPrefix.contains("EXT") -> "EXT."
                else -> "INT."
            }
            remainder = match.groupValues[1].trim()
        } else {
            prefix = "INT."
            remainder = trimmed
        }

        if (remainder.isEmpty()) return prefix

        // Split comma/dash delimited chunks cleanly first (STT frequently inserts commas on pauses: "interior, bedroom, night")
        val commaChunks = remainder
            .split(Regex("""\s*[,;—–]+\s*|\s+-\s+|\.\s+"""))
            .map { it.trim().trimEnd('.').uppercase(Locale.US) }
            .filter { it.isNotEmpty() }

        if (commaChunks.size >= 2) {
            val lastChunk = commaChunks.last()
            val locationPart = commaChunks.dropLast(1).joinToString(" - ")
            return "$prefix $locationPart - $lastChunk"
        }

        // Single continuous phrase without commas: detect known Time of Day suffix at the end
        val upperRem = commaChunks.firstOrNull() ?: remainder.uppercase(Locale.US)
        val matchedTime = TIME_OF_DAY_PHRASES.firstOrNull { tod ->
            upperRem == tod || upperRem.endsWith(" $tod")
        }
        if (matchedTime != null && upperRem.length > matchedTime.length) {
            val locationOnly = upperRem.removeSuffix(" $matchedTime").trim().trimEnd('-', ',')
            if (locationOnly.isNotEmpty()) {
                return "$prefix $locationOnly - $matchedTime"
            }
        }

        return "$prefix $upperRem"
    }

    fun formatParenthetical(raw: String): String {
        val clean = raw.trim().trim('(', ')', '.', ',').lowercase(Locale.US)
        return if (clean.isBlank()) "()" else "($clean)"
    }

    fun formatTransition(raw: String): String {
        val trimmed = raw.trim()
        for ((pattern, formatted) in TRANSITION_PATTERNS) {
            if (pattern.matches(trimmed.trimEnd('.', ' '))) {
                return formatted
            }
        }
        val upper = trimmed.uppercase(Locale.US)
        return if (upper.endsWith(":") || upper.endsWith(".")) upper else "$upper:"
    }

    fun formatDialogueLine(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }
    }

    fun formatProseSentence(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        val capitalized = trimmed.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }
        return if (capitalized.endsWith(".") || capitalized.endsWith("!") ||
            capitalized.endsWith("?") || capitalized.endsWith("\"") ||
            capitalized.endsWith("”") || capitalized.endsWith("...")
        ) {
            capitalized
        } else {
            "$capitalized."
        }
    }

    /**
     * Generates a compact human-readable live preview label for VoiceDictationBar.
     */
    fun formatPreviewTag(result: SmartDictationParseResult): String {
        if (result.blocks.isNotEmpty()) {
            return result.blocks.joinToString("  •  ") { block ->
                val tag = when (block.type) {
                    BlockType.SCENE_HEADING -> "SLUGLINE"
                    BlockType.ACTION -> "ACTION"
                    BlockType.CHARACTER -> "CHARACTER"
                    BlockType.PARENTHETICAL -> "PAREN"
                    BlockType.DIALOGUE -> "DIALOGUE"
                    BlockType.TRANSITION -> "TRANSITION"
                    else -> block.type.name.replace('_', ' ')
                }
                "$tag: ${block.text.take(36)}"
            }
        }
        val pending = result.pendingBlockType
        if (pending != null) {
            return "SWITCH LINE → ${pending.name.replace('_', ' ')}"
        }
        return ""
    }
}
