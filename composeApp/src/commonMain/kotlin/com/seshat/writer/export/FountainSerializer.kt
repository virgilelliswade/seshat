package com.seshat.writer.export

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import java.io.File
import java.util.Locale

/**
 * Serializes and deserializes screenplays to and from the Fountain plain-text format (fountain.io).
 */
object FountainSerializer {

    data class FountainDocument(
        val titlePage: TitlePageData,
        val blocks: List<ScreenplayBlock>
    )

    fun exportToFountain(
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData
    ): String {
        val sb = StringBuilder()

        // 1. Title Page Metadata
        val hasMetadata = titlePageData.title.isNotBlank() || titlePageData.author.isNotBlank() ||
                titlePageData.contactInfo.isNotBlank() || titlePageData.draftInfo.isNotBlank()

        if (hasMetadata) {
            if (titlePageData.title.isNotBlank()) {
                sb.append("Title: ").append(titlePageData.title.trim()).append("\n")
            }
            if (titlePageData.writtenByLabel.isNotBlank()) {
                sb.append("Credit: ").append(titlePageData.writtenByLabel.trim()).append("\n")
            }
            if (titlePageData.author.isNotBlank()) {
                sb.append("Author: ").append(titlePageData.author.trim()).append("\n")
            }
            if (titlePageData.sourceMaterial.isNotBlank()) {
                sb.append("Source: ").append(titlePageData.sourceMaterial.trim()).append("\n")
            }
            if (titlePageData.draftInfo.isNotBlank()) {
                sb.append("Draft date: ").append(titlePageData.draftInfo.trim().replace("\n", " / ")).append("\n")
            }
            if (titlePageData.contactInfo.isNotBlank()) {
                sb.append("Contact:\n")
                for (line in titlePageData.contactInfo.lines()) {
                    sb.append("    ").append(line).append("\n")
                }
            }
            sb.append("\n") // Blank line separating metadata from screenplay body
        }

        // 2. Screenplay Blocks
        for (i in blocks.indices) {
            val block = blocks[i]
            when (block.type) {
                BlockType.SCENE_HEADING -> {
                    val text = block.text.trim().uppercase(Locale.US)
                    val base = if (isStandardSlugline(text)) text else ".$text"
                    val sceneSuffix = if (block.sceneNumber != null) " #${block.sceneNumber}#" else ""
                    sb.append(base).append(sceneSuffix).append("\n\n")
                }
                BlockType.STAGE_ACT_SCENE -> {
                    val text = block.text.trim().uppercase(Locale.US)
                    sb.append(".$text\n\n")
                }
                BlockType.COMIC_PAGE -> {
                    val text = block.text.trim().uppercase(Locale.US)
                    sb.append(".$text\n\n")
                }
                BlockType.COMIC_PANEL -> {
                    val text = block.text.trim().uppercase(Locale.US)
                    sb.append(".$text\n\n")
                }
                BlockType.ACTION, BlockType.STAGE_DIRECTION, BlockType.COMIC_ART_NOTE -> {
                    sb.append(block.text.trim()).append("\n\n")
                }
                BlockType.COMIC_CAPTION -> {
                    val raw = block.text.trim()
                    sb.append("[[$raw]]\n\n")
                }
                BlockType.COMIC_SFX -> {
                    val upper = block.text.trim().uppercase(Locale.US)
                    sb.append(">$upper<\n\n")
                }
                BlockType.CHARACTER -> {
                    val upper = block.text.trim().uppercase(Locale.US)
                    val suffix = if (block.dualSide == com.seshat.writer.model.DualDialogueSide.RIGHT) " ^" else ""
                    sb.append(upper).append(suffix).append("\n")
                }
                BlockType.PARENTHETICAL -> {
                    val raw = block.text.trim()
                    val formatted = if (raw.startsWith("(") && raw.endsWith(")")) raw else "(${raw.trim('(', ')')})"
                    sb.append(formatted).append("\n")
                }
                BlockType.DIALOGUE, BlockType.COMIC_BALLOON -> {
                    sb.append(block.text.trim()).append("\n\n")
                }
                BlockType.TRANSITION, BlockType.STAGE_CURTAIN -> {
                    val upper = block.text.trim().uppercase(Locale.US)
                    if (upper.endsWith("TO:") || upper.startsWith("FADE ")) {
                        sb.append("> ").append(upper).append("\n\n")
                    } else {
                        sb.append("> ").append(upper).append("\n\n")
                    }
                }
            }
            if (!block.note.isNullOrBlank()) {
                sb.append("[[").append(block.note.trim()).append("]]\n\n")
            }
        }

        return sb.toString()
    }

    fun importFromFountain(fountainText: String): FountainDocument {
        val lines = fountainText.lines()
        var index = 0

        var title = ""
        var writtenByLabel = "written by"
        var author = ""
        var sourceMaterial = ""
        var draftInfo = ""
        val contactLines = mutableListOf<String>()

        var inMetadata = true
        var inContactBlock = false

        // Parse Header Metadata
        while (index < lines.size && inMetadata) {
            val line = lines[index]
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                // An empty line marks the end of header metadata if we encountered metadata keys
                if (title.isNotEmpty() || author.isNotEmpty() || contactLines.isNotEmpty()) {
                    index++
                    break
                }
                index++
                continue
            }

            if (inContactBlock && (line.startsWith(" ") || line.startsWith("\t"))) {
                contactLines.add(trimmed)
                index++
                continue
            } else {
                inContactBlock = false
            }

            val colonIdx = line.indexOf(':')
            if (colonIdx > 0 && !line.startsWith(".") && !line.startsWith(">")) {
                val key = line.substring(0, colonIdx).trim().lowercase(Locale.US)
                val value = line.substring(colonIdx + 1).trim()
                when (key) {
                    "title" -> title = value
                    "credit" -> writtenByLabel = value
                    "author", "authors" -> author = value
                    "source" -> sourceMaterial = value
                    "draft date", "date" -> draftInfo = value
                    "contact" -> {
                        inContactBlock = true
                        if (value.isNotEmpty()) contactLines.add(value)
                    }
                    else -> {
                        // Unrecognized key -> likely start of script
                        inMetadata = false
                        break
                    }
                }
                index++
            } else {
                inMetadata = false
                break
            }
        }

        val contactInfo = contactLines.joinToString("\n")
        val titlePageData = TitlePageData(
            title = title,
            writtenByLabel = writtenByLabel,
            author = author,
            sourceMaterial = sourceMaterial,
            contactInfo = contactInfo,
            draftInfo = draftInfo
        )

        // Parse Script Body Lines into Blocks
        val bodyLines = lines.drop(index)
        val blocks = mutableListOf<ScreenplayBlock>()

        var lineIdx = 0
        var inDialogueGroup = false

        while (lineIdx < bodyLines.size) {
            val raw = bodyLines[lineIdx]
            val trimmed = raw.trim()

            if (trimmed.isEmpty()) {
                inDialogueGroup = false
                lineIdx++
                continue
            }

            val noteRegex = Regex("""\[\[(.*?)\]\]""")
            val inlineMatch = noteRegex.find(trimmed)
            val extractedNote = inlineMatch?.groupValues?.get(1)?.trim()
            val cleanTrimmed = if (inlineMatch != null) trimmed.removeRange(inlineMatch.range).trim() else trimmed

            if (cleanTrimmed.isEmpty()) {
                if (extractedNote != null && blocks.isNotEmpty()) {
                    val last = blocks.last()
                    val combined = if (last.note != null) "${last.note}\n$extractedNote" else extractedNote
                    blocks[blocks.lastIndex] = last.copy(note = combined)
                }
                lineIdx++
                continue
            }

            val sceneNumberRegex = Regex("""#([^#]+)#\s*$""")
            fun parseSceneHeadingAndNumber(text: String): Pair<String, String?> {
                val match = sceneNumberRegex.find(text)
                return if (match != null) {
                    val sNum = match.groupValues[1].trim()
                    val cleaned = text.removeRange(match.range).trim()
                    cleaned to sNum
                } else {
                    text to null
                }
            }

            // Forced Slugline: .INT. or .OVER BLACK
            if (cleanTrimmed.startsWith(".") && !cleanTrimmed.startsWith("...")) {
                val (slugText, sceneNum) = parseSceneHeadingAndNumber(cleanTrimmed.drop(1).trim().uppercase(Locale.US))
                blocks.add(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = slugText, sceneNumber = sceneNum, note = extractedNote))
                inDialogueGroup = false
                lineIdx++
                continue
            }

            // Transition: > CUT TO:
            if (cleanTrimmed.startsWith(">") && !cleanTrimmed.endsWith("<")) {
                val transText = cleanTrimmed.drop(1).trim().uppercase(Locale.US)
                blocks.add(ScreenplayBlock(type = BlockType.TRANSITION, text = transText, note = extractedNote))
                inDialogueGroup = false
                lineIdx++
                continue
            }

            // Standard Scene Heading: INT., EXT., etc.
            val candidateHeading = cleanTrimmed.uppercase(Locale.US)
            if (isStandardSlugline(candidateHeading)) {
                val (slugText, sceneNum) = parseSceneHeadingAndNumber(candidateHeading)
                blocks.add(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = slugText, sceneNumber = sceneNum, note = extractedNote))
                inDialogueGroup = false
                lineIdx++
                continue
            }

            // Parenthetical: (smiling)
            if (cleanTrimmed.startsWith("(") && cleanTrimmed.endsWith(")")) {
                val prevBlock = blocks.lastOrNull()
                val dualId = if (prevBlock?.dualSide == com.seshat.writer.model.DualDialogueSide.RIGHT) prevBlock.dualDialogueId else null
                val dualSide = if (dualId != null) com.seshat.writer.model.DualDialogueSide.RIGHT else null
                blocks.add(ScreenplayBlock(type = BlockType.PARENTHETICAL, text = cleanTrimmed, dualDialogueId = dualId, dualSide = dualSide, note = extractedNote))
                inDialogueGroup = true
                lineIdx++
                continue
            }

            // Character cue: All uppercase line with no punctuation at end (or preceded by blank line)
            val prevLineEmpty = lineIdx == 0 || bodyLines[lineIdx - 1].trim().isEmpty()
            val isDualCue = cleanTrimmed.endsWith("^")
            val candidateText = if (isDualCue) cleanTrimmed.dropLast(1).trim() else cleanTrimmed
            val isUpper = candidateText.length <= 40 && candidateText.all { it.isUpperCase() || it.isWhitespace() || it == '-' || it == '\'' || it == '.' || it == '(' || it == ')' }
            val endsWithTransition = candidateText.endsWith("TO:")

            if (prevLineEmpty && isUpper && !endsWithTransition) {
                if (isDualCue) {
                    val dualId = java.util.UUID.randomUUID().toString()
                    val lastCharIdx = blocks.indexOfLast { it.type == BlockType.CHARACTER }
                    if (lastCharIdx != -1) {
                        for (k in lastCharIdx until blocks.size) {
                            blocks[k] = blocks[k].copy(
                                dualDialogueId = dualId,
                                dualSide = com.seshat.writer.model.DualDialogueSide.LEFT
                            )
                        }
                    }
                    blocks.add(ScreenplayBlock(
                        type = BlockType.CHARACTER,
                        text = candidateText,
                        dualDialogueId = dualId,
                        dualSide = com.seshat.writer.model.DualDialogueSide.RIGHT,
                        note = extractedNote
                    ))
                } else {
                    blocks.add(ScreenplayBlock(type = BlockType.CHARACTER, text = candidateText, note = extractedNote))
                }
                inDialogueGroup = true
                lineIdx++
                continue
            }

            // If inside a dialogue group following Character or Parenthetical
            if (inDialogueGroup) {
                val prevBlock = blocks.lastOrNull()
                val dualId = if (prevBlock?.dualSide == com.seshat.writer.model.DualDialogueSide.RIGHT) prevBlock.dualDialogueId else null
                val dualSide = if (dualId != null) com.seshat.writer.model.DualDialogueSide.RIGHT else null
                blocks.add(ScreenplayBlock(type = BlockType.DIALOGUE, text = cleanTrimmed, dualDialogueId = dualId, dualSide = dualSide, note = extractedNote))
                lineIdx++
                continue
            }

            // Otherwise, Action
            blocks.add(ScreenplayBlock(type = BlockType.ACTION, text = cleanTrimmed, note = extractedNote))
            lineIdx++
        }

        val finalBlocks = if (blocks.isEmpty()) {
            listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = ""))
        } else {
            blocks
        }

        return FountainDocument(titlePage = titlePageData, blocks = finalBlocks)
    }

    private fun isStandardSlugline(upper: String): Boolean {
        return upper.startsWith("INT.") || upper.startsWith("EXT.") || upper.startsWith("I/E.") ||
                upper.startsWith("INT/EXT.") || upper.startsWith("EST.") || upper.startsWith("INT ") ||
                upper.startsWith("EXT ") || upper.startsWith("OVER BLACK")
    }
}
