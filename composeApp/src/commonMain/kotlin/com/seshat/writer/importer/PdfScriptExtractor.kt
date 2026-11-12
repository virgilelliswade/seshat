package com.seshat.writer.importer

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import java.io.File
import java.io.InputStream
import java.util.Locale

data class PdfPositionedLine(
    val text: String,
    val minX: Float,
    val pageIndex: Int
)

/**
 * Shared layout-aware classifier turning positioned PDF script lines into editable ScreenplayBlocks.
 */
object ScreenplayPdfClassifier {
    private val pageNumRegex = Regex("^\\s*\\d{1,3}\\.?\\s*$")
    private val pageHeaderRegex = Regex("^\\s*(PAGE\\s+\\d+|CONTINUED:?|\\(CONTINUED\\)|\\(MORE\\))\\s*$", RegexOption.IGNORE_CASE)
    private val sceneHeadingRegex = Regex("^(INT\\.|EXT\\.|INT\\./EXT\\.|EXT\\./INT\\.|I/E\\.|EST\\.)(?:\\s+.*|$)", RegexOption.IGNORE_CASE)
    private val transitionRegex = Regex("^(FADE IN:|FADE OUT\\.?|FADE TO BLACK\\.?|CUT TO:|SMASH CUT TO:|DISSOLVE TO:|MATCH CUT TO:|JUMP CUT TO:|TIME CUT:|THE END\\.?)$", RegexOption.IGNORE_CASE)

    fun classifyLines(lines: List<PdfPositionedLine>, fallbackTitle: String): ScreenplayImportDocument {
        val rawTitle = fallbackTitle
            .substringBeforeLast('.')
            .replace('_', ' ')
            .trim()
            .ifBlank { "Imported PDF Script" }

        val blocks = mutableListOf<ScreenplayBlock>()
        var lastType: BlockType? = null

        for (line in lines) {
            val trimmed = line.text.trim()
            if (trimmed.isEmpty()) {
                lastType = null
                continue
            }
            if (pageNumRegex.matches(trimmed) || pageHeaderRegex.matches(trimmed)) {
                continue
            }

            val x = line.minX
            val detectedType = when {
                sceneHeadingRegex.matches(trimmed) -> BlockType.SCENE_HEADING
                transitionRegex.matches(trimmed) || (x >= 360f && trimmed.endsWith(":")) -> BlockType.TRANSITION
                trimmed.startsWith("(") && trimmed.endsWith(")") -> BlockType.PARENTHETICAL
                x >= 228f && isLikelyCharacterCue(trimmed) -> BlockType.CHARACTER
                (lastType == BlockType.CHARACTER || lastType == BlockType.PARENTHETICAL || lastType == BlockType.DIALOGUE) &&
                    x in 150f..235f -> BlockType.DIALOGUE
                x >= 165f && x < 228f && lastType == BlockType.CHARACTER -> BlockType.DIALOGUE
                lastType == null && isLikelyCharacterCue(trimmed) && trimmed.length <= 36 -> BlockType.CHARACTER
                else -> BlockType.ACTION
            }

            // Merge consecutive wrapped lines of the same body block type cleanly
            val prev = blocks.lastOrNull()
            if (prev != null && prev.type == detectedType &&
                (detectedType == BlockType.DIALOGUE || detectedType == BlockType.ACTION)
            ) {
                blocks[blocks.lastIndex] = prev.copy(text = "${prev.text} $trimmed")
            } else {
                blocks.add(
                    ScreenplayBlock(
                        type = detectedType,
                        text = if (detectedType == BlockType.SCENE_HEADING || detectedType == BlockType.CHARACTER || detectedType == BlockType.TRANSITION) {
                            trimmed.uppercase(Locale.US)
                        } else {
                            trimmed
                        }
                    )
                )
            }
            lastType = detectedType
        }

        if (blocks.isEmpty()) {
            blocks.add(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. SCENE - DAY"))
        }

        return ScreenplayImportDocument(
            titlePage = TitlePageData(title = rawTitle),
            blocks = blocks
        )
    }

    private fun isLikelyCharacterCue(text: String): Boolean {
        val clean = text.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()
        if (clean.isEmpty() || clean.length > 38) return false
        if (clean.endsWith(".") || clean.endsWith("!") || clean.endsWith("?")) return false
        val letters = clean.filter { it.isLetter() }
        return letters.isNotEmpty() && letters.all { it.isUpperCase() }
    }
}

object PdfScriptExtractor {
    fun extractFromFile(file: File): ScriptImportResult {
        return file.inputStream().use { extractFromStream(it, file.name) }
    }

    fun extractFromStream(inputStream: InputStream, fileName: String): ScriptImportResult {
        return try {
            PDDocument.load(inputStream).use { doc ->
                val collected = mutableListOf<PdfPositionedLine>()
                for (pageIdx in 0 until doc.numberOfPages) {
                    val stripper = object : PDFTextStripper() {
                        init {
                            startPage = pageIdx + 1
                            endPage = pageIdx + 1
                            sortByPosition = true
                        }

                        override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
                            val t = text?.trimEnd() ?: return
                            val minX = textPositions?.minOfOrNull { it.xDirAdj } ?: 108f
                            collected.add(PdfPositionedLine(text = t, minX = minX, pageIndex = pageIdx))
                        }
                    }
                    stripper.getText(doc)
                    // Insert blank separator between pages
                    collected.add(PdfPositionedLine(text = "", minX = 0f, pageIndex = pageIdx))
                }
                val parsed = ScreenplayPdfClassifier.classifyLines(collected, fileName)
                ScriptImportResult(
                    isSuccess = true,
                    document = parsed,
                    detectedFormat = ScriptFormat.PDF
                )
            }
        } catch (e: Exception) {
            ScriptImportResult(
                isSuccess = false,
                errorMessage = "Failed to import PDF: ${e.localizedMessage ?: e.message}"
            )
        }
    }
}
